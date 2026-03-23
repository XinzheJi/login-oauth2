package com.system.powermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.powermanagement.domain.entity.PowerMonitorRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 电源监控记录Mapper接口
 */
@Mapper
public interface PowerMonitorRecordMapper extends BaseMapper<PowerMonitorRecord> {
    
    /**
     * 根据设备ID查询最新的监控记录
     * @param deviceId 设备ID
     * @param monitorName 监控项名称
     * @return 监控记录
     */
    PowerMonitorRecord selectLatestByDeviceIdAndMonitorName(
            @Param("deviceId") Long deviceId,
            @Param("monitorName") String monitorName
    );
    
    /**
     * 统计不同区域的告警数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 统计结果
     */
    List<Map<String, Object>> countAlarmByLocation(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
    
    /**
     * 统计不同告警级别的数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户ID
     * @return 统计结果
     */
    List<Map<String, Object>> countAlarmByLevel(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("tenantId") Long tenantId
    );
} 