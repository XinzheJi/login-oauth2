package com.system.equipmenttypemanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.equipmenttypemanagement.domain.entity.EquipmentType;
import com.system.equipmenttypemanagement.domain.vo.PageResult;
import com.system.equipmenttypemanagement.mapper.EquipmentTypeMapper;
import com.system.equipmenttypemanagement.service.EquipmentTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EquipmentTypeServiceImpl implements EquipmentTypeService {

    @Autowired
    private EquipmentTypeMapper equipmentTypeMapper;
    @Override
    public PageResult<EquipmentType> selectDevicePage(Page<EquipmentType> page, Character isOpticalBypass) {
        IPage<EquipmentType> deviceIPage = equipmentTypeMapper.selectDevicePage(page, isOpticalBypass);
        PageResult<EquipmentType> pageResult = new PageResult<>(
                deviceIPage.getCurrent(),
                deviceIPage.getSize(),
                deviceIPage.getTotal(),
                deviceIPage.getRecords()
        );

        return pageResult;
    }
}