package com.system.login.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.system.login.domain.entity.Role;
import com.system.login.domain.entity.Tenant;
import com.system.login.domain.entity.User;
import com.system.login.domain.vo.LoginRequest;
import com.system.login.domain.vo.LoginResponse;
import com.system.login.mapper.UserMapper;
import com.system.login.config.JwtConfig;
import com.system.login.security.cache.UserDetailsCacheHelper;
import com.system.login.security.tenant.TenantContext;
import com.system.login.service.security.JwtTokenService;
import com.system.login.service.tenant.TenantService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 * 支持多租户环境下的用户管理
 */
@Slf4j
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TenantService tenantService;
    private final JwtConfig jwtConfig;
    private final UserDetailsCacheHelper userDetailsCacheHelper;

    @Autowired
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, RoleService roleService,
                       AuthenticationManager authenticationManager, JwtTokenService jwtTokenService,
                       TenantService tenantService, JwtConfig jwtConfig,
                       UserDetailsCacheHelper userDetailsCacheHelper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.tenantService = tenantService;
        this.jwtConfig = jwtConfig;
        this.userDetailsCacheHelper = userDetailsCacheHelper;
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应结果
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());

        // 调用Spring Security进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 认证通过，设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户详情
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 获取用户信息
        User user = userMapper.findByUsername(userDetails.getUsername());

        if (!StringUtils.hasText(user.getPassword())) {
            log.warn("UserService.login: 用户 {} 在数据库中的密码为空", user.getUsername());
        }

        // 获取业务租户ID
        String businessTenantId = null;
        if (user.getTenantId() != null) {
            businessTenantId = tenantService.getBusinessTenantIdByPrimaryKey(user.getTenantId());
        }
        if (businessTenantId == null) {
            log.error("无法确定用户 {} 的业务租户ID，登录失败", user.getUsername());
            throw new RuntimeException("用户租户信息配置错误");
        }


        // 生成JWT令牌
        String jwt = jwtTokenService.generateToken(user, userDetails, businessTenantId);
        long expiresIn = jwtConfig.getExpiration();
        long expiresAt = System.currentTimeMillis() + expiresIn * 1000;

//        System.out.println("JWT令牌: " + jwt);

        // 返回登录响应
        return LoginResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .username(user.getUsername())
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .loginMethod("local")
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 创建新用户
     * 自动处理密码加密和租户ID设置
     * 自动分配普通用户角色
     */
    @Override
    public boolean save(User user) {
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置租户ID (主键ID)
        if (user.getTenantId() == null) {
            String businessTenantId = TenantContext.getCurrentTenant();
            if (businessTenantId != null) {
                Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
                if (tenant != null) {
                    user.setTenantId(tenant.getId()); // 设置为租户表的主键ID
                } else {
                    log.warn("创建用户时，根据业务租户ID {} 未找到对应租户记录", businessTenantId);
                    // 根据您的业务需求，这里可以抛出异常或采取其他措施
                }
            }
        }

        // 存储name字段到用户名中（临时解决方案）
        if (user.getName() != null && !user.getName().isEmpty()) {
            // 在日志中记录用户姓名，因为数据库暂时不支持
            log.info("用户姓名 '{}' 暂时无法存储到数据库", user.getName());
        }

        // 保存用户
        boolean saved = super.save(user);
        if (saved) {
            userDetailsCacheHelper.evictUser(user);
        }

        // 如果用户创建成功，自动分配普通用户角色（角色ID为2）
        if (saved) {
            try {
                roleService.assignRoleToUser(user.getId(), 2L); // 2 是普通用户角色的ID
                log.info("为新用户 {} 自动分配普通用户角色", user.getUsername());
            } catch (Exception e) {
                log.error("为新用户分配角色失败", e);
                // 不因为角色分配失败而回滚用户创建
            }
        }

        return saved;
    }

    /**
     * 根据用户名查询用户
     */
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);

        // 如果当前有租户上下文，则按租户过滤
        String businessTenantId = TenantContext.getCurrentTenant();
        if (businessTenantId != null) {
            Tenant tenant = tenantService.getByBusinessTenantId(businessTenantId);
            if (tenant != null) {
                wrapper.eq(User::getTenantId, tenant.getId()); // 使用租户表主键ID进行过滤
            } else {
                log.warn("查询用户 {} 时，根据业务租户ID {} 未找到对应租户记录", username, businessTenantId);
                return null; // 或者抛出异常
            }
        }

        return getOne(wrapper);
    }

    /**
     * 根据租户ID查询用户列表
     */
    public List<User> getUsersByTenantId(Long primaryKeyTenantId) { // 参数名修改以明确是主键ID
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getTenantId, primaryKeyTenantId);
        return list(wrapper);
    }

    /**
     * 更新用户信息
     * 如果密码字段不为空，则进行加密处理
     */
    @Override
    public boolean updateById(User user) {
        // 如果更新了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userDetailsCacheHelper.evictUser(user);
        } else {
            // 如果没有提供密码，则不更新密码字段
            User existingUser = getById(user.getId());
            user.setPassword(existingUser.getPassword());
        }

        // 处理name字段（临时解决方案）
        if (user.getName() != null && !user.getName().isEmpty()) {
            // 在日志中记录用户姓名更新，因为数据库暂时不支持
            log.info("用户ID {} 的姓名更新为 '{}' (暂时无法存储到数据库)", user.getId(), user.getName());
        }

        return super.updateById(user);
    }

    /**
     * 为用户分配角色
     */
    public void assignRoleToUser(Long userId, Long roleId) {
        roleService.assignRoleToUser(userId, roleId);
    }

    /**
     * 移除用户角色
     */
    public void removeRoleFromUser(Long userId, Long roleId) {
        roleService.removeRoleFromUser(userId, roleId);
    }

    /**
     * 根据用户名查询用户ID
     * @param username
     * @return
     */
    public Long getUserIdByUsername(String username) {
        User user = getByUsername(username);
        if (user != null) {
            return user.getId();
        } else {
            return null;
        }
    }

    /**
     * 获取所有用户列表并填充角色信息
     */
    public List<User> listWithRoles() {
        List<User> users = list();

        // 为每个用户填充角色信息并设置默认姓名
        for (User user : users) {
            // 设置默认姓名为用户名（临时解决方案）
            user.setName(user.getUsername());

            // 填充角色信息
            List<Role> roles = roleService.getRolesByUserId(user.getId());
            user.setRoles(roles);
        }

        return users;
    }
}
