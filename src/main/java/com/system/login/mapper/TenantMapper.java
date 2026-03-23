package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 租户Mapper接口
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
    
    /**
     * 根据租户编码查询租户
     */
    @Select("SELECT * FROM tenant WHERE tenant_code = #{tenantCode}")
    Tenant findByTenantCode(@Param("tenantCode") String tenantCode);
}