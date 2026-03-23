package com.system.powermanagement.service;

import com.system.powermanagement.domain.dto.PowerDeviceDTO;
import com.system.powermanagement.domain.dto.PowerDeviceQueryDTO;
import com.system.powermanagement.domain.entity.PowerDevice;
import com.system.powermanagement.domain.vo.PageResult;
import com.system.powermanagement.domain.vo.PowerDeviceVO;

/**
 * 电源设备服务接口
 */
public interface PowerDeviceService {
    
    /**
     * 分页查询电源设备
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<PowerDeviceVO> listByPage(PowerDeviceQueryDTO queryDTO);
    
    /**
     * 根据业务ID查询电源设备
     * @param deviceId 业务设备ID
     * @return 电源设备视图对象
     */
    PowerDeviceVO getByDeviceId(String deviceId);
    
    /**
     * 创建电源设备
     * @param deviceDTO 设备数据
     * @return 创建的设备视图对象 (包含新的deviceId)
     */
    PowerDeviceVO createDevice(PowerDeviceDTO deviceDTO);
    
    /**
     * 根据业务ID更新电源设备
     * @param deviceId 业务设备ID
     * @param deviceDTO 设备数据
     * @return 更新后的设备视图对象
     */
    PowerDeviceVO updateDevice(String deviceId, PowerDeviceDTO deviceDTO);
    
    /**
     * 根据业务ID删除电源设备
     * @param deviceId 业务设备ID
     * @return 是否成功
     */
    boolean deleteDevice(String deviceId);
    
    /**
     * 根据IP地址查询电源设备
     * @param ipAddress IP地址
     * @return 电源设备
     */
    PowerDevice getByIpAddress(String ipAddress);
} 