package com.system.trsmanagement.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源设备视图对象
 */
@Data
public class TrsDeviceVO {

    /**
     * 序号
     */
    private Integer serialNumber;

    /**
     * 型号名称
     */
    private String modelName;

    /**
     * 别名
     */
    private String alias;

    /**
     * 型号分类
     */
    private String modelCategory;

    /**
     * 所属厂商
     */
    private String manufacturer;

    /**
     * 型号标识OID
     */
    private String modelOid;

    /**
     * 关联设备数量
     */
    private Integer associatedDeviceCount;

    /**
     * 关联（正常运行）设备数量
     */
    private Integer associatedRunningDeviceCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 光旁路设备标识：1是 0否
     */
    private Character isOpticalBypass;
} 