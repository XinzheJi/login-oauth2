package com.system.powermanagement.domain.dto;

// import com.fasterxml.jackson.annotation.JsonFormat; // Commente out or remove
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源设备数据传输对象 (用于创建和更新部分字段)
 */
@Data
public class PowerDeviceDTO {
    
    // private Long id; // 数据库ID，不由客户端在DTO中提供
    // private String deviceId; // 业务ID，创建时由后端生成，更新时通过URL路径传递
    
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
     * 后端应能解析 "YYYY-MM-DDTHH:mm:ss" (ISO_LOCAL_DATE_TIME) 格式
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // Removed or commented out
    private LocalDateTime installDate;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 租户ID (数据库中的租户主键)
     * 在创建设备时，如果需要关联当前用户的租户，此字段可能不需要客户端传递，
     * 而是从TenantContext获取。这里暂时保留，具体逻辑在Service层处理。
     */
    private Long tenantId;
} 