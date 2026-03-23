package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * 预测请求中的最新指标快照。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeMetrics {

    @JsonProperty("voltage")
    Double voltage;

    @JsonProperty("temperature")
    Double temperature;

    @JsonProperty("memory_usage")
    Double memoryUsage;

    @JsonProperty("cpu_usage")
    Double cpuUsage;

    @JsonProperty("port_error_rate")
    Double portErrorRate;

    @JsonProperty("icmp_loss")
    Double icmpLoss;

    @JsonProperty("health_index_hint")
    Double healthIndexHint;
}
