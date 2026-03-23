package com.mock.a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 系统A模拟服务应用程序入口
 */
@SpringBootApplication
@EnableConfigurationProperties
public class MockApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockApplication.class, args);
    }
} 