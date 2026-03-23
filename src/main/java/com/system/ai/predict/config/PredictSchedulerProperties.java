package com.system.ai.predict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "predict.scheduler")
public class PredictSchedulerProperties {
    private boolean enabled = false;
    private String cron = "0 0/5 * * * ?"; // 每5分钟
    private List<String> deviceIds = new ArrayList<>();
    private int batchSize = 20;
    private Integer historyLength = 336;
    private Integer minimumHistorySize = 64;
    private String mode = "default";
}

