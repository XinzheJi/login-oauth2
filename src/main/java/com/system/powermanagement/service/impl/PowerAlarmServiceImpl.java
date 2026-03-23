package com.system.powermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.login.domain.entity.Tenant;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.tenant.TenantService;
import com.system.powermanagement.domain.dto.AlarmProcessDTO;
import com.system.powermanagement.domain.dto.PowerAlarmQueryDTO;
import com.system.powermanagement.domain.entity.PowerDevice;
import com.system.powermanagement.domain.entity.PowerMonitorRecord;
import com.system.powermanagement.domain.enums.AlarmLevel;
import com.system.powermanagement.domain.vo.AlarmStatisticsVO;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerAlarmVO;
import com.system.powermanagement.domain.vo.Result;
import com.system.powermanagement.mapper.PowerAlarmMapper;
import com.system.powermanagement.mapper.PowerDeviceMapper;
import com.system.powermanagement.mapper.PowerMonitorRecordMapper;
import com.system.powermanagement.service.PowerAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 电源告警服务实现类
 */
@Slf4j
@Service
public class PowerAlarmServiceImpl implements PowerAlarmService {
    
    private final PowerAlarmMapper powerAlarmMapper;
    private final PowerMonitorRecordMapper powerMonitorRecordMapper;
    private final PowerDeviceMapper powerDeviceMapper;
    private final TenantService tenantService;
    
    @Autowired
    public PowerAlarmServiceImpl(PowerAlarmMapper powerAlarmMapper, PowerMonitorRecordMapper powerMonitorRecordMapper, 
                                 PowerDeviceMapper powerDeviceMapper, TenantService tenantService) {
        this.powerAlarmMapper = powerAlarmMapper;
        this.powerMonitorRecordMapper = powerMonitorRecordMapper;
        this.powerDeviceMapper = powerDeviceMapper;
        this.tenantService = tenantService;
    }
    
    @Override
    public PageResult<PowerAlarmVO> listAlarmByPage(PowerAlarmQueryDTO queryDTO) {
        Page<PowerAlarmVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Long primaryKeyTenantId = getPrimaryKeyTenantId();
        
        IPage<PowerAlarmVO> resultPage = powerAlarmMapper.selectAlarmPage(
                page,
                queryDTO.getDeviceId(),
                queryDTO.getDeviceName(),
                queryDTO.getIpAddress(),
                queryDTO.getLocation(),
                queryDTO.getAlarmType(),
                queryDTO.getAlarmLevel(),
                queryDTO.getIsProcessed(),
                queryDTO.getAlarmTimeStart(),
                queryDTO.getAlarmTimeEnd(),
                primaryKeyTenantId
        );
        
        return new PageResult<>(
                resultPage.getCurrent(),
                resultPage.getSize(),
                resultPage.getTotal(),
                resultPage.getRecords()
        );
    }
    
    @Override
    public PowerAlarmVO getAlarmDetail(Long alarmId) {
        PowerMonitorRecord record = powerMonitorRecordMapper.selectById(alarmId);
        if (record == null || !record.getIsAlarm()) {
            log.warn("告警记录不存在或非告警记录: {}", alarmId);
            return null;
        }
        
        checkTenantAccess(record.getTenantId(), "查询");
        
        // 查询设备信息
        PowerDevice device = powerDeviceMapper.selectById(record.getDeviceId());
        if (device == null) {
            log.warn("设备不存在，设备ID: {}", record.getDeviceId());
            return null;
        }
        
        // 转换为VO
        PowerAlarmVO alarmVO = convertToVO(record, device);
        return alarmVO;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> processAlarm(AlarmProcessDTO processDTO) {
        PowerMonitorRecord record = powerMonitorRecordMapper.selectById(processDTO.getAlarmId());
        if (record == null || !record.getIsAlarm()) {
            return Result.error(404, "告警记录不存在");
        }
        
        checkTenantAccess(record.getTenantId(), "处理");
        
        // 更新处理信息
        record.setIsProcessed(true);
        record.setProcessTime(LocalDateTime.now());
        record.setProcessBy(processDTO.getProcessBy());
        record.setProcessDescription(processDTO.getProcessDescription());
        record.setProcessMethod(processDTO.getProcessMethod());
        record.setResolved(processDTO.getResolved());
        
        int rows = powerMonitorRecordMapper.updateById(record);
        if (rows > 0) {
            return Result.success(true);
        } else {
            return Result.error("处理告警失败");
        }
    }
    
    @Override
    public AlarmStatisticsVO getAlarmStatistics(String startTime, String endTime) {
        Long primaryKeyTenantId = getPrimaryKeyTenantId();
        
        // 获取汇总数据
        Map<String, Object> summaryMap = powerAlarmMapper.countAlarmSummary(startTime, endTime, primaryKeyTenantId);
        
        // 按告警级别统计
        List<Map<String, Object>> levelStats = powerAlarmMapper.countByAlarmLevel(startTime, endTime, primaryKeyTenantId);
        Map<String, Integer> levelCountMap = new HashMap<>();
        for (Map<String, Object> stat : levelStats) {
            String level = (String) stat.get("alarmLevel");
            Number count = (Number) stat.get("count");
            levelCountMap.put(level, count.intValue());
        }
        
        // 按告警类型统计
        List<Map<String, Object>> typeStats = powerAlarmMapper.countByMonitorName(startTime, endTime, primaryKeyTenantId);
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (Map<String, Object> stat : typeStats) {
            String type = (String) stat.get("monitorName");
            Number count = (Number) stat.get("count");
            typeCountMap.put(type, count.intValue());
        }
        
        // 按区域统计
        List<Map<String, Object>> locationStats = powerAlarmMapper.countByLocation(startTime, endTime, primaryKeyTenantId);
        Map<String, Integer> locationCountMap = new HashMap<>();
        for (Map<String, Object> stat : locationStats) {
            String location = (String) stat.get("location");
            Number count = (Number) stat.get("count");
            locationCountMap.put(location == null ? "未知" : location, count.intValue());
        }
        
        // 趋势数据
        List<Map<String, Object>> trendData = powerAlarmMapper.countAlarmTrend(startTime, endTime, primaryKeyTenantId);
        
        // 构建统计结果
        AlarmStatisticsVO statisticsVO = new AlarmStatisticsVO();
        statisticsVO.setTotalAlarms(summaryMap != null ? ((Number) summaryMap.getOrDefault("totalAlarms", 0)).intValue() : 0);
        statisticsVO.setUnprocessedAlarms(summaryMap != null ? ((Number) summaryMap.getOrDefault("unprocessedAlarms", 0)).intValue() : 0);
        statisticsVO.setResolvedAlarms(summaryMap != null ? ((Number) summaryMap.getOrDefault("resolvedAlarms", 0)).intValue() : 0);
        statisticsVO.setAlarmLevelCounts(levelCountMap);
        statisticsVO.setAlarmTypeCounts(typeCountMap);
        statisticsVO.setAlarmLocationCounts(locationCountMap);
        statisticsVO.setAlarmTrend(trendData);
        
        return statisticsVO;
    }
    
    @Override
    public Long getUnprocessedAlarmCount() {
        Long primaryKeyTenantId = getPrimaryKeyTenantId();
        
        LambdaQueryWrapper<PowerMonitorRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PowerMonitorRecord::getIsAlarm, true)
               .eq(PowerMonitorRecord::getIsProcessed, false);
        
        if (primaryKeyTenantId != null) {
            wrapper.eq(PowerMonitorRecord::getTenantId, primaryKeyTenantId);
        }
        
        return powerMonitorRecordMapper.selectCount(wrapper);
    }
    
    @Override
    public PageResult<PowerAlarmVO> listAlarmByDeviceId(String deviceId, Integer pageNum, Integer pageSize) {
        // 先查询设备
        PowerDevice device = powerDeviceMapper.selectOne(new LambdaQueryWrapper<PowerDevice>().eq(PowerDevice::getDeviceId, deviceId));
        if (device == null) {
            log.warn("设备不存在，deviceId: {}", deviceId);
            return new PageResult<>(1L, pageSize.longValue(), 0L, List.of());
        }
        
        checkTenantAccess(device.getTenantId(), "查询");
        
        // 查询告警
        PowerAlarmQueryDTO queryDTO = new PowerAlarmQueryDTO();
        queryDTO.setDeviceId(deviceId);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        return listAlarmByPage(queryDTO);
    }
    
    @Override
    public PageResult<PowerAlarmVO> listRecentAlarms(Integer pageNum, Integer pageSize) {
        PowerAlarmQueryDTO queryDTO = new PowerAlarmQueryDTO();
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        return listAlarmByPage(queryDTO);
    }
    
    /**
     * 转换告警记录为VO
     * @param record 告警记录
     * @param device 设备信息
     * @return 告警VO
     */
    private PowerAlarmVO convertToVO(PowerMonitorRecord record, PowerDevice device) {
        PowerAlarmVO vo = new PowerAlarmVO();
        vo.setId(record.getId());
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceName(device.getDeviceName());
        vo.setIpAddress(device.getIpAddress());
        vo.setLocation(device.getLocation());
        vo.setMonitorName(record.getMonitorName());
        vo.setValue(record.getValue());
        vo.setThresholdUpper(record.getThresholdUpper());
        vo.setThresholdLower(record.getThresholdLower());
        vo.setAlarmLevel(record.getAlarmLevel());
        vo.setAlarmLevelColor(getAlarmLevelColor(record.getAlarmLevel()));
        vo.setAlarmDesc(record.getAlarmDesc());
        vo.setCollectTime(record.getCollectTime());
        vo.setIsProcessed(record.getIsProcessed());
        vo.setProcessTime(record.getProcessTime());
        vo.setProcessBy(record.getProcessBy());
        vo.setProcessDescription(record.getProcessDescription());
        vo.setProcessMethod(record.getProcessMethod());
        vo.setResolved(record.getResolved());
        vo.setTenantId(record.getTenantId());
        
        // 设置租户名称
        if (record.getTenantId() != null) {
            Tenant tenant = tenantService.getById(record.getTenantId());
            if (tenant != null) {
                vo.setTenantName(tenant.getTenantName());
            }
        }
        
        return vo;
    }
    
    /**
     * 获取当前租户主键ID
     */
    private Long getPrimaryKeyTenantId() {
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId == null) {
            return null;
        }
        
        Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
        if (tenant == null) {
            log.warn("根据业务租户ID {} 未找到对应租户记录", businessTenantId);
            return null;
        }
        return tenant.getId();
    }
    
    /**
     * 根据告警级别获取颜色
     * @param alarmLevel 告警级别
     * @return 颜色代码
     */
    private String getAlarmLevelColor(String alarmLevel) {
        if (alarmLevel == null) {
            return "#909399"; // 默认灰色
        }

        switch (alarmLevel) {
            case "信息":
            case "INFO":
                return "#2196F3"; // 蓝色
            case "警告":
            case "WARNING":
                return "#FF9800"; // 橙色
            case "严重":
            case "CRITICAL":
                return "#F44336"; // 红色
            case "紧急":
            case "EMERGENCY":
                return "#9C27B0"; // 紫色
            default:
                return "#909399"; // 默认灰色
        }
    }

    /**
     * 检查租户访问权限
     * @param resourceTenantId 资源所属租户ID
     * @param operationType 操作类型
     * @throws RuntimeException 如果无权限
     */
    private void checkTenantAccess(Long resourceTenantId, String operationType) {
        String currentBusinessTenantId = TenantContext.getCurrentTenant();
        if (currentBusinessTenantId != null) {
            Tenant currentTenant = tenantService.getByBusinessTenantId(currentBusinessTenantId);
            if (currentTenant == null || !resourceTenantId.equals(currentTenant.getId())) {
                log.warn("无权{}其他租户的告警数据，所属租户主键ID: {}, 当前业务租户ID: {}",
                        operationType, resourceTenantId, currentBusinessTenantId);
                throw new RuntimeException("无权操作属于其他租户的告警数据");
            }
        } else {
            throw new RuntimeException("无法在无租户上下文中操作告警数据");
        }
    }
} 