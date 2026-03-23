package com.system.trsmanagement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.trsmanagement.domain.entity.TrsDevice;
import com.system.trsmanagement.domain.vo.PageResult;
import com.system.trsmanagement.domain.vo.TrsDeviceVO;

public interface TrsDeviceService {
    /**
     * 分页查询设备台账列表
     * @param page 分页参数
     * @param isOpticalBypass 是否光学旁路
     * @return 设备VO分页列表
     */
    PageResult<TrsDevice> selectDevicePage(Page<TrsDevice> page, Character isOpticalBypass);
}