package com.system.equipmenttypemanagement.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

@Data
@TableName(value = "pub_model") // 修改映射表名
public class EquipmentType {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    /**
     * 创建人
     */
    @TableField("create_user")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @TableField("update_user")
    private Long updateUser;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 是否已删除
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 型号名称
     */
    @TableField("name")
    private String name;

    /**
     * 状态：1启用 0禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 分类：字典值
     */
    @TableField("category")
    private String category;

    /**
     * 子分类：字典值，要通过字典子项，与category联动
     */
    @TableField("sub_category")
    private String subCategory;

    /**
     * 型号匹配KEY
     */
    @TableField("snmp_key")
    private String snmpKey;

    /**
     * 额外配置项：json数组，让用户添加项（key value）的方式保存
     */
    @TableField("ext_config")
    private String extConfig; // 可改为 JSON 类型如 JSONObject 或 Map<String, Object>，视具体需要而定

    /**
     * 厂商id
     */
    @TableField("manufacturer_id")
    private Long manufacturerId;

    /**
     * 是否为板卡交换机(0:否 1:是)
     */
    @TableField("board_switch")
    private String boardSwitch;

    /**
     * 别名(分号隔开)
     */
    @TableField("alias")
    private String alias;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 光旁路设备标识：1是 0否
     */
    @TableField("is_optical_bypass")
    private String isOpticalBypass;
} 