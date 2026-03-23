package com.system.powermanagement.domain.dto;

import lombok.Data;

/**
 * 电源告警查询条件
 */
@Data
public class PowerAlarmQueryDTO {
    
    /**
     * 设备ID
     */
    private String deviceId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 设备位置
     */
    private String location;
    
    /**
     * 告警类型
     */
    private String alarmType;
    
    /**
     * 告警级别
     */
    private String alarmLevel;
    
    /**
     * 是否已处理
     */
    private Boolean isProcessed;
    
    /**
     * 开始告警时间
     */
    private String alarmTimeStart;
    
    /**
     * 结束告警时间
     */
    private String alarmTimeEnd;
    
    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
} 