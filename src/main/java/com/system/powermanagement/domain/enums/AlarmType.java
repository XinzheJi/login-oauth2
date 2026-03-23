package com.system.powermanagement.domain.enums;

import lombok.Getter;

/**
 * 告警类型枚举
 * 定义系统中所有可能的告警类型
 */
@Getter
public enum AlarmType {
    
    UNDER_VOLTAGE("欠压告警", "电源电压低于下限阈值"),
    OVER_VOLTAGE("过压告警", "电源电压高于上限阈值"),
    OVER_TEMPERATURE("温度过高", "设备温度超过安全阈值"),
    LOW_BATTERY("电池电量低", "电池电量低于安全阈值"),
    BATTERY_HEALTH_CRITICAL("电池健康度告警", "电池健康度低于安全阈值"),
    COMMUNICATION_FAILURE("通信失败", "设备通信中断或异常"),
    POWER_OUTAGE("电源断电", "电源供应中断"),
    HARDWARE_FAULT("硬件故障", "设备硬件异常"),
    ABNORMAL_CURRENT("电流异常", "电流值超出正常范围");
    
    /**
     * 告警类型名称
     */
    private final String name;
    
    /**
     * 告警类型描述
     */
    private final String description;
    
    AlarmType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * 根据名称获取告警类型
     * @param name 告警类型名称
     * @return 告警类型枚举，未找到则返回null
     */
    public static AlarmType getByName(String name) {
        for (AlarmType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
} 