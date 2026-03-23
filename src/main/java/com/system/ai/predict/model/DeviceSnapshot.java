package com.system.ai.predict.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 单设备最新健康指标快照。
 */
@Value
@Builder
public class DeviceSnapshot {
    Instant timestamp;
    Double voltage;
    Double temperature;
    Double memoryUsage;
    Double cpuUsage;
    Double portErrorRate;
    Double icmpLoss;
    Double healthIndexHint;
}

