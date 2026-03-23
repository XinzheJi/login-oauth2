package com.system.trsmanagement.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.trsmanagement.domain.entity.TrsDevice;
import com.system.trsmanagement.domain.vo.PageResult;
import com.system.trsmanagement.domain.vo.TrsDeviceVO;
import com.system.trsmanagement.mapper.TrsDeviceMapper;
import com.system.trsmanagement.service.TrsDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrsDeviceServiceImpl implements TrsDeviceService {

    @Autowired
    private TrsDeviceMapper trsDeviceMapper;


    @Override
    public PageResult<TrsDevice> selectDevicePage(Page<TrsDevice> page, Character isOpticalBypass) {
        IPage<TrsDevice> deviceIPage = trsDeviceMapper.selectDevicePage(page, isOpticalBypass);
        PageResult<TrsDevice> pageResult = new PageResult<>(
                deviceIPage.getCurrent(),
                deviceIPage.getSize(),
                deviceIPage.getTotal(),
                deviceIPage.getRecords()
        );

        return pageResult;
    }
}