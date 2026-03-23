package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 预测请求中的设备元数据。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeMetadata {

    @JsonProperty("device_name")
    String deviceName;

    @JsonProperty("vendor")
    String vendor;

    @JsonProperty("model")
    String model;

    @JsonProperty("batch")
    String batch;

    @JsonProperty("region")
    String region;

    @JsonProperty("sw_version")
    String swVersion;

    @JsonProperty("hw_version")
    String hwVersion;

    @JsonProperty("install_date")
    Instant installDate;

    @JsonProperty("warranty_end")
    Instant warrantyEnd;
}
