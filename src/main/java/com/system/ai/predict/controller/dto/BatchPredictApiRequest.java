package com.system.ai.predict.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * 批量预测接口入参。
 */
@Getter
@Setter
public class BatchPredictApiRequest {

    @NotEmpty(message = "deviceIds 不能为空")
    private List<String> deviceIds;

    private Instant historyStart;

    private Instant historyEnd;

    @Min(value = 1, message = "historyLength 必须大于 0")
    private Integer historyLength;

    @Min(value = 1, message = "minimumHistorySize 必须大于 0")
    private Integer minimumHistorySize;

    private Instant requestTimestamp;

    /**
     * 可选批量预测模式，透传至 timemoe 服务。
     */
    private String mode;
}

