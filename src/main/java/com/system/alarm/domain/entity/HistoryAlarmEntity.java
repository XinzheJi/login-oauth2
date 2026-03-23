package com.system.alarm.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史告警数据实体，对应历史告警表。
 */
@Data
public class HistoryAlarmEntity {

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
    private Long tenantId;
}

