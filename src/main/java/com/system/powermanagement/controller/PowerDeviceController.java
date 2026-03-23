package com.system.powermanagement.controller;

import com.system.login.security.permission.RequirePermission;
import com.system.powermanagement.domain.dto.PowerDeviceDTO;
import com.system.powermanagement.domain.dto.PowerDeviceQueryDTO;
import com.system.powermanagement.domain.entity.PowerDevice;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerDeviceVO;
import com.system.powermanagement.domain.vo.Result;
import com.system.powermanagement.service.PowerDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 电源设备控制器
 * 提供电源台账管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/power/devices")
public class PowerDeviceController {
    
    private final PowerDeviceService powerDeviceService;
    
    @Autowired
    public PowerDeviceController(PowerDeviceService powerDeviceService) {
        this.powerDeviceService = powerDeviceService;
    }
    
    /**
     * 分页查询电源设备
     */
    @GetMapping
    @RequirePermission("power:device")
    public Result<PageResult<PowerDeviceVO>> listByPage(PowerDeviceQueryDTO queryDTO) {
        PageResult<PowerDeviceVO> pageResult = powerDeviceService.listByPage(queryDTO);
        return Result.success(pageResult);
    }
    
    /**
     * 根据业务ID查询电源设备
     */
    @GetMapping("/{deviceId}")
    @RequirePermission("power:device")
    public Result<PowerDeviceVO> getByDeviceId(@PathVariable String deviceId) {
        PowerDeviceVO device = powerDeviceService.getByDeviceId(deviceId);
        if (device == null) {
            return Result.error(404, "设备不存在，deviceId: " + deviceId);
        }
        return Result.success(device);
    }
    
    /**
     * 创建电源设备
     */
    @PostMapping
    @RequirePermission("power:device")
    public Result<PowerDeviceVO> create(@RequestBody PowerDeviceDTO deviceDTO) {
        // 检查IP地址是否已存在 (getByIpAddress现在在租户内检查)
        PowerDevice existingByIp = powerDeviceService.getByIpAddress(deviceDTO.getIpAddress());
        if (existingByIp != null) {
            return Result.error(400, "IP地址 " + deviceDTO.getIpAddress() + " 已存在于当前租户");
        }
        
        try {
            PowerDeviceVO createdDevice = powerDeviceService.createDevice(deviceDTO);
            return Result.success(createdDevice);
        } catch (RuntimeException e) {
            log.error("创建设备失败: ", e);
            return Result.error("创建设备失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据业务ID更新电源设备
     */
    @PutMapping("/{deviceId}")
    @RequirePermission("power:device")
    public Result<PowerDeviceVO> update(@PathVariable String deviceId, @RequestBody PowerDeviceDTO deviceDTO) {
        PowerDeviceVO existingDevice = powerDeviceService.getByDeviceId(deviceId);
        if (existingDevice == null) {
            return Result.error(404, "设备不存在，deviceId: " + deviceId);
        }
        
        // 检查IP地址是否已被其他设备使用 (在当前租户内)
        if (deviceDTO.getIpAddress() != null && !deviceDTO.getIpAddress().equals(existingDevice.getIpAddress())) {
            PowerDevice deviceWithNewIp = powerDeviceService.getByIpAddress(deviceDTO.getIpAddress());
            if (deviceWithNewIp != null) {
                return Result.error(400, "IP地址 " + deviceDTO.getIpAddress() + " 已被其他设备使用于当前租户");
            }
        }
        
        try {
            PowerDeviceVO updatedDevice = powerDeviceService.updateDevice(deviceId, deviceDTO);
            return Result.success(updatedDevice);
        } catch (RuntimeException e) {
            log.error("更新设备 {} 失败: ", deviceId, e);
            return Result.error("更新设备失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据业务ID删除电源设备
     */
    @DeleteMapping("/{deviceId}")
    @RequirePermission("power:device")
    public Result<Boolean> delete(@PathVariable String deviceId) {
        try {
            boolean success = powerDeviceService.deleteDevice(deviceId);
            if (success) {
                return Result.success(true);
            } else {
                // deleteDevice内部如果找不到设备会返回false，或者权限问题抛异常
                return Result.error(404, "设备不存在或删除失败，deviceId: " + deviceId);
            }
        } catch (RuntimeException e) {
            log.error("删除设备 {} 失败: ", deviceId, e);
            return Result.error("删除设备失败: " + e.getMessage());
        }
    }
} 