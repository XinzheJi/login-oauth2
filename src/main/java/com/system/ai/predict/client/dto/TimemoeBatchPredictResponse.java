package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

/**
 * timemoe 批量预测响应。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeBatchPredictResponse {

    @JsonProperty("results")
    @Singular
    List<TimemoeBatchPredictResult> results;

    @JsonProperty("failed")
    Map<String, String> failed;

    @JsonProperty("failed_count")
    Integer failedCount;

    @JsonProperty("cost_ms")
    Long costMillis;
}
