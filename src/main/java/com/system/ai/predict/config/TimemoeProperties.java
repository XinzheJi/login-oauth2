package com.system.ai.predict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * timemoe 推理服务客户端配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "timemoe")
public class TimemoeProperties {

    /**
     * 服务基础地址，例如 http://host:8000。
     */
    private String baseUrl = "http://localhost:8000";

    /**
     * 健康探针路径。
     */
    private String healthPath = "/api/v1/health";

    /**
     * 老化预测路径。
     */
    private String agingPath = "/api/v1/predict/aging";

    /**
     * 故障预测路径。
     */
    private String faultPath = "/api/v1/predict/fault";

    /**
     * 批量预测路径。
     */
    private String batchPath = "/api/v1/batch/predict";

    /**
     * 连接超时（毫秒）。
     */
    private int connectTimeoutMs = 2000;

    /**
     * 读取超时（毫秒）。
     */
    private int readTimeoutMs = 5000;

    /**
     * 调用失败时默认重试次数。
     */
    private int maxRetries = 1;

    /**
     * 可选的 API Key。
     */
    private String apiKey;

    /**
     * API Key 所使用的头名称，默认 Authorization Bearer。
     */
    private String apiKeyHeader = "Authorization";

    /**
     * 当使用 Authorization 时是否自动补全 Bearer 前缀。
     */
    private boolean apiKeyUseBearer = true;

    /**
     * 当批量预测请求返回 4xx（如 422）时，是否自动回退为逐设备单发。
     */
    private boolean batchFallbackToSingle = true;

    /**
     * 调用 timemoe 的最大并发数。
     */
    private int maxConcurrent = 12;

    /**
     * 获取并发许可超时时间（毫秒）。
     */
    private long acquireTimeoutMs = 80L;
}
