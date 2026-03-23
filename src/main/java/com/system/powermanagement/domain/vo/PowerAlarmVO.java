package com.system.powermanagement.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源告警视图对象
 */
@Data
public class PowerAlarmVO {
    
    /**
     * 告警ID
     */
    private Long id;
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备IP地址
     */
    private String ipAddress;
    
    /**
     * 设备位置
     */
    private String location;
    
    /**
     * 监控项名称
     */
    private String monitorName;
    
    /**
     * 监控值
     */
    private Float value;
    
    /**
     * 上限阈值
     */
    private Float thresholdUpper;
    
    /**
     * 下限阈值
     */
    private Float thresholdLower;
    
    /**
     * 告警级别
     */
    private String alarmLevel;
    
    /**
     * 告警级别颜色
     */
    private String alarmLevelColor;
    
    /**
     * 告警描述
     */
    private String alarmDesc;
    
    /**
     * 告警时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;
    
    /**
     * 是否已处理
     */
    private Boolean isProcessed;
    
    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;
    
    /**
     * 处理人
     */
    private String processBy;
    
    /**
     * 处理描述
     */
    private String processDescription;
    
    /**
     * 处理方式
     */
    private String processMethod;
    
    /**
     * 是否已解决
     */
    private Boolean resolved;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 租户名称
     */
    private String tenantName;
} 