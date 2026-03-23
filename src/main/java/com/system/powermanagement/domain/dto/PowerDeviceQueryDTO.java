package com.system.powermanagement.domain.dto;

import lombok.Data;

/**
 * 电源设备查询条件
 */
@Data
public class PowerDeviceQueryDTO {
    
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
     * 开始安装日期
     */
    private String installDateStart;
    
    /**
     * 结束安装日期
     */
    private String installDateEnd;
    
    /**
     * 每页记录数
     */
    private Integer pageSize = 10;
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
} 