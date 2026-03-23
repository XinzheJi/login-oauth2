package com.system.powermanagement.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源监控记录实体类
 */
@Data
@TableName("power_monitor_record")
public class PowerMonitorRecord {
    
    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 监控项名称
     */
    private String monitorName;
    
    /**
     * 数据类型
     */
    private String dataType;
    
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
     * 是否告警
     */
    private Boolean isAlarm;
    
    /**
     * 告警级别
     */
    private String alarmLevel;
    
    /**
     * 告警描述
     */
    private String alarmDesc;
    
    /**
     * 标准参数
     */
    private String standardParam;
    
    /**
     * 采集时间
     */
    private LocalDateTime collectTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 是否已处理
     */
    private Boolean isProcessed;
    
    /**
     * 处理时间
     */
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
     * 例如："确认"、"忽略"、"修复"
     */
    private String processMethod;
    
    /**
     * 是否已解决
     */
    private Boolean resolved;
} 