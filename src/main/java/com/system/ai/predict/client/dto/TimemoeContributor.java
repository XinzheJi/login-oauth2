package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * timemoe 解释项。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeContributor {

    @JsonProperty("metric")
    String metric;

    @JsonProperty("weight")
    Double weight;
}
