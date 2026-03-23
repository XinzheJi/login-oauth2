package com.system.login.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MybatisPlus配置测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class MybatisPlusConfigTest {

    @Autowired
    private MybatisPlusInterceptor mybatisPlusInterceptor;

    @Test
    public void testMybatisPlusInterceptorCreation() {
        assertNotNull(mybatisPlusInterceptor, "MybatisPlusInterceptor should be created successfully");
        System.out.println("MybatisPlusInterceptor created successfully: " + mybatisPlusInterceptor.getClass().getName());
    }
}