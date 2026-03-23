package com.system.ai.predict.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 设备历史时间序列中的单个采样点。
 */
@Value
@Builder
public class DeviceHistoryPoint {
    Instant timestamp;
    Double temperature;
    Double voltage;
    Double memoryUsage;
    Double cpuUsage;
    Double portErrorRate;
    Double icmpLoss;
}

