package com.system;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 系统B主应用程序
 */
@SpringBootApplication
@EnableConfigurationProperties
@MapperScan(basePackages = {"com.system.**.mapper",}, annotationClass = Mapper.class)
@EnableCaching
public class SystemBApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemBApplication.class, args);
    }
}


