package com.system.ai.predict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 预测接口异步执行配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "predict.async")
public class PredictAsyncProperties {

    /**
     * 异步处理超时时间（毫秒）。
     */
    private long timeoutMs = 30000L;

    /**
     * 线程池核心线程数。
     */
    private int corePoolSize = 8;

    /**
     * 线程池最大线程数。
     */
    private int maxPoolSize = 16;

    /**
     * 队列容量。
     */
    private int queueCapacity = 20;
}
