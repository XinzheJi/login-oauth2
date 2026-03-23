package com.system.powermanagement.controller;

import com.system.login.security.permission.RequirePermission;
import com.system.powermanagement.domain.enums.AlarmLevel;
import com.system.powermanagement.domain.enums.AlarmType;
import com.system.powermanagement.domain.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 告警选项控制器
 * 提供告警类型、级别等选项数据
 */
@Slf4j
@RestController
@RequestMapping("/api/power/alarm-options")
public class AlarmOptionController {
    
    /**
     * 获取所有告警类型选项
     */
    @GetMapping("/types")
    @RequirePermission("power:alarm")
    public Result<List<Map<String, Object>>> listAlarmTypes() {
        List<Map<String, Object>> types = Arrays.stream(AlarmType.values())
                .map(type -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", type.name());
                    map.put("name", type.getName());
                    map.put("description", type.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
        
        return Result.success(types);
    }
    
    /**
     * 获取所有告警级别选项
     */
    @GetMapping("/levels")
    @RequirePermission("power:alarm")
    public Result<List<Map<String, Object>>> listAlarmLevels() {
        List<Map<String, Object>> levels = Arrays.stream(AlarmLevel.values())
                .map(level -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", level.name());
                    map.put("name", level.getName());
                    map.put("level", level.getLevel());
                    map.put("color", level.getColor());
                    return map;
                })
                .collect(Collectors.toList());
        
        return Result.success(levels);
    }
} 