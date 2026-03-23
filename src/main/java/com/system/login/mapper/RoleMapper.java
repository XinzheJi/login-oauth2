package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据用户ID查询角色列表
     */
    @Select("SELECT r.* FROM role r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和租户ID查询角色列表（支持多租户）
     * 注意：tenant_id字段只存在于role表中，不在user_role表中
     */
    @Select("SELECT r.id, r.name, r.code FROM role r JOIN user_role ur ON r.id = ur.role_id /*tenant_id*/WHERE ur.user_id = #{userId} AND r.tenant_id = #{tenantId}")
    List<Role> findRolesByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}