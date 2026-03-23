package com.system.trsmanagement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.login.security.permission.RequirePermission;
import com.system.trsmanagement.domain.entity.TrsDevice;
import com.system.trsmanagement.domain.vo.PageResult;
import com.system.trsmanagement.domain.vo.Result;
import com.system.trsmanagement.domain.vo.TrsDeviceVO;
import com.system.trsmanagement.service.TrsDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备控制器
 * 提供设备台账管理相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/trs/devices")
public class TrsDeviceController {
    @Autowired
    private TrsDeviceService trsDeviceService;
    /**
     * 分页查询设备台账列表
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @param isOpticalBypass 光旁路标识
     * @return 设备VO分页列表
     */
    @GetMapping("/page")
    public Result<PageResult<TrsDevice>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(value = "isOpticalBypass", required = false) Character isOpticalBypass) {
        Page<TrsDevice> page = new Page<>(pageNum, pageSize);
        log.info("分页查询设备台账列表，当前页码：{}，每页条数：{}，光旁路标识：{}", pageNum, pageSize, isOpticalBypass);
        PageResult<TrsDevice> resultPage = trsDeviceService.selectDevicePage(page, isOpticalBypass);
        return Result.success(resultPage);
    }


}
