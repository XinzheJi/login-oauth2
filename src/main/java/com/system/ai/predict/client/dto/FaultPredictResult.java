package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.ai.predict.client.support.TimemoeInstantDeserializer;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * 故障趋势预测结果详情。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FaultPredictResult {

    @JsonProperty("device_id")
    String deviceId;

    @JsonProperty("prob_7d")
    Double probability7d;

    @JsonProperty("prob_14d")
    Double probability14d;

    @JsonProperty("prob_30d")
    Double probability30d;

    @JsonProperty("risk_level")
    String riskLevel;

    @JsonProperty("explanation")
    String explanation;

    @JsonProperty("contributors")
    @Singular
    List<TimemoeContributor> contributors;

    @JsonProperty("prediction_time")
    @JsonDeserialize(using = TimemoeInstantDeserializer.class)
    Instant predictionTime;

    @JsonProperty("forecast_interval_hours")
    Integer forecastIntervalHours;

    @JsonProperty("forecast_horizon_hours")
    Integer forecastHorizonHours;

    @JsonProperty("forecast")
    @Singular("forecastPoint")
    List<FaultForecastPoint> forecast;

    // 补充现场快照指标，便于前端展示
    @JsonProperty("memory_usage")
    Double memoryUsage;

    @JsonProperty("cpu_usage")
    Double cpuUsage;

    @JsonProperty("voltage")
    Double voltage;

    @JsonProperty("temperature")
    Double temperature;
}
