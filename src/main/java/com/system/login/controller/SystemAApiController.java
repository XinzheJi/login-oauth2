package com.system.login.controller;

import com.system.login.service.oauth2.SystemAApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统A API控制器
 * 用于调用系统A的受保护接口并转发给前端
 */
@Slf4j
@RestController
@RequestMapping("/api/system-a")
public class SystemAApiController {

    private final SystemAApiService systemAApiService;
    
    @Autowired
    public SystemAApiController(SystemAApiService systemAApiService) {
        this.systemAApiService = systemAApiService;
    }
    
    /**
     * 获取系统A的受保护数据
     * 需要ROLE_USER角色才能访问
     */
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/protected-data")
    public ResponseEntity<Map> getProtectedData() {
        log.info("获取系统A受保护数据");
        return systemAApiService.getProtectedData();
    }
} 
