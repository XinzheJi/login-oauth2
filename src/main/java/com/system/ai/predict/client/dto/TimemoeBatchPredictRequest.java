package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * timemoe 批量预测请求。
 * 注意：服务端要求顶层字段名为 devices（非 requests）。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeBatchPredictRequest {

    @JsonProperty("devices")
    @Singular("device")
    List<TimemoePredictRequest> devices;

    /**
     * 可选模式字段，对应 timemoe 服务的不同预测策略。
     */
    @JsonProperty("mode")
    String mode;
}
