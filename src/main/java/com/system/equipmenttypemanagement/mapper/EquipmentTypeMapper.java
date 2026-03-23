package com.system.equipmenttypemanagement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.equipmenttypemanagement.domain.entity.EquipmentType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备数据访问层接口
 */
@Mapper
public interface EquipmentTypeMapper extends BaseMapper<EquipmentType> {

    /**
     * 分页条件查询设备台账列表
     *
     * @param page            分页参数
     * @param isOpticalBypass
     * @return 设备VO分页列表
     */

    IPage<EquipmentType> selectDevicePage(Page<?> page, Character isOpticalBypass);
}
