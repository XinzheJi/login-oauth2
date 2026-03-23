package com.system.ai.predict.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 设备在 TDengine 标签中的静态元数据。
 */
@Value
@Builder
public class DeviceMetadata {
    String deviceId;
    String deviceName;
    String vendor;
    String model;
    String batch;
    String region;
    String swVersion;
    String hwVersion;
    Instant installDate;
    Instant warrantyEnd;
}

