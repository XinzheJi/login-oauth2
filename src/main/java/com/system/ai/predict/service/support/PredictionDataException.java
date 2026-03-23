package com.system.ai.predict.service.support;

import lombok.Getter;

/**
 * 预测前置数据缺失或不完整时抛出的异常。
 */
@Getter
public class PredictionDataException extends RuntimeException {

    private final String deviceId;
    private final Reason reason;
    private final Integer expected;
    private final Integer actual;

    private PredictionDataException(String deviceId, Reason reason, String message, Integer expected, Integer actual) {
        super(message);
        this.deviceId = deviceId;
        this.reason = reason;
        this.expected = expected;
        this.actual = actual;
    }

    public static PredictionDataException missingSnapshot(String deviceId) {
        return new PredictionDataException(
                deviceId,
                Reason.SNAPSHOT_NOT_FOUND,
                "设备无可用快照，deviceId=" + deviceId,
                null,
                null
        );
    }

    public static PredictionDataException insufficientHistory(String deviceId, int expected, int actual) {
        return new PredictionDataException(
                deviceId,
                Reason.HISTORY_INSUFFICIENT,
                String.format("设备历史序列不足，deviceId=%s, expected=%d, actual=%d", deviceId, expected, actual),
                expected,
                actual
        );
    }

    public enum Reason {
        SNAPSHOT_NOT_FOUND,
        HISTORY_INSUFFICIENT
    }
}
