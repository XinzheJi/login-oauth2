package com.system.ai.predict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "predict.cache")
public class PredictCacheProperties {

    private CacheSpec snapshot = new CacheSpec(10, 1000);
    private CacheSpec metadata = new CacheSpec(600, 10000);

    @Data
    public static class CacheSpec {
        private int ttlSeconds;
        private int maximumSize;

        public CacheSpec() {}

        public CacheSpec(int ttlSeconds, int maximumSize) {
            this.ttlSeconds = ttlSeconds;
            this.maximumSize = maximumSize;
        }
    }
}

