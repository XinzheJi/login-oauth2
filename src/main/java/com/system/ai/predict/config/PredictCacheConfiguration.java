package com.system.ai.predict.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class PredictCacheConfiguration {

    public static final String SNAPSHOT_CACHE = "aiPredict:deviceSnapshot";
    public static final String METADATA_CACHE = "aiPredict:deviceMetadata";

    @Bean(name = "aiPredictCacheManager")
    public CacheManager aiPredictCacheManager(PredictCacheProperties props) {
        CaffeineCache snapshotCache = new CaffeineCache(
                SNAPSHOT_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(props.getSnapshot().getMaximumSize())
                        .expireAfterWrite(props.getSnapshot().getTtlSeconds(), TimeUnit.SECONDS)
                        .build()
        );

        CaffeineCache metadataCache = new CaffeineCache(
                METADATA_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(props.getMetadata().getMaximumSize())
                        .expireAfterWrite(props.getMetadata().getTtlSeconds(), TimeUnit.SECONDS)
                        .build()
        );

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(snapshotCache, metadataCache));
        return manager;
    }
}

