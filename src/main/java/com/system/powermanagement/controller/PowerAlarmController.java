package com.system.powermanagement.controller;

import com.system.login.security.permission.RequirePermission;
import com.system.powermanagement.domain.dto.AlarmProcessDTO;
import com.system.powermanagement.domain.dto.PowerAlarmQueryDTO;
import com.system.powermanagement.domain.vo.AlarmStatisticsVO;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerAlarmVO;
import com.system.powermanagement.domain.vo.Result;
import com.system.powermanagement.service.PowerAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 电源告警控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/power/alarms")
public class PowerAlarmController {
    
    private final PowerAlarmService powerAlarmService;
    
    @Autowired
    public PowerAlarmController(PowerAlarmService powerAlarmService) {
        this.powerAlarmService = powerAlarmService;
    }
    
    /**
     * 分页查询告警
     */
    @GetMapping
    @RequirePermission("power:alarm")
    public Result<PageResult<PowerAlarmVO>> listByPage(PowerAlarmQueryDTO queryDTO) {
        // 检查并设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        
        log.info("分页查询告警，参数：pageNum={}, pageSize={}", queryDTO.getPageNum(), queryDTO.getPageSize());
        
        PageResult<PowerAlarmVO> pageResult = powerAlarmService.listAlarmByPage(queryDTO);
        return Result.success(pageResult);
    }
    
    /**
     * 查询告警详情
     */
    @GetMapping("/{alarmId}")
    @RequirePermission("power:alarm")
    public Result<PowerAlarmVO> getDetail(@PathVariable Long alarmId) {
        PowerAlarmVO alarm = powerAlarmService.getAlarmDetail(alarmId);
        if (alarm == null) {
            return Result.error(404, "告警不存在");
        }
        return Result.success(alarm);
    }
    
    /**
     * 处理告警
     */
    @PostMapping("/process")
    @RequirePermission("power:alarm:process")
    public Result<Boolean> processAlarm(@RequestBody AlarmProcessDTO processDTO) {
        return powerAlarmService.processAlarm(processDTO);
    }
    
    /**
     * 统计告警数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics")
    @RequirePermission("power:alarm")
    public Result<AlarmStatisticsVO> getAlarmStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        AlarmStatisticsVO statistics = powerAlarmService.getAlarmStatistics(startTime, endTime);
        return Result.success(statistics);
    }
    
    /**
     * 获取未处理的告警数量
     */
    @GetMapping("/unprocessed/count")
    @RequirePermission("power:alarm")
    public Result<Integer> getUnprocessedAlarmCount() {
        Long count = powerAlarmService.getUnprocessedAlarmCount();
        return Result.success(Math.toIntExact(count));
    }
    
    /**
     * 获取指定设备的告警列表
     */
    @GetMapping("/device/{deviceId}")
    @RequirePermission("power:alarm")
    public Result<PageResult<PowerAlarmVO>> getDeviceAlarms(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 检查并设置默认分页参数
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        
        PageResult<PowerAlarmVO> pageResult = powerAlarmService.listAlarmByDeviceId(deviceId, pageNum, pageSize);
        return Result.success(pageResult);
    }
    
    /**
     * 获取最近的告警列表
     */
    @GetMapping("/recent")
    @RequirePermission("power:alarm")
    public Result<PageResult<PowerAlarmVO>> getRecentAlarms(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 检查并设置默认分页参数
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        
        PageResult<PowerAlarmVO> pageResult = powerAlarmService.listRecentAlarms(pageNum, pageSize);
        return Result.success(pageResult);
    }
} 