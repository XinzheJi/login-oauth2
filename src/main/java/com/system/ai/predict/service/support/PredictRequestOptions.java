package com.system.ai.predict.service.support;

import lombok.Getter;

import java.time.Instant;

/**
 * 预测请求的可选参数，用于指定历史窗口、长度等信息。
 */
@Getter
public class PredictRequestOptions {

    private static final int DEFAULT_HISTORY_LENGTH = 336;//默认历史窗口长度为 7 天
    private static final int DEFAULT_MINIMUM_HISTORY_SIZE = 64;//默认历史窗口最小长度为 64 个数据点
    private static final int MAX_HISTORY_LENGTH = 1440;//默认历史窗口最大长度为 24 小时

    private final String deviceId;
    private final Instant historyStart;
    private final Instant historyEnd;
    private final int historyLength;
    private final Instant requestTimestamp;
    private final int minimumHistorySize;


    private PredictRequestOptions(Builder builder) {
        this.deviceId = builder.deviceId;
        this.historyStart = builder.historyStart;
        this.historyEnd = builder.historyEnd;
        int sanitizedLength = sanitizeHistoryLength(builder.historyLength);
        this.historyLength = sanitizedLength;
        this.requestTimestamp = builder.requestTimestamp;
        this.minimumHistorySize = sanitizeMinimumHistorySize(builder.minimumHistorySize, sanitizedLength);
    }

    public static Builder builder() {
        return new Builder();
    }


    public static PredictRequestOptions forDevice(String deviceId) {
        return builder().deviceId(deviceId).build();
    }


    public PredictRequestOptions merge(PredictRequestOptions override) {
        if (override == null) {
            return this;
        }
        return PredictRequestOptions.builder()
                .deviceId(this.deviceId)
                .historyStart(override.historyStart != null ? override.historyStart : this.historyStart)
                .historyEnd(override.historyEnd != null ? override.historyEnd : this.historyEnd)
                .historyLength(override.historyLength)
                .requestTimestamp(override.requestTimestamp != null ? override.requestTimestamp : this.requestTimestamp)
                .minimumHistorySize(override.minimumHistorySize)
                .build();
    }

    // 确保历史窗口长度在允许范围内
    private int sanitizeHistoryLength(Integer raw) {
        int candidate = raw == null ? DEFAULT_HISTORY_LENGTH : raw;
        if (candidate <= 0) {
            candidate = DEFAULT_HISTORY_LENGTH;
        }
        return Math.min(candidate, MAX_HISTORY_LENGTH);
    }

    // 确保最小历史窗口大小在历史窗口长度范围内
    private int sanitizeMinimumHistorySize(Integer raw, int historyLength) {
        int candidate = raw == null ? DEFAULT_MINIMUM_HISTORY_SIZE : raw;
        if (candidate <= 0) {
            candidate = DEFAULT_MINIMUM_HISTORY_SIZE;
        }
        return Math.min(candidate, historyLength);
    }

    public static final class Builder {
        private String deviceId;
        private Instant historyStart;
        private Instant historyEnd;
        private Integer historyLength;
        private Instant requestTimestamp;
        private Integer minimumHistorySize;

        private Builder() {
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder historyStart(Instant historyStart) {
            this.historyStart = historyStart;
            return this;
        }

        public Builder historyEnd(Instant historyEnd) {
            this.historyEnd = historyEnd;
            return this;
        }

        public Builder historyLength(Integer historyLength) {
            this.historyLength = historyLength;
            return this;
        }

        public Builder requestTimestamp(Instant requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
            return this;
        }

        public Builder minimumHistorySize(Integer minimumHistorySize) {
            this.minimumHistorySize = minimumHistorySize;
            return this;
        }

        public PredictRequestOptions build() {
            return new PredictRequestOptions(this);
        }
    }
}
