package com.system.powermanagement.domain.dto;

import lombok.Data;

/**
 * 告警处理DTO
 */
@Data
public class AlarmProcessDTO {
    
    /**
     * 告警ID
     */
    private Long alarmId;
    
    /**
     * 处理结果描述
     */
    private String processDescription;
    
    /**
     * 处理方式
     * 例如："确认"、"忽略"、"修复"
     */
    private String processMethod;
    
    /**
     * 是否已解决
     */
    private Boolean resolved;
    
    /**
     * 处理人
     */
    private String processBy;
    
    /**
     * 备注
     */
    private String remark;
} 