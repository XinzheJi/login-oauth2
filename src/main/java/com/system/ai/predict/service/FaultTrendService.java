package com.system.ai.predict.service;

import com.system.ai.predict.client.TimemoeClient;
import com.system.ai.predict.client.dto.FaultPredictResponse;
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
 * 故障趋势预测服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaultTrendService {

    private final DeviceHealthRepository deviceHealthRepository;
    private final TimemoeClient timemoeClient;
    private final TimemoeRequestAssembler requestAssembler;

    public FaultPredictResponse predict(String deviceId, PredictRequestOptions options) {
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
        FaultPredictResponse response = timemoeClient.predictFault(request);
        if (response == null) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.UNKNOWN,
                    null,
                    "predict/fault",
                    null,
                    "timemoe 返回空响应",
                    null
            );
        }
        if (!response.isSuccess() || response.getResult() == null) {
            throw new TimemoeClientException(
                    TimemoeClientException.ErrorType.SERVER_ERROR,
                    null,
                    "predict/fault",
                    null,
                    response.getMessage() != null ? response.getMessage() : "timemoe 返回失败",
                    null
            );
        }
        return enrichWithSnapshot(response, snapshot);
    }

    private FaultPredictResponse enrichWithSnapshot(FaultPredictResponse response, DeviceSnapshot snapshot) {
        if (snapshot == null || response.getResult() == null) {
            return response;
        }
        var result = response.getResult();
        var builder = com.system.ai.predict.client.dto.FaultPredictResult.builder()
                .deviceId(result.getDeviceId())
                .probability7d(result.getProbability7d())
                .probability14d(result.getProbability14d())
                .probability30d(result.getProbability30d())
                .riskLevel(result.getRiskLevel())
                .explanation(result.getExplanation())
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

        return com.system.ai.predict.client.dto.FaultPredictResponse.builder()
                .success(response.isSuccess())
                .message(response.getMessage())
                .result(enrichedResult)
                .build();
    }
}
