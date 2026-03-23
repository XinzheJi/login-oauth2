package com.system.ai.predict.service;

import com.system.ai.predict.client.TimemoeClient;
import com.system.ai.predict.client.dto.*;
import com.system.ai.predict.client.exception.TimemoeClientException;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceMetadata;
import com.system.ai.predict.model.DeviceSnapshot;
import com.system.ai.predict.repository.DeviceHealthRepository;
import com.system.ai.predict.config.TimemoeProperties;
import com.system.ai.predict.service.support.PredictRequestOptions;
import com.system.ai.predict.service.support.TimemoeRequestAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量预测服务，支持对多台设备一次性执行老化/故障预测。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchPredictService {

    private final DeviceHealthRepository deviceHealthRepository;
    private final TimemoeClient timemoeClient;
    private final TimemoeRequestAssembler requestAssembler;
    private final TimemoeProperties timemoeProperties;

    public TimemoeBatchPredictResponse batchPredict(Collection<String> deviceIds,
                                                    Instant historyStart,
                                                    Instant historyEnd,
                                                    Integer historyLength,
                                                    Integer minimumHistorySize,
                                                    Instant requestTimestamp,
                                                    String mode) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            throw new IllegalArgumentException("deviceIds 不能为空");
        }

        // 去重并清洗空白ID
        List<String> ids = deviceIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("deviceIds 不能为空");
        }

        Map<String, String> localFailures = new LinkedHashMap<>();
        List<TimemoePredictRequest> prepared = new ArrayList<>(ids.size());

        // 预取最新快照（可选带窗口起点）
        Map<String, DeviceSnapshot> snapshots = deviceHealthRepository.fetchBatchLatestSnapshots(ids, historyStart);

        // 逐设备组装请求
        for (String deviceId : ids) {
            DeviceSnapshot snapshot = snapshots.get(deviceId);
            if (snapshot == null) {
                localFailures.put(deviceId, "缺少最新快照");
                continue;
            }

            // 组装并清洗选项（沿用单设备逻辑的默认/上限）
            PredictRequestOptions options = PredictRequestOptions.builder()
                    .deviceId(deviceId)
                    .historyStart(historyStart)
                    .historyEnd(historyEnd)
                    .historyLength(historyLength)
                    .minimumHistorySize(minimumHistorySize)
                    .requestTimestamp(requestTimestamp)
                    .build();

            List<DeviceHistoryPoint> history = deviceHealthRepository.fetchHistorySeries(
                    deviceId,
                    options.getHistoryStart(),
                    options.getHistoryEnd(),
                    options.getHistoryLength()
            );

            if (history.size() < options.getMinimumHistorySize()) {
                localFailures.put(deviceId, "历史样本不足：" + history.size() + "/" + options.getMinimumHistorySize());
                continue;
            }

            DeviceMetadata metadata = deviceHealthRepository.fetchMetadata(deviceId).orElse(null);
            Instant ts = options.getRequestTimestamp() != null ? options.getRequestTimestamp() : Instant.now();

            TimemoePredictRequest req = requestAssembler.assemble(deviceId, snapshot, history, metadata, ts);
            prepared.add(req);
        }

        if (prepared.isEmpty()) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.CLIENT_ERROR,
                    422,
                    "batch/predict",
                    null,
                    "全部设备缺少快照或历史样本不足，无法执行批量预测",
                    null
            );
        }

        TimemoeBatchPredictRequest batchRequest = TimemoeBatchPredictRequest.builder()
                .devices(prepared)
                .mode(mode)
                .build();

        TimemoeBatchPredictResponse tm;
        try {
            tm = timemoeClient.batchPredict(batchRequest);
        } catch (TimemoeClientException ex) {
            // timemoe 批量接口可能对历史长度校验更严格（如要求=336），当出现 422/4xx 时可按配置回退到逐设备单发
            boolean canFallback = Boolean.TRUE.equals(timemoeProperties.isBatchFallbackToSingle());
            Integer status = ex.getStatusCode();
            if (canFallback && status != null && status >= 400 && status < 500) {
                log.warn("批量预测调用失败(status={}), 启用单设备回退策略: {} 台", status, prepared.size());
                tm = fallbackBySingle(prepared);
            } else {
                throw ex;
            }
        }
        if (tm == null) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.UNKNOWN,
                    null,
                    "batch/predict",
                    null,
                    "timemoe 返回空响应",
                    null
            );
        }

        boolean noDetails = tm.getResults() != null && !tm.getResults().isEmpty()
                && tm.getResults().stream().allMatch(r -> r.getAging() == null && r.getFault() == null);

        TimemoeBatchPredictResponse finalResp = tm;
        if (noDetails && Boolean.TRUE.equals(timemoeProperties.isBatchFallbackToSingle())) {
            log.warn("批量预测返回结果不含明细(仅device_id)，启用单设备回退补全明细: {} 台", prepared.size());
            finalResp = fallbackBySingle(prepared);
        }

        // 合并本地装配失败与 timemoe 失败条目（包含回退结果）
        Map<String, String> mergedFailed = new LinkedHashMap<>();
        if (finalResp.getFailed() != null) {
            mergedFailed.putAll(finalResp.getFailed());
        }
        if (!localFailures.isEmpty()) {
            mergedFailed.putAll(localFailures);
        }
        int tmFailedCount = finalResp.getFailedCount() != null ? finalResp.getFailedCount() : (finalResp.getFailed() != null ? finalResp.getFailed().size() : 0);
        int totalFailed = tmFailedCount + localFailures.size();

        return TimemoeBatchPredictResponse.builder()
                .results(finalResp.getResults())
                .failed(mergedFailed.isEmpty() ? null : mergedFailed)
                .failedCount(totalFailed)
                .costMillis(finalResp.getCostMillis())
                .build();
    }

    private TimemoeBatchPredictResponse fallbackBySingle(List<TimemoePredictRequest> prepared) {
        List<TimemoeBatchPredictResult> results = new ArrayList<>();
        Map<String, String> failed = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        for (TimemoePredictRequest req : prepared) {
            String deviceId = req.getDeviceId();
            AgingPredictResponse aging = null;
            FaultPredictResponse fault = null;
            try {
                aging = timemoeClient.predictAging(req);
            } catch (TimemoeClientException e) {
                failed.put(deviceId, (failed.getOrDefault(deviceId, "") + " aging:" + safeMsg(e)).trim());
            } catch (Exception e) {
                failed.put(deviceId, (failed.getOrDefault(deviceId, "") + " aging:UNKNOWN").trim());
            }
            try {
                fault = timemoeClient.predictFault(req);
            } catch (TimemoeClientException e) {
                failed.put(deviceId, (failed.getOrDefault(deviceId, "") + " fault:" + safeMsg(e)).trim());
            } catch (Exception e) {
                failed.put(deviceId, (failed.getOrDefault(deviceId, "") + " fault:UNKNOWN").trim());
            }
            if (aging != null || fault != null) {
                results.add(TimemoeBatchPredictResult.builder()
                        .deviceId(deviceId)
                        .aging(aging)
                        .fault(fault)
                        .build());
            }
        }
        long cost = System.currentTimeMillis() - start;
        return TimemoeBatchPredictResponse.builder()
                .results(results)
                .failed(failed.isEmpty() ? null : failed)
                .failedCount(failed.size())
                .costMillis(cost)
                .build();
    }

    private String safeMsg(TimemoeClientException e) {
        String m = e.getMessage();
        return m == null ? e.getErrorType().name() : m;
    }
}
