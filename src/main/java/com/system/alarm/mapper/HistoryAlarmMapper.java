package com.system.alarm.mapper;

import com.system.alarm.domain.entity.HistoryAlarmEntity;
import com.system.alarm.service.AlarmService;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 历史告警查询Mapper。
 */
@Mapper
public interface HistoryAlarmMapper {

    /**
     * 统计符合条件的历史告警数量。
     *
     * @param query    查询条件
     * @param tenantId 当前租户主键ID，可为空
     * @return 告警数量
     */
    long countHistoryAlarms(@Param("query") AlarmService.HistoryAlarmQuery query,
                            @Param("tenantId") Long tenantId);

    /**
     * 分页查询历史告警列表。
     *
     * @param query    查询条件
     * @param tenantId 当前租户主键ID，可为空
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 告警记录
     */
    List<HistoryAlarmEntity> selectHistoryAlarms(@Param("query") AlarmService.HistoryAlarmQuery query,
                                                 @Param("tenantId") Long tenantId,
                                                 @Param("offset") long offset,
                                                 @Param("limit") long limit);

    /**
     * 根据ID查询历史告警详情。
     *
     * @param id       告警ID
     * @param tenantId 当前租户主键ID，可为空
     * @return 历史告警实体
     */
    HistoryAlarmEntity selectHistoryAlarmById(@Param("id") Long id,
                                              @Param("tenantId") Long tenantId);
}
