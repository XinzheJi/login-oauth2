package com.system.equipmenttypemanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.equipmenttypemanagement.domain.entity.EquipmentType;
import com.system.equipmenttypemanagement.domain.vo.PageResult;

public interface EquipmentTypeService {
    /**
     * 分页查询设备台账列表
     *
     * @param page            分页参数
     * @param isOpticalBypass
     * @return 设备VO分页列表
     */
    PageResult<EquipmentType> selectDevicePage(Page<EquipmentType> page, Character isOpticalBypass);
}