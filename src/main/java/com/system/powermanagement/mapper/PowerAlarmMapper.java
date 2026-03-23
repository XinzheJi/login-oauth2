package com.system.powermanagement.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.powermanagement.domain.vo.PowerAlarmVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 电源告警Mapper接口
 */
@Mapper
public interface PowerAlarmMapper {
    
    /**
     * 分页查询告警信息
     * @param page 分页参数
     * @param
     * @param tenantId 租户ID
     * @return 分页数据
     */
    IPage<PowerAlarmVO> selectAlarmPage(
            Page<?> page,
            @Param("deviceId") String deviceId,
            @Param("deviceName") String deviceName,
            @Param("ipAddress") String ipAddress,
            @Param("location") String location,
            @Param("alarmType") String alarmType,
            @Param("alarmLevel") String alarmLevel,
            @Param("isProcessed") Boolean isProcessed,
            @Param("alarmTimeStart") String alarmTimeStart,
            @Param("alarmTimeEnd") String alarmTimeEnd,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 按告警级别统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 统计结果
     */
    List<Map<String, Object>> countByAlarmLevel(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 按监控项名称统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 统计结果
     */
    List<Map<String, Object>> countByMonitorName(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 按区域统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 统计结果
     */
    List<Map<String, Object>> countByLocation(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 统计告警趋势
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 趋势数据
     */
    List<Map<String, Object>> countAlarmTrend(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 统计告警汇总数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 汇总数据
     */
    Map<String, Object> countAlarmSummary(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
} 