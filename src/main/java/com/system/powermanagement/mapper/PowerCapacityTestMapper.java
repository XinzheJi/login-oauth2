package com.system.powermanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.powermanagement.domain.entity.PowerCapacityTest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 电源远程核容测试Mapper接口
 */
@Mapper
public interface PowerCapacityTestMapper extends BaseMapper<PowerCapacityTest> {
    
    /**
     * 查询设备的历史核容记录
     * @param deviceId 设备ID
     * @return 核容记录列表
     */
    List<PowerCapacityTest> selectHistoryByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 查询最近的核容记录
     * @param deviceId 设备ID
     * @return 核容记录
     */
    PowerCapacityTest selectLatestByDeviceId(@Param("deviceId") Long deviceId);
} 