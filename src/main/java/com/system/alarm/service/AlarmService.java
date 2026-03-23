package com.system.alarm.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史告警展示服务接口，提供基于历史告警表的查询能力。
 */
public interface AlarmService {

    /**
     * 分页查询历史告警记录。
     *
     * @param query 查询条件
     * @return 历史告警分页结果
     */
    HistoryAlarmPage listHistoryAlarms(HistoryAlarmQuery query);

    /**
     * 根据ID获取历史告警详情。
     *
     * @param id 告警ID
     * @return 历史告警详情，可能为空
     */
    HistoryAlarmView getHistoryAlarm(Long id);

    /**
     * 历史告警查询条件。
     *
     * @param areaId                区域ID
     * @param alarmResourceId       告警对象ID
     * @param alarmResourceName     告警对象名称（模糊匹配）
     * @param statusList            告警状态集合
     * @param levelIds              告警等级ID集合
     * @param alarmResourceType     告警对象类型
     * @param alarmSource           告警来源
     * @param confirmStatus         确认状态
     * @param alarmType             告警类型
     * @param alarmStartTime        告警开始时间（创建时间）起始
     * @param alarmEndTime          告警开始时间（创建时间）结束
     * @param recoverStartTime      恢复时间起始
     * @param recoverEndTime        恢复时间结束
     * @param pageNum               页码，从1开始
     * @param pageSize              每页数量，默认10，最大200
     */
    record HistoryAlarmQuery(
            Long areaId,
            Long alarmResourceId,
            String alarmResourceName,
            List<Integer> statusList,
            List<Long> levelIds,
            String alarmResourceType,
            String alarmSource,
            String confirmStatus,
            String alarmType,
            LocalDateTime alarmStartTime,
            LocalDateTime alarmEndTime,
            LocalDateTime recoverStartTime,
            LocalDateTime recoverEndTime,
            int pageNum,
            int pageSize
    ) {
        public HistoryAlarmQuery {
            statusList = statusList == null ? List.of() : List.copyOf(statusList);
            levelIds = levelIds == null ? List.of() : List.copyOf(levelIds);
            pageNum = pageNum < 1 ? 1 : pageNum;
            if (pageSize < 1) {
                pageSize = 10;
            } else if (pageSize > 200) {
                pageSize = 200;
            }
        }
    }

    /**
     * 历史告警分页信息。
     *
     * @param total    告警总数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param records  当前页数据
     */
    record HistoryAlarmPage(
            long total,
            int pageNum,
            int pageSize,
            List<HistoryAlarmView> records
    ) {
        /**
         * 计算总页数，避免零除。
         */
        public long pages() {
            if (pageSize <= 0) {
                return 0L;
            }
            return (long) Math.ceil((double) total / (double) pageSize);
        }
    }

    /**
     * 历史告警展示信息，对应历史告警表字段。
     *
     * @param id                          主键
     * @param createUser                  创建人
     * @param createTime                  创建时间
     * @param updateUser                  修改人
     * @param updateTime                  修改时间
     * @param remark                      备注
     * @param name                        告警名称
     * @param areaId                      区域ID
     * @param code                        告警编码
     * @param levelId                     告警等级ID
     * @param alarmConfigId               告警配置ID
     * @param alarmResourceType           告警对象类型
     * @param alarmResourceId             告警对象ID
     * @param alarmResourceName           告警对象名称
     * @param status                      告警状态
     * @param alarmSource                 告警来源
     * @param alarmSourceId               告警来源ID
     * @param alarmValue                  告警值
     * @param reAlarmSource               恢复来源
     * @param reAlarmSourceId             恢复来源ID
     * @param reAlarmValue                恢复值
     * @param confirmStatus               确认状态
     * @param confirmUser                 确认人
     * @param confirmUserId               确认人ID
     * @param confirmTime                 确认时间
     * @param reason                      可能原因
     * @param suggestion                  处理建议
     * @param reAlarmTime                 恢复时间
     * @param alarmResourceDetailsType    告警详细对象类型
     * @param alarmResourceDetailsName    告警详细对象名称
     * @param alarmResourceDetailsId      告警详细对象ID
     * @param alarmResourceDetailsIndex   告警详细对象索引
     * @param alarmType                   告警类型
     */
    record HistoryAlarmView(
            Long id,
            Long createUser,
            LocalDateTime createTime,
            Long updateUser,
            LocalDateTime updateTime,
            String remark,
            String name,
            Long areaId,
            String code,
            Long levelId,
            Long alarmConfigId,
            String alarmResourceType,
            Long alarmResourceId,
            String alarmResourceName,
            Integer status,
            String alarmSource,
            Long alarmSourceId,
            String alarmValue,
            String reAlarmSource,
            Long reAlarmSourceId,
            String reAlarmValue,
            String confirmStatus,
            String confirmUser,
            Long confirmUserId,
            LocalDateTime confirmTime,
            String reason,
            String suggestion,
            LocalDateTime reAlarmTime,
            String alarmResourceDetailsType,
            String alarmResourceDetailsName,
            Long alarmResourceDetailsId,
            String alarmResourceDetailsIndex,
            String alarmType
    ) {
    }
}
