package com.system.login.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.system.login.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND status = 1")
    User findByUsername(@Param("username") String username);
    
    /**
     * 根据用户名和租户ID查询用户（支持多租户）
     */
    @Select("SELECT * FROM user WHERE username = #{username} AND tenant_id = #{tenantId} AND status = 1")
    User findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") Long tenantId);
} 