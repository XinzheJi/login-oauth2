package com.system.alarm.controller;

import com.system.alarm.domain.dto.HistoryAlarmQueryDTO;
import com.system.alarm.domain.vo.HistoryAlarmVO;
import com.system.alarm.service.AlarmService;
import com.system.login.security.permission.RequirePermission;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 历史告警接口控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/power/history-alarms")
public class HistoryAlarmController {

    private final AlarmService alarmService;

    @Autowired
    public HistoryAlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    /**
     * 分页查询历史告警。
     *
     * @param queryDTO 查询条件
     * @return 历史告警分页结果
     */
    @PostMapping("/page")
    @RequirePermission("power:alarm:history")
    public Result<PageResult<HistoryAlarmVO>> listHistoryAlarms(@RequestBody HistoryAlarmQueryDTO queryDTO) {
        Objects.requireNonNull(queryDTO, "历史告警查询条件不能为空");
        var query = queryDTO.toQuery();
        var page = alarmService.listHistoryAlarms(query);
        List<HistoryAlarmVO> list = page.records().stream()
                .map(HistoryAlarmVO::from)
                .toList();
        PageResult<HistoryAlarmVO> pageResult = new PageResult<>(
                (long) page.pageNum(),
                (long) page.pageSize(),
                page.total(),
                list
        );
        pageResult.setPages(page.pages());
        return Result.success(pageResult);
    }

    /**
     * 获取单条历史告警详情。
     *
     * @param alarmId 告警ID
     * @return 历史告警详情
     */
    @GetMapping("/{alarmId}")
    @RequirePermission("power:alarm:history")
    public Result<HistoryAlarmVO> getHistoryAlarm(@PathVariable Long alarmId) {
        Objects.requireNonNull(alarmId, "历史告警ID不能为空");
        var view = alarmService.getHistoryAlarm(alarmId);
        if (view == null) {
            return Result.error(404, "历史告警不存在");
        }
        return Result.success(HistoryAlarmVO.from(view));
    }
}
