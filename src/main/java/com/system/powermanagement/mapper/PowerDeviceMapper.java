package com.system.powermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.powermanagement.domain.entity.PowerDevice;
import com.system.powermanagement.domain.vo.PowerDeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 电源设备Mapper接口
 */
@Mapper
public interface PowerDeviceMapper extends BaseMapper<PowerDevice> {
    
    /**
     * 分页查询电源设备（包含租户名称）
     * @param page 分页参数
     * @param deviceName 设备名称
     * @param ipAddress IP地址
     * @param location 设备位置
     * @param deviceType 设备类型
     * @param tenantId 租户ID
     * @return 分页结果
     */
    IPage<PowerDeviceVO> selectPageWithTenant(
            Page<?> page,
            @Param("deviceName") String deviceName,
            @Param("ipAddress") String ipAddress,
            @Param("location") String location,
            @Param("deviceType") String deviceType,
            @Param("tenantId") Long tenantId
    );
} 