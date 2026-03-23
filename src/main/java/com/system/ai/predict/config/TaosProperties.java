package com.system.ai.predict.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "taos")
public class TaosProperties {
    private String baseUrl = "http://172.30.154.81:6041";
    private String database = "tk";
    private String username = "root";
    private String password = "root";
    private int timeoutMs = 5000;

    private String healthStable = "t_device_health";
    private boolean useSubTables = true;
    private int historyDefaultLength = 336;
}

