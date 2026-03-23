package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * timemoe 老化预测响应顶层结构。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgingPredictResponse {

    @JsonProperty("success")
    boolean success;

    @JsonProperty("result")
    AgingPredictResult result;

    @JsonProperty("message")
    String message;
}
