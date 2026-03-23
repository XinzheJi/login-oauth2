package com.system.equipmenttypemanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.system.equipmenttypemanagement.domain.entity.EquipmentType;
import com.system.equipmenttypemanagement.domain.vo.PageResult;
import com.system.equipmenttypemanagement.domain.vo.Result;
import com.system.equipmenttypemanagement.service.EquipmentTypeService;
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
@RequestMapping("/api/equipment-type/devices")
public class EquipmentTypeController {
    @Autowired
    private EquipmentTypeService equipmentTypeService;
    /**
     * 分页查询设备台账列表
     * @param pageNum 当前页码
     * @param pageSize 每页条数
     * @return 设备VO分页列表
     */
    @GetMapping("/page")
    public Result<PageResult<EquipmentType>> page( // 修改返回类型为Result包
                                                   @RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize,
                                                   @RequestParam(value = "isOpticalBypass", required = false) Character isOpticalBypass) {
        Page<EquipmentType> page = new Page<>(pageNum, pageSize);
        log.info("分页查询设备管理列表，当前页码：{}，每页条数：{}", pageNum, pageSize);
        PageResult<EquipmentType> resultPage = equipmentTypeService.selectDevicePage(page, isOpticalBypass);
        return Result.success(resultPage); // 使用统一响应包装
    }


}
