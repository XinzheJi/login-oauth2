package com.system.powermanagement.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源设备视图对象
 */
@Data
public class PowerDeviceVO {
    
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
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 安装日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime installDate;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 租户ID (数据库中的租户主键)
     */
    private Long tenantId;
    
    /**
     * 租户业务ID (例如租户编码) - 根据实际情况决定是否需要以及如何获取
     * private String tenantBusinessId;
     */
    
    /**
     * 租户名称（非数据库字段，需要关联查询）
     */
    private String tenantName;
} 