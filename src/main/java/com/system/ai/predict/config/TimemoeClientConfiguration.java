package com.system.ai.predict.config;

import com.system.ai.predict.client.HttpTimemoeClient;
import com.system.ai.predict.client.TimemoeClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * timemoe 客户端配置。
 */
@Configuration
public class TimemoeClientConfiguration {

    private final TimemoeProperties properties;

    public TimemoeClientConfiguration(TimemoeProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "timemoeRestTemplate")
    public RestTemplate timemoeRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }

    @Bean
    @ConditionalOnMissingBean(TimemoeClient.class)
    public TimemoeClient timemoeClient(@Qualifier("timemoeRestTemplate") RestTemplate restTemplate) {
        return new HttpTimemoeClient(restTemplate, properties);
    }
}
