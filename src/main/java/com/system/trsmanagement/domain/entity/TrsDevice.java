package com.system.trsmanagement.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "trs_device")
public class TrsDevice {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否已删除（0 否，1 是）
     */
    @TableLogic
    private Short isDeleted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 所属地市
     */
    private String dscode;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备资产编码
     */
    private String code;

    /**
     * 名称拼音
     */
    private String pinyin;

    /**
     * 规范名称（区域+站点(若有)+机房(若有)+机架(若有)＋名称组成）
     */
    private String specName;

    /**
     * 规范编码（仅作保留字段）
     */
    private String specCode;

    /**
     * 规范名称拼音
     */
    private String specPinyin;

    /**
     * 采集名称
     */
    private String gatherName;

    /**
     * 站点ID
     */
    private Long stationId;

    /**
     * 机房ID
     */
    private Long deviceroomId;

    /**
     * 机架ID
     */
    private Long rackId;

    /**
     * 机架位置（U）
     */
    private Short rackU;

    /**
     * 所处位置（仅作页面回显）
     */
    private String positionFullName;

    /**
     * 型号ID
     */
    private Long modelId;

    /**
     * 设备型号（仅作页面回显）
     */
    private String modelFullName;

    /**
     * 投运状态：0未投运 1已投运 2已退运 3临时停运
     */
    private Character operatingState;

    /**
     * 投运时间（默认今天）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operatingTime;

    /**
     * 设备性质：
     * 1三层核心交换机,2三层汇聚交换机,3二层接入交换机,4三层接入交换机
     */
    private String deviceRole;

    /**
     * 设备分类：
     * switch交换机 / route路由器 / firewall防火墙
     */
    private String deviceType;

    /**
     * 是否重要设备：1是 0否
     */
    private Character important;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IPv6地址
     */
    private String ipv6;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 配置项 JSON 格式字符串（或可用 Map 结构）
     */
    private String gatherConfig;

    /**
     * 其他配置项 JSON 格式字符串
     */
    private String extConfig;

    /**
     * 来源：0手工录入 1自动发现 2文件导入
     */
    private Character source;

    /**
     * 数据域ID
     */
    private Long domainId;

    /**
     * 0:网元 1:终端
     */
    private Long neOrTerminal;

    /**
     * 使用端口数量
     */
    private Short useCount;

    /**
     * 端口总数量
     */
    private Short portCount;

    /**
     * 端口使用率
     */
    private BigDecimal useRadio;

    /**
     * 来源ID（来自生产系统、GIS系统的ID）
     */
    private String sourceId;

    /**
     * 来源名称
     */
    private String sourceName;

    /**
     * 电口总数
     */
    private Integer electricsPortCount;

    /**
     * 电口使用数
     */
    private Integer electricsPortUseCount;

    /**
     * 电口使用率
     */
    private BigDecimal electricsPortUseRatio;

    /**
     * 光口总数
     */
    private Integer fiberPortCount;

    /**
     * 光口使用数
     */
    private Integer fiberPortUseCount;

    /**
     * 光口使用率
     */
    private BigDecimal fiberPortUseRatio;

    /**
     * 台账资产（0/1）
     */
    private Short asset;

    /**
     * 台账资产唯一编码
     */
    private String assetUniqueCode;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 光旁路设备标识：1是 0否
     */
    private Character isOpticalBypass;
} 