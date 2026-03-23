package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 根据用户ID查询角色ID列表
     */
    @Select("SELECT role_id FROM user_role WHERE user_id = #{userId}")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询用户ID列表
     */
    @Select("SELECT user_id FROM user_role WHERE role_id = #{roleId}")
    List<Long> findUserIdsByRoleId(@Param("roleId") Long roleId);
}