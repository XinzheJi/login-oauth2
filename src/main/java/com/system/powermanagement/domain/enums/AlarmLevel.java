package com.system.powermanagement.domain.enums;

import lombok.Getter;

/**
 * 告警级别枚举
 * 定义系统中不同级别的告警
 */
@Getter
public enum AlarmLevel {
    
    INFO("信息", 1, "#2196F3"),
    WARNING("警告", 2, "#FF9800"),
    CRITICAL("严重", 3, "#F44336"),
    EMERGENCY("紧急", 4, "#9C27B0");
    
    /**
     * 级别名称
     */
    private final String name;
    
    /**
     * 级别值（数字越大级别越高）
     */
    private final int level;
    
    /**
     * 级别对应的颜色（用于前端展示）
     */
    private final String color;
    
    AlarmLevel(String name, int level, String color) {
        this.name = name;
        this.level = level;
        this.color = color;
    }
    
    /**
     * 根据级别名称获取告警级别
     * @param name 级别名称
     * @return 告警级别枚举，未找到则返回null
     */
    public static AlarmLevel getByName(String name) {
        for (AlarmLevel level : values()) {
            if (level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }
} 