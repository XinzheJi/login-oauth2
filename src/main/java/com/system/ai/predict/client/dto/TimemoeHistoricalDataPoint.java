package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 历史序列中的单个采样点。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeHistoricalDataPoint {

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    Instant timestamp;

    @JsonProperty("temperature")
    Double temperature;

    @JsonProperty("voltage")
    Double voltage;

    @JsonProperty("memory_usage")
    Double memoryUsage;

    @JsonProperty("cpu_usage")
    Double cpuUsage;

    @JsonProperty("port_error_rate")
    Double portErrorRate;

    @JsonProperty("icmp_loss")
    Double icmpLoss;
}
