package com.system.powermanagement.service;

import com.system.powermanagement.domain.dto.AlarmProcessDTO;
import com.system.powermanagement.domain.dto.PowerAlarmQueryDTO;
import com.system.powermanagement.domain.vo.AlarmStatisticsVO;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerAlarmVO;
import com.system.powermanagement.domain.vo.Result;

/**
 * 电源告警服务接口
 */
public interface PowerAlarmService {
    
    /**
     * 分页查询告警
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<PowerAlarmVO> listAlarmByPage(PowerAlarmQueryDTO queryDTO);
    
    /**
     * 查询告警详情
     * @param alarmId 告警ID
     * @return 告警详情
     */
    PowerAlarmVO getAlarmDetail(Long alarmId);
    
    /**
     * 处理告警
     * @param processDTO 处理信息
     * @return 处理结果
     */
    Result<Boolean> processAlarm(AlarmProcessDTO processDTO);
    
    /**
     * 统计告警数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    AlarmStatisticsVO getAlarmStatistics(String startTime, String endTime);
    
    /**
     * 获取未处理的告警数量
     *
     * @return 未处理告警数量
     */
    Long getUnprocessedAlarmCount();
    
    /**
     * 获取指定设备的告警列表
     * @param deviceId 设备ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PowerAlarmVO> listAlarmByDeviceId(String deviceId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取最近的告警列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<PowerAlarmVO> listRecentAlarms(Integer pageNum, Integer pageSize);
} 