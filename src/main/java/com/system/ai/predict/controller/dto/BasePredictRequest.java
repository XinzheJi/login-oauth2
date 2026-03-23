package com.system.ai.predict.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 预测请求公共字段。
 */
@Getter
@Setter
public class BasePredictRequest {

    @NotBlank(message = "deviceId 不能为空")
    private String deviceId;

    private Instant historyStart;

    private Instant historyEnd;

    @Min(value = 1, message = "historyLength 必须大于 0")
    private Integer historyLength;

    @Min(value = 1, message = "minimumHistorySize 必须大于 0")
    private Integer minimumHistorySize;

    private Instant requestTimestamp;
}
