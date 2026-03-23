package com.system.ai.predict.service;

import com.system.ai.predict.client.TimemoeClient;
import com.system.ai.predict.client.dto.AgingPredictResponse;
import com.system.ai.predict.client.dto.TimemoePredictRequest;
import com.system.ai.predict.client.exception.TimemoeClientException;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceMetadata;
import com.system.ai.predict.model.DeviceSnapshot;
import com.system.ai.predict.repository.DeviceHealthRepository;
import com.system.ai.predict.service.support.PredictRequestOptions;
import com.system.ai.predict.service.support.PredictionDataException;
import com.system.ai.predict.service.support.TimemoeRequestAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 老化预测服务，负责将仓储数据组装后调用 timemoe 推理接口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgingPredictService {

    private final DeviceHealthRepository deviceHealthRepository;
    private final TimemoeClient timemoeClient;
    private final TimemoeRequestAssembler requestAssembler;

    public AgingPredictResponse predict(String deviceId, PredictRequestOptions options) {
        PredictRequestOptions effective = PredictRequestOptions.forDevice(deviceId).merge(options);

        DeviceSnapshot snapshot = deviceHealthRepository.fetchLatestSnapshot(deviceId)
                .orElseThrow(() -> PredictionDataException.missingSnapshot(deviceId));

        List<DeviceHistoryPoint> history = deviceHealthRepository.fetchHistorySeries(
                deviceId,
                effective.getHistoryStart(),
                effective.getHistoryEnd(),
                effective.getHistoryLength()
        );

        if (history.size() < effective.getMinimumHistorySize()) {
            throw PredictionDataException.insufficientHistory(deviceId, effective.getMinimumHistorySize(), history.size());
        }

        DeviceMetadata metadata = deviceHealthRepository.fetchMetadata(deviceId).orElse(null);
        Instant timestamp = effective.getRequestTimestamp() != null ? effective.getRequestTimestamp() : Instant.now();

        TimemoePredictRequest request = requestAssembler.assemble(deviceId, snapshot, history, metadata, timestamp);
        log.info("发送给timemoe的请求: deviceId={}, historySize={}, snapshot={}, metadata={}",
                 deviceId, history.size(), snapshot, metadata);
        AgingPredictResponse response = timemoeClient.predictAging(request);
        if (response == null) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.UNKNOWN,
                    null,
                    "predict/aging",
                    null,
                    "timemoe 返回空响应",
                    null
            );
        }
        if (!response.isSuccess() || response.getResult() == null) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.SERVER_ERROR,
                    null,
                    "predict/aging",
                    null,
                    response.getMessage() != null ? response.getMessage() : "timemoe 返回失败",
                    null
            );
        }
        return enrichWithSnapshot(response, snapshot);
    }

    private AgingPredictResponse enrichWithSnapshot(AgingPredictResponse response, DeviceSnapshot snapshot) {
        if (snapshot == null || response.getResult() == null) {
            return response;
        }
        var result = response.getResult();
        var builder = com.system.ai.predict.client.dto.AgingPredictResult.builder()
                .deviceId(result.getDeviceId())
                .healthIndex(result.getHealthIndex())
                .tteHours(result.getTteHours())
                .rulHours(result.getRulHours())
                .riskLevel(result.getRiskLevel())
                .predictionTime(result.getPredictionTime())
                .forecastIntervalHours(result.getForecastIntervalHours())
                .forecastHorizonHours(result.getForecastHorizonHours())
                .memoryUsage(snapshot.getMemoryUsage())
                .cpuUsage(snapshot.getCpuUsage())
                .voltage(snapshot.getVoltage())
                .temperature(snapshot.getTemperature());

        if (result.getContributors() != null) {
            builder.contributors(result.getContributors());
        }
        if (result.getForecast() != null) {
            builder.forecast(result.getForecast());
        }

        var enrichedResult = builder.build();

        return com.system.ai.predict.client.dto.AgingPredictResponse.builder()
                .success(response.isSuccess())
                .message(response.getMessage())
                .result(enrichedResult)
                .build();
    }
}
