package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * timemoe 服务的通用预测请求体。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoePredictRequest {

    @JsonProperty("device_id")
    String deviceId;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    Instant timestamp;

    @JsonProperty("metrics")
    TimemoeMetrics metrics;

    @JsonProperty("metadata")
    TimemoeMetadata metadata;

    @JsonProperty("historical_data")
    @Singular("historicalPoint")
    List<TimemoeHistoricalDataPoint> historicalData;
}
