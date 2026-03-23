package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.system.ai.predict.client.support.TimemoeInstantDeserializer;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * 老化预测折线点。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgingForecastPoint {

    @JsonProperty("timestamp")
    @JsonDeserialize(using = TimemoeInstantDeserializer.class)
    Instant timestamp;

    @JsonProperty("temperature")
    Double temperature;

    @JsonProperty("health_index")
    Double healthIndex;
}
