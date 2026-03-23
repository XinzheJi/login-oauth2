package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * timemoe 批量预测结果条目。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeBatchPredictResult {

    @JsonProperty("device_id")
    String deviceId;

    @JsonProperty("aging")
    AgingPredictResponse aging;

    @JsonProperty("fault")
    FaultPredictResponse fault;
}
