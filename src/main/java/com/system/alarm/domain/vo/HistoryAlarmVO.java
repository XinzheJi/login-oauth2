package com.system.alarm.domain.vo;

import com.system.alarm.service.AlarmService;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史告警返回VO。
 */
@Data
public class HistoryAlarmVO {

    private Long id;
    private Long createUser;
    private LocalDateTime createTime;
    private Long updateUser;
    private LocalDateTime updateTime;
    private String remark;
    private String name;
    private Long areaId;
    private String code;
    private Long levelId;
    private Long alarmConfigId;
    private String alarmResourceType;
    private Long alarmResourceId;
    private String alarmResourceName;
    private Integer status;
    private String alarmSource;
    private Long alarmSourceId;
    private String alarmValue;
    private String reAlarmSource;
    private Long reAlarmSourceId;
    private String reAlarmValue;
    private String confirmStatus;
    private String confirmUser;
    private Long confirmUserId;
    private LocalDateTime confirmTime;
    private String reason;
    private String suggestion;
    private LocalDateTime reAlarmTime;
    private String alarmResourceDetailsType;
    private String alarmResourceDetailsName;
    private Long alarmResourceDetailsId;
    private String alarmResourceDetailsIndex;
    private String alarmType;

    /**
     * 将服务层视图对象转换为VO。
     *
     * @param view 历史告警视图
     * @return VO对象
     */
    public static HistoryAlarmVO from(AlarmService.HistoryAlarmView view) {
        if (view == null) {
            return null;
        }
        HistoryAlarmVO vo = new HistoryAlarmVO();
        vo.setId(view.id());
        vo.setCreateUser(view.createUser());
        vo.setCreateTime(view.createTime());
        vo.setUpdateUser(view.updateUser());
        vo.setUpdateTime(view.updateTime());
        vo.setRemark(view.remark());
        vo.setName(view.name());
        vo.setAreaId(view.areaId());
        vo.setCode(view.code());
        vo.setLevelId(view.levelId());
        vo.setAlarmConfigId(view.alarmConfigId());
        vo.setAlarmResourceType(view.alarmResourceType());
        vo.setAlarmResourceId(view.alarmResourceId());
        vo.setAlarmResourceName(view.alarmResourceName());
        vo.setStatus(view.status());
        vo.setAlarmSource(view.alarmSource());
        vo.setAlarmSourceId(view.alarmSourceId());
        vo.setAlarmValue(view.alarmValue());
        vo.setReAlarmSource(view.reAlarmSource());
        vo.setReAlarmSourceId(view.reAlarmSourceId());
        vo.setReAlarmValue(view.reAlarmValue());
        vo.setConfirmStatus(view.confirmStatus());
        vo.setConfirmUser(view.confirmUser());
        vo.setConfirmUserId(view.confirmUserId());
        vo.setConfirmTime(view.confirmTime());
        vo.setReason(view.reason());
        vo.setSuggestion(view.suggestion());
        vo.setReAlarmTime(view.reAlarmTime());
        vo.setAlarmResourceDetailsType(view.alarmResourceDetailsType());
        vo.setAlarmResourceDetailsName(view.alarmResourceDetailsName());
        vo.setAlarmResourceDetailsId(view.alarmResourceDetailsId());
        vo.setAlarmResourceDetailsIndex(view.alarmResourceDetailsIndex());
        vo.setAlarmType(view.alarmType());
        return vo;
    }
}
