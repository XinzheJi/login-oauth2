package com.system.trsmanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.powermanagement.domain.vo.PowerDeviceVO;
import com.system.trsmanagement.domain.entity.TrsDevice;
import com.system.trsmanagement.domain.vo.TrsDeviceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备数据访问层接口
 */
@Mapper
public interface TrsDeviceMapper extends BaseMapper<TrsDevice> {
    
    /**
     * 分页条件查询设备台账列表
     * @param page 分页参数
     * @param isOpticalBypass 是否光学旁路
     * @return 设备VO分页列表
     */
    IPage<TrsDevice> selectDevicePage(Page<?> page, @Param("isOpticalBypass") Character isOpticalBypass);
}
