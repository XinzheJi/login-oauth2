package com.system.alarm.service.impl;

import com.system.alarm.domain.entity.HistoryAlarmEntity;
import com.system.alarm.mapper.HistoryAlarmMapper;
import com.system.alarm.service.AlarmService;
import com.system.login.domain.entity.Tenant;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 历史告警服务实现。
 */
@Slf4j
@Service
public class AlarmServiceImpl implements AlarmService {

    private final HistoryAlarmMapper historyAlarmMapper;
    private final TenantService tenantService;

    @Autowired
    public AlarmServiceImpl(HistoryAlarmMapper historyAlarmMapper, TenantService tenantService) {
        this.historyAlarmMapper = historyAlarmMapper;
        this.tenantService = tenantService;
    }

    @Override
    public HistoryAlarmPage listHistoryAlarms(HistoryAlarmQuery query) {
        Objects.requireNonNull(query, "历史告警查询条件不能为空");

        int pageSize = query.pageSize();
        int pageNum = query.pageNum();
        long offset = (long) (pageNum - 1) * (long) pageSize;
        if (offset < 0) {
            offset = 0;
        }

        Long currentTenantId = resolveCurrentTenantId();
        long total = historyAlarmMapper.countHistoryAlarms(query, currentTenantId);
        if (total == 0L) {
            return new HistoryAlarmPage(0L, pageNum, pageSize, List.of());
        }

        List<HistoryAlarmEntity> entities = historyAlarmMapper.selectHistoryAlarms(query, currentTenantId, offset, pageSize);
        if (entities == null || entities.isEmpty()) {
            return new HistoryAlarmPage(total, pageNum, pageSize, List.of());
        }

        List<HistoryAlarmView> records = entities.stream()
                .map(this::convertToView)
                .toList();
        return new HistoryAlarmPage(total, pageNum, pageSize, records);
    }

    @Override
    public HistoryAlarmView getHistoryAlarm(Long id) {
        Objects.requireNonNull(id, "历史告警ID不能为空");
        Long currentTenantId = resolveCurrentTenantId();
        HistoryAlarmEntity entity = historyAlarmMapper.selectHistoryAlarmById(id, currentTenantId);
        if (entity == null) {
            return null;
        }
        return convertToView(entity);
    }

    private HistoryAlarmView convertToView(HistoryAlarmEntity entity) {
        if (entity == null) {
            return null;
        }
        return new HistoryAlarmView(
                entity.getId(),
                entity.getCreateUser(),
                entity.getCreateTime(),
                entity.getUpdateUser(),
                entity.getUpdateTime(),
                entity.getRemark(),
                entity.getName(),
                entity.getAreaId(),
                entity.getCode(),
                entity.getLevelId(),
                entity.getAlarmConfigId(),
                entity.getAlarmResourceType(),
                entity.getAlarmResourceId(),
                entity.getAlarmResourceName(),
                entity.getStatus(),
                entity.getAlarmSource(),
                entity.getAlarmSourceId(),
                entity.getAlarmValue(),
                entity.getReAlarmSource(),
                entity.getReAlarmSourceId(),
                entity.getReAlarmValue(),
                entity.getConfirmStatus(),
                entity.getConfirmUser(),
                entity.getConfirmUserId(),
                entity.getConfirmTime(),
                entity.getReason(),
                entity.getSuggestion(),
                entity.getReAlarmTime(),
                entity.getAlarmResourceDetailsType(),
                entity.getAlarmResourceDetailsName(),
                entity.getAlarmResourceDetailsId(),
                entity.getAlarmResourceDetailsIndex(),
                entity.getAlarmType()
        );
    }

    /**
     * 根据TenantContext获取当前租户主键ID。
     */
    private Long resolveCurrentTenantId() {
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId == null) {
            return null;
        }
        Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
        if (tenant == null) {
            log.warn("未找到TenantContext中业务租户ID对应的租户记录: {}", businessTenantId);
            return null;
        }
        return tenant.getId();
    }
}
