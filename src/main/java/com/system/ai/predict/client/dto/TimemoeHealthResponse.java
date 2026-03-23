package com.system.ai.predict.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * timemoe 健康探针响应。
 */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimemoeHealthResponse {

    @JsonProperty("status")
    String status;

    @JsonProperty("model_loaded")
    Boolean modelLoaded;

    @JsonProperty("version")
    String version;

    @JsonProperty("message")
    String message;

    public boolean isHealthy() {
        return "healthy".equalsIgnoreCase(status) && Boolean.TRUE.equals(modelLoaded);
    }
}
