package com.system.powermanagement.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源设备实体类
 */
@Data
@TableName("power_device")
public class PowerDevice {
    
    /**
     * 设备ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    private String deviceId; // 业务ID
    
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
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 安装日期
     */
    private LocalDateTime installDate;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
} 