package com.system.alarm.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.system.alarm.service.AlarmService;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史告警查询请求DTO。
 */
@Data
public class HistoryAlarmQueryDTO {

    private Long areaId;
    private Long alarmResourceId;
    private String alarmResourceName;
    private List<Integer> statusList;
    private List<Long> levelIds;
    private String alarmResourceType;
    private String alarmSource;
    private String confirmStatus;
    private String alarmType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmStartTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmEndTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recoverStartTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recoverEndTime;

    private Integer pageNum;
    private Integer pageSize;

    /**
     * 将DTO转换为服务层查询对象。
     *
     * @return 历史告警查询参数
     */
    public AlarmService.HistoryAlarmQuery toQuery() {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;

        List<Integer> statusValues = statusList == null ? List.of() : List.copyOf(statusList);
        List<Long> levelValues = levelIds == null ? List.of() : List.copyOf(levelIds);

        return new AlarmService.HistoryAlarmQuery(
                areaId,
                alarmResourceId,
                alarmResourceName,
                statusValues,
                levelValues,
                alarmResourceType,
                alarmSource,
                confirmStatus,
                alarmType,
                alarmStartTime,
                alarmEndTime,
                recoverStartTime,
                recoverEndTime,
                safePageNum,
                safePageSize
        );
    }

}
