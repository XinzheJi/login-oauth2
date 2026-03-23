package com.system.login.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * OAuth2用户信息
 */
@Data
public class OAuth2UserInfo {
    
    /**
     * 用户唯一标识符（对应数据库id）
     */
    private String sub;
    
    /**
     * 用户登录账号（对应account字段）
     */
    private String username;
    
    /**
     * 用户真实姓名（对应real_name字段）
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL（对应avatar字段）
     */
    private String picture;
    
    /**
     * 手机号（对应phone字段）
     */
    private String phone;
    
    /**
     * 是否邮箱已验证
     */
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    
    /**
     * 租户ID（对应tenant_id字段）
     */
    @JsonProperty("tenant_id")
    private String tenantId;
    
    /**
     * 角色ID列表（对应role_id字段，可能是逗号分隔的字符串）
     */
    @JsonProperty("role_id")
    private String roleId;
    
    /**
     * 部门ID（对应dept_id字段）
     */
    @JsonProperty("dept_id")
    private String deptId;
    
    /**
     * 岗位ID（对应post_id字段）
     */
    @JsonProperty("post_id")  
    private String postId;
    
    /**
     * 用户状态（对应status字段）
     */
    private Integer status;
    
    /**
     * 是否为4A用户（对应is_4A字段）
     */
    @JsonProperty("is_4A")
    private String is4A;
    
    /**
     * 4A用户ID（对应_4A_id字段）
     */
    @JsonProperty("4A_id")
    private String fourAId;
    
    /**
     * 组织ID（对应org_id字段）
     */
    @JsonProperty("org_id")
    private String orgId;
    
    /**
     * 获取用户名，优先使用username，其次使用sub
     */
    public String getPreferredUsername() {
        return username != null ? username : sub;
    }
}
