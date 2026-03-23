package com.system.ai.predict.controller;

import com.system.ai.predict.client.dto.AgingPredictResponse;
import com.system.ai.predict.client.dto.FaultPredictResponse;
import com.system.ai.predict.client.dto.TimemoeBatchPredictResponse;
import com.system.ai.predict.config.PredictAsyncProperties;
import com.system.ai.predict.controller.dto.AgingPredictApiRequest;
import com.system.ai.predict.controller.dto.BasePredictRequest;
import com.system.ai.predict.controller.dto.FaultPredictApiRequest;
import com.system.ai.predict.controller.dto.BatchPredictApiRequest;
import com.system.ai.predict.controller.vo.ApiResponse;
import com.system.ai.predict.service.AgingPredictService;
import com.system.ai.predict.service.FaultTrendService;
import com.system.ai.predict.service.BatchPredictService;
import com.system.ai.predict.service.support.PredictRequestOptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.time.Instant;

/**
 * 预测接口。
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tool/predict")
public class PredictController {

    private final AgingPredictService agingPredictService;
    private final FaultTrendService faultTrendService;
    private final BatchPredictService batchPredictService;
    private final com.system.ai.predict.service.PredictionResultService predictionResultService;
    @Qualifier("predictTaskExecutor")
    private final AsyncTaskExecutor predictTaskExecutor;
    private final PredictAsyncProperties predictAsyncProperties;


    // 老化预测接口
    @PostMapping("/aging")
    public WebAsyncTask<ApiResponse<AgingPredictResponse>> predictAging(@Valid @RequestBody AgingPredictApiRequest request) {
        validateHistoryWindow(request);
        String taskName = "aging:" + request.getDeviceId();
        return buildAsyncTask(taskName, () -> {
            PredictRequestOptions options = buildOptions(request);
            AgingPredictResponse response = agingPredictService.predict(request.getDeviceId(), options);
            predictionResultService.saveAging(response);
            return ApiResponse.success(response);
        });
    }

    // 故障趋势预测接口
    @PostMapping("/fault")
    public WebAsyncTask<ApiResponse<FaultPredictResponse>> predictFault(@Valid @RequestBody FaultPredictApiRequest request) {
        validateHistoryWindow(request);
        String taskName = "fault:" + request.getDeviceId();
        return buildAsyncTask(taskName, () -> {
            PredictRequestOptions options = buildOptions(request);
            FaultPredictResponse response = faultTrendService.predict(request.getDeviceId(), options);
            predictionResultService.saveFault(response);
            return ApiResponse.success(response);
        });
    }

    // 批量预测接口（同时支持老化/故障，取决于 timemoe 服务端实现）
    @PostMapping("/batch")
    public ApiResponse<TimemoeBatchPredictResponse> batchPredict(@Valid @RequestBody BatchPredictApiRequest request) {
        validateHistoryWindow(request.getHistoryStart(), request.getHistoryEnd());
        TimemoeBatchPredictResponse response = batchPredictService.batchPredict(
                request.getDeviceIds(),
                request.getHistoryStart(),
                request.getHistoryEnd(),
                request.getHistoryLength(),
                request.getMinimumHistorySize(),
                request.getRequestTimestamp(),
                request.getMode()
        );
        // 批量预测结果写入历史表（与调度保持一致）
        predictionResultService.saveBatch(response);
        return ApiResponse.success(response);
    }

    // 验证历史窗口
    private void validateHistoryWindow(BasePredictRequest request) {
        validateHistoryWindow(request.getHistoryStart(), request.getHistoryEnd());
    }

    private void validateHistoryWindow(Instant start, Instant end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("historyEnd 不能早于 historyStart");
        }
    }

    // 构建预测请求选项
    private PredictRequestOptions buildOptions(BasePredictRequest request) {
        PredictRequestOptions.Builder builder = PredictRequestOptions.builder()
                .deviceId(request.getDeviceId());
        if (request.getHistoryStart() != null) {
            builder.historyStart(request.getHistoryStart());
        }
        if (request.getHistoryEnd() != null) {
            builder.historyEnd(request.getHistoryEnd());
        }
        if (request.getHistoryLength() != null) {
            builder.historyLength(request.getHistoryLength());
        }
        if (request.getMinimumHistorySize() != null) {
            builder.minimumHistorySize(request.getMinimumHistorySize());
        }
        if (request.getRequestTimestamp() != null) {
            builder.requestTimestamp(request.getRequestTimestamp());
        }
        return builder.build();
    }

    private <T> WebAsyncTask<ApiResponse<T>> buildAsyncTask(String taskName, java.util.concurrent.Callable<ApiResponse<T>> callable) {
        WebAsyncTask<ApiResponse<T>> task = new WebAsyncTask<>(
                predictAsyncProperties.getTimeoutMs(),
                predictTaskExecutor,
                () -> {
                    long start = System.currentTimeMillis();
                    try {
                        return callable.call();
                    } finally {
                        log.info("预测异步任务完成，task={}, costMs={}", taskName, System.currentTimeMillis() - start);
                    }
                }
        );
        task.onTimeout(() -> {
            log.warn("预测异步任务超时，task={}, timeoutMs={}", taskName, predictAsyncProperties.getTimeoutMs());
            return ApiResponse.failure(504, "预测处理超时，请稍后重试");
        });
        return task;
    }
}
