package com.system.ai.predict.service.support;

import com.system.ai.predict.client.dto.TimemoeHistoricalDataPoint;
import com.system.ai.predict.client.dto.TimemoeMetadata;
import com.system.ai.predict.client.dto.TimemoeMetrics;
import com.system.ai.predict.client.dto.TimemoePredictRequest;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceMetadata;
import com.system.ai.predict.model.DeviceSnapshot;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 将仓储层实体转为 timemoe 预测请求所需的结构。
 */
@Component
public class TimemoeRequestAssembler {

    public TimemoePredictRequest assemble(String deviceId,
                                          DeviceSnapshot snapshot,
                                          List<DeviceHistoryPoint> history,
                                          DeviceMetadata metadata,
                                          Instant timestamp) {
        return TimemoePredictRequest.builder()
                .deviceId(deviceId)
                .timestamp(timestamp)
                .metrics(toMetrics(snapshot))
                .metadata(toMetadata(metadata))
                .historicalData(toHistory(history))
                .build();
    }

    private TimemoeMetrics toMetrics(DeviceSnapshot snapshot) {
        if (snapshot == null) {
            // metrics 是必填字段，不能省略，返回空对象
            return TimemoeMetrics.builder().build();
        }
        return TimemoeMetrics.builder()
                .voltage(snapshot.getVoltage())
                .temperature(snapshot.getTemperature())
                .memoryUsage(toRatio(snapshot.getMemoryUsage()))
                .cpuUsage(toRatio(snapshot.getCpuUsage()))
                .portErrorRate(snapshot.getPortErrorRate())
                .icmpLoss(snapshot.getIcmpLoss())
                .healthIndexHint(snapshot.getHealthIndexHint())
                .build();
    }

    private TimemoeMetadata toMetadata(DeviceMetadata metadata) {
        if (metadata == null) {
            // FaultRequest.metadata 是必填字段，不能省略，返回空对象
            return TimemoeMetadata.builder().build();
        }
        return TimemoeMetadata.builder()
                .deviceName(metadata.getDeviceName())
                .vendor(metadata.getVendor())
                .model(metadata.getModel())
                .batch(metadata.getBatch())
                .region(metadata.getRegion())
                .swVersion(metadata.getSwVersion())
                .hwVersion(metadata.getHwVersion())
                .installDate(metadata.getInstallDate())
                .warrantyEnd(metadata.getWarrantyEnd())
                .build();
    }

    private List<TimemoeHistoricalDataPoint> toHistory(List<DeviceHistoryPoint> history) {
        if (history == null || history.isEmpty()) {
            return Collections.emptyList();
        }
        return history.stream()
                .filter(Objects::nonNull)
                .map(point -> TimemoeHistoricalDataPoint.builder()
                        .timestamp(point.getTimestamp())
                        .temperature(point.getTemperature())
                        .voltage(point.getVoltage())
                        .memoryUsage(toRatio(point.getMemoryUsage()))
                        .cpuUsage(toRatio(point.getCpuUsage()))
                        .portErrorRate(point.getPortErrorRate())
                        .icmpLoss(point.getIcmpLoss())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 将百分比值（0-100）转换为比率（0-1），timemoe 要求 0-1 范围。
     */
    private static Double toRatio(Double percentValue) {
        return percentValue != null ? percentValue / 100.0 : null;
    }
}
