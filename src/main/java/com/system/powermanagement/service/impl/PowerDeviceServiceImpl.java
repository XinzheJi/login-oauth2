package com.system.powermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.login.domain.entity.Tenant;
import com.system.login.security.tenant.TenantContext;
import com.system.powermanagement.domain.dto.PowerDeviceDTO;
import com.system.powermanagement.domain.dto.PowerDeviceQueryDTO;
import com.system.powermanagement.domain.entity.PowerDevice;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerDeviceVO;
import com.system.powermanagement.mapper.PowerDeviceMapper;
import com.system.powermanagement.service.PowerDeviceService;
import com.system.login.service.tenant.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 电源设备服务实现类
 */
@Slf4j
@Service
public class PowerDeviceServiceImpl implements PowerDeviceService {
    
    private final PowerDeviceMapper powerDeviceMapper;
    private final TenantService tenantService;
    
    @Autowired
    public PowerDeviceServiceImpl(PowerDeviceMapper powerDeviceMapper, TenantService tenantService) {
        this.powerDeviceMapper = powerDeviceMapper;
        this.tenantService = tenantService;
    }
    
    @Override
    public PageResult<PowerDeviceVO> listByPage(PowerDeviceQueryDTO queryDTO) {
        Page<PowerDeviceVO> pageVo = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Long primaryKeyTenantId = getPrimaryKeyTenantId();
        
        IPage<PowerDeviceVO> resultPageVO = powerDeviceMapper.selectPageWithTenant(
                pageVo,
                queryDTO.getDeviceName(),
                queryDTO.getIpAddress(),
                queryDTO.getLocation(),
                queryDTO.getDeviceType(),
                primaryKeyTenantId
        );
        
        return new PageResult<>(
                resultPageVO.getCurrent(),
                resultPageVO.getSize(),
                resultPageVO.getTotal(),
                resultPageVO.getRecords()
        );
    }
    
    @Override
    public PowerDeviceVO getByDeviceId(String deviceId) {
        PowerDevice device = powerDeviceMapper.selectOne(new LambdaQueryWrapper<PowerDevice>().eq(PowerDevice::getDeviceId, deviceId));
        if (device == null) {
            return null;
        }
        checkTenantAccess(device.getTenantId(), "查看", deviceId);
        return convertToVO(device);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PowerDeviceVO createDevice(PowerDeviceDTO deviceDTO) {
        PowerDevice device = new PowerDevice();
        BeanUtils.copyProperties(deviceDTO, device);
        
        device.setDeviceId(UUID.randomUUID().toString());
        
        Long primaryKeyTenantId = getPrimaryKeyTenantIdForModification("创建");
        device.setTenantId(primaryKeyTenantId);
        
        LocalDateTime now = LocalDateTime.now();
        device.setCreateTime(now);
        device.setUpdateTime(now);
        
        powerDeviceMapper.insert(device);
        return convertToVO(device);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PowerDeviceVO updateDevice(String deviceId, PowerDeviceDTO deviceDTO) {
        PowerDevice existingDevice = powerDeviceMapper.selectOne(new LambdaQueryWrapper<PowerDevice>().eq(PowerDevice::getDeviceId, deviceId));
        if (existingDevice == null) {
            log.warn("尝试更新设备失败：未找到 deviceId 为 {} 的设备", deviceId);
            throw new RuntimeException("未找到要更新的设备，deviceId: " + deviceId);
        }
        
        checkTenantAccess(existingDevice.getTenantId(), "更新", deviceId);
        
        BeanUtils.copyProperties(deviceDTO, existingDevice, "id", "deviceId", "createTime", "tenantId");
        existingDevice.setUpdateTime(LocalDateTime.now());
        
        powerDeviceMapper.updateById(existingDevice);
        return convertToVO(existingDevice);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDevice(String deviceId) {
        PowerDevice device = powerDeviceMapper.selectOne(new LambdaQueryWrapper<PowerDevice>().eq(PowerDevice::getDeviceId, deviceId));
        if (device == null) {
            log.warn("尝试删除设备：未找到 deviceId 为 {} 的设备", deviceId);
            return false;
        }
        checkTenantAccess(device.getTenantId(), "删除", deviceId);
        return powerDeviceMapper.deleteById(device.getId()) > 0;
    }
    
    @Override
    public PowerDevice getByIpAddress(String ipAddress) {
        LambdaQueryWrapper<PowerDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PowerDevice::getIpAddress, ipAddress);
        
        Long primaryKeyTenantId = getPrimaryKeyTenantId();
        if (primaryKeyTenantId != null) {
            wrapper.eq(PowerDevice::getTenantId, primaryKeyTenantId);
        }
        PowerDevice device = powerDeviceMapper.selectOne(wrapper);
        return device;
    }
    
    private PowerDeviceVO convertToVO(PowerDevice device) {
        if (device == null) return null;
        PowerDeviceVO vo = new PowerDeviceVO();
        BeanUtils.copyProperties(device, vo);
        
        if (device.getTenantId() != null) {
            Tenant tenantEntity = tenantService.getById(device.getTenantId());
            if (tenantEntity != null) {
                vo.setTenantName(tenantEntity.getTenantName());
            }
        }
        return vo;
    }
    
    private Long getPrimaryKeyTenantId() {
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                return tenant.getId();
            }
            log.warn("根据业务租户ID {} 未找到对应租户记录", businessTenantId);
        }
        return null;
    }
    
    private Long getPrimaryKeyTenantIdForModification(String operationType) {
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId == null) {
            log.error("{}电源设备时，TenantContext中无业务租户ID", operationType);
            throw new RuntimeException("无法确定当前操作的租户信息");
        }
        Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
        if (tenant == null) {
            log.error("{}电源设备时，根据业务租户ID {} 未找到对应租户记录", operationType, businessTenantId);
            throw new RuntimeException("无法确定当前操作的租户信息，无效的租户标识: " + businessTenantId);
        }
        return tenant.getId();
    }
    
    private void checkTenantAccess(Long deviceTenantId, String operationType, String deviceId) {
        String currentBusinessTenantId = TenantContext.getCurrentTenant();
        if (currentBusinessTenantId != null) {
            Tenant currentTenant = tenantService.getByBusinessTenantId(currentBusinessTenantId);
            if (currentTenant == null || !deviceTenantId.equals(currentTenant.getId())) {
                log.warn("无权{}其他租户的设备数据，设备业务ID: {}, 设备所属租户主键ID: {}, 当前业务租户ID: {}",
                        operationType, deviceId, deviceTenantId, currentBusinessTenantId);
                throw new RuntimeException("无权操作属于其他租户的设备数据");
            }
        } else {
            // 根据业务需求，决定在TenantContext为空时是否允许操作。
            // 如果系统设计为所有操作都必须在租户上下文中，这里应该抛出异常。
            // log.warn("TenantContext为空，允许{}设备 {}，但可能存在风险", operationType, deviceId);
            // 如果不允许，则：
             throw new RuntimeException("无法在无租户上下文中操作设备数据");
        }
    }
} 