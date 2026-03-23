package com.system.powermanagement.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电源远程核容记录实体类
 */
@Data
@TableName("power_capacity_test")
public class PowerCapacityTest {
    
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
     * 执行时间
     */
    private LocalDateTime executeTime;
    
    /**
     * 持续时间（分钟）
     */
    private Integer durationMinutes;
    
    /**
     * 测试结果
     */
    private String result;
    
    /**
     * 测试前容量
     */
    private Float capacityBefore;
    
    /**
     * 测试后容量
     */
    private Float capacityAfter;
    
    /**
     * 电池健康度
     */
    private Float batteryHealth;
    
    /**
     * 曲线数据（JSON格式）
     */
    private String curveData;
    
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