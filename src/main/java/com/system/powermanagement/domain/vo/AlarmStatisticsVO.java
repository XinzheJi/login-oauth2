package com.system.powermanagement.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 告警统计视图对象
 */
@Data
public class AlarmStatisticsVO {
    
    /**
     * 总告警数
     */
    private Integer totalAlarms;
    
    /**
     * 未处理告警数
     */
    private Integer unprocessedAlarms;
    
    /**
     * 已解决告警数
     */
    private Integer resolvedAlarms;
    
    /**
     * 按告警级别统计
     * key: 告警级别名称
     * value: 数量
     */
    private Map<String, Integer> alarmLevelCounts;
    
    /**
     * 按告警类型统计
     * key: 告警类型名称
     * value: 数量
     */
    private Map<String, Integer> alarmTypeCounts;
    
    /**
     * 按区域统计
     * key: 区域名称
     * value: 数量
     */
    private Map<String, Integer> alarmLocationCounts;
    
    /**
     * 按时间统计的趋势数据
     * 包含日期和对应的告警数量
     */
    private List<Map<String, Object>> alarmTrend;
} 