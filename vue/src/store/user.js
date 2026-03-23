// store/userStore.js
import { defineStore } from 'pinia'
import { login, getUserInfo, logout, checkSSOEnabled, getSSOAuthorizeUrl, startSSOLogin, handleSSOCallback, setTokenExpiresAt, markUserActivity } from '@/api/auth'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { nextTick } from 'vue'

// 密码安全存储 - 使用SessionStorage而非LocalStorage
// SessionStorage会在页面会话结束时清除（关闭标签或浏览器）
// 这比LocalStorage更安全，因为LocalStorage会永久存储
const DEFAULT_TOKEN_TTL_MS = 24 * 60 * 60 * 1000
/**
 * 保存认证令牌
 * @param {String} token JWT令牌
 */
function saveAuthToken(token, expiresAt) {
  if (!token) {
    console.warn('尝试保存空token');
    return;
  }
  
  try {
    // 直接保存token，不进行任何处理，保持原始格式
    sessionStorage.setItem('token', token);
    if (expiresAt) {
      setTokenExpiresAt(expiresAt);
    }
    markUserActivity();
    
    if (process.env.NODE_ENV !== 'production') {
      console.log('令牌已保存到会话存储');
    }
  } catch (e) {
    console.error('保存token时出错:', e);
  }
}

const getAuthToken = () => {
  try {
    // 仅从sessionStorage获取
    const token = sessionStorage.getItem('token');
    // 清理旧版本localStorage残留
    try {
      if (localStorage.getItem('token')) {
        localStorage.removeItem('token');
      }
    } catch (e) {
      console.warn('无法清理旧token', e);
    }
    return token || '';
  } catch (e) {
    console.error('获取授权令牌失败');
    return '';
  }
};

const clearAuthToken = () => {
  try {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('token_expires_at');
    sessionStorage.removeItem('last_activity_ts');
    sessionStorage.removeItem('token_remaining_seconds');
    // 清理旧版本遗留
    try { localStorage.removeItem('token'); localStorage.removeItem('user'); } catch (e) {}
  } catch (e) {
    console.error('清除授权令牌失败');
  }
};

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getAuthToken(),
    userInfo: JSON.parse(sessionStorage.getItem('user') || '{}'),
    ssoEnabled: false,
    ssoProvider: ''
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo.username || '',
    roles: (state) => state.userInfo.roles || [],
    permissions: (state) => {
      // 确保权限都是小写格式，以便前后端一致
      if (!state.userInfo.permissions) return [];
      return state.userInfo.permissions.map(p => typeof p === 'string' ? p.toLowerCase() : p);
    }
  },
  
  actions: {
    // 登录
    async loginAction(username, password) {
      this.resetState();
      try {
        const loginResponse = await login(username, password);
        if (!loginResponse || !loginResponse.token) {
          throw new Error('登录失败：服务器未返回有效令牌');
        }
        const token = loginResponse.token;
        const expiresAt = loginResponse.expiresAt || (loginResponse.expiresIn ? Date.now() + Number(loginResponse.expiresIn) * 1000 : Date.now() + DEFAULT_TOKEN_TTL_MS);
        if (process.env.NODE_ENV !== 'production') {
          console.log(`登录成功，获取到令牌，长度: ${token.length}`);
        }
        this.token = token;
        saveAuthToken(token, expiresAt);
        try {
          console.log('准备获取用户信息...');
          await this.getUserInfoAction();
          if (process.env.NODE_ENV !== 'production') {
            console.log('loginAction: 成功获取用户信息');
          }
        } catch (userInfoError) {
          console.warn('loginAction: 无法获取用户信息:', userInfoError.message);
          // 构建基本用户信息
          const basicUserInfo = {
            username: username,
            name: username,
            avatar: '',
            roles: username.toLowerCase() === 'admin' ? ['ADMIN', 'user'] : ['user'],
            permissions: username.toLowerCase() === 'admin' ? [
              // 用户管理权限
              'user:view', 'user:create', 'user:update', 'user:delete',
              // 角色管理权限
              'role:view', 'role:create', 'role:update', 'role:delete',
              // 权限管理
              'permission:view', 'permission:create', 'permission:update', 'permission:delete', 
              'permission:assign',
              // 租户管理
              'tenant:view', 'tenant:create', 'tenant:update', 'tenant:delete',
              // 电力设备管理
              'power:device', 'power:alarm', 'power:alarm:statistics',
              // 通配符权限（最高权限）
              '*'
            ] : ['user:view']
          };
          
          // 保存到state
          this.userInfo = basicUserInfo;
          
          // 保存到sessionStorage
          try {
            const userInfoStr = JSON.stringify(basicUserInfo);
            sessionStorage.setItem('user', userInfoStr);
            console.log('loginAction: 已保存基本用户信息到本地存储');
          } catch (err) {
            console.error('loginAction: 保存基本用户信息到本地存储失败:', err);
          }
        }
        this.token = token; // Ensure token is set on state
        console.log('loginAction: Token已设置, 准备跳转. Current token state:', this.token ? 'Token Set' : 'Token NOT Set');
        console.log('loginAction: Router instance:', router);
        console.log('loginAction: 当前路由:', router.currentRoute.value.fullPath);
        
        // 尝试直接跳转，移除 nextTick 观察效果
        // await nextTick(); 
        
        try {
          // 尝试方法1：使用 router.push
          console.log("loginAction: 尝试执行 router.push('/')...");
          await router.push("/");
          console.log("loginAction: router.push('/') 执行完毕. 当前路由:", router.currentRoute.value.fullPath);
          
          // 如果上面的跳转失败，检查一下跳转到其他路径
          if (router.currentRoute.value.fullPath === "/login") {
            console.log("loginAction: 首页跳转失败，尝试跳转到其他路径...");
            
            // 尝试方法2：使用 router.replace
            await router.replace("/");
            console.log("loginAction: router.replace('/') 执行完毕. 当前路由:", router.currentRoute.value.fullPath);
            
            // 尝试方法3：使用 window.location
            if (router.currentRoute.value.fullPath === "/login") {
              console.log("loginAction: router.replace也失败了，尝试使用window.location...");
              // 使用window.location直接改变URL（浏览器级别的跳转）
              // 注意：这会导致页面刷新
              window.location.href = "/#/";
            }
          }
        } catch (navigationError) {
          console.error("loginAction: 路由跳转出错:", navigationError);
        }
        
        ElMessage.success('登录成功'); 
        console.log('loginAction: ElMessage 登录成功消息已发送.');
        return true;
      } catch (error) {
        this.resetState();
        ElMessage.error(error.message || '登录失败，请重试');
        console.error('loginAction: 登录操作失败:', error);
        return false;
      }
    },
    
    // 获取用户信息
    async getUserInfoAction() {
      try {
        if (process.env.NODE_ENV !== 'production') {
          console.log('正在获取用户信息...');
        }
        
        // 先检查是否有token
        const token = getAuthToken();
        if (!token) {
          throw new Error('未找到授权令牌，请先登录');
        }
        
        // 获取用户信息
        const userInfo = await getUserInfo();
        
        if (!userInfo) {
          throw new Error('获取用户信息失败：服务器返回空数据');
        }
        
        // 使用后端返回的基本信息，补充完整必要的前端权限字段
        const enhancedUserInfo = {
          ...userInfo,
          roles: userInfo.roles || [],
          permissions: userInfo.permissions || []
        };
        
        // 如果是admin用户，且权限为空，则添加默认管理员权限
        if (enhancedUserInfo.username === 'admin' && (!enhancedUserInfo.permissions || enhancedUserInfo.permissions.length === 0)) {
          console.log('检测到管理员用户，添加完整权限集');
          // 添加管理员角色
          if (!enhancedUserInfo.roles.includes('ADMIN')) {
            enhancedUserInfo.roles.push('ADMIN');
          }
          
          // 添加常用权限
          enhancedUserInfo.permissions = [
            // 用户管理权限
            'user:view', 'user:create', 'user:update', 'user:delete', 'user:assign-role', 'user:remove-role',
            // 角色管理权限
            'role:view', 'role:create', 'role:update', 'role:delete',
            // 权限管理
            'permission:view', 'permission:create', 'permission:update', 'permission:delete', 'permission:assign', 'permission:remove',
            // 租户管理
            'tenant:view', 'tenant:create', 'tenant:update', 'tenant:delete',
            // 电力设备管理
            'power:device', 'power:alarm', 'power:alarm:statistics',
            // 通配符权限（最高权限）
            '*'
          ];
        }
        
        // 规范化用户权限字段 - 确保都是小写格式
        if (enhancedUserInfo.permissions && Array.isArray(enhancedUserInfo.permissions)) {
          enhancedUserInfo.permissions = enhancedUserInfo.permissions.map(p => 
            typeof p === 'string' ? p.toLowerCase() : p);
        } else {
          enhancedUserInfo.permissions = [];
        }
        
        // 规范化用户角色字段 - 至少有一个默认角色
        if (!enhancedUserInfo.roles || !Array.isArray(enhancedUserInfo.roles) || enhancedUserInfo.roles.length === 0) {
          enhancedUserInfo.roles = ['user'];
        }
        
        // 保存增强后的用户信息到 state
        this.userInfo = enhancedUserInfo;
        
        // 保存到 sessionStorage，确保路由守卫能找到
        try {
          const userInfoStr = JSON.stringify(enhancedUserInfo);
          sessionStorage.setItem('user', userInfoStr);
          
          if (process.env.NODE_ENV !== 'production') {
            console.log('增强后的用户信息已保存到本地存储:', JSON.stringify({
              username: enhancedUserInfo.username,
              roles: enhancedUserInfo.roles,
              permissionsCount: enhancedUserInfo.permissions ? enhancedUserInfo.permissions.length : 0
            }));
          }
        } catch (err) {
          console.error('保存用户信息到本地存储失败:', err);
        }
        
        if (process.env.NODE_ENV !== 'production') {
          console.log('用户信息获取成功');
        }
        
        return enhancedUserInfo;
      } catch (error) {
        console.error('获取用户信息失败:', error.message);
        throw error;
      }
    },
    
    // 检查是否有权限
    hasPermission(permission) {
      if (!permission) return true
      
      const permissions = this.permissions
      
      // 规范化权限字符串为小写
      const requiredPermission = typeof permission === 'string' 
        ? permission.toLowerCase() 
        : permission
      
      // 检查是否有指定权限
      const hasPermission = permissions.some(p => {
        // 如果权限包含通配符'*'，则认为拥有所有权限
        if (p === '*') return true
        // 直接比较权限字符串
        return p === requiredPermission
      })
      
      return hasPermission
    },
    
    // 退出登录
    async logoutAction() {
      try {
        // 无论后端接口是否成功，都清除本地状态
        await logout().catch(error => {
          console.warn('退出登录请求发生错误，但仍将清除本地登录状态:', error.message)
        })
        
        this.resetState()
        router.push('/login')
        return true
      } catch (error) {
        // 即使发生错误，也要重置状态
        console.warn('退出登录失败，但仍将清除本地登录状态:', error.message)
        this.resetState()
        router.push('/login')
        // 返回成功而不是抛出错误
        return true
      }
    },
    
  // 重置状态
  resetState() {
    this.token = ''
    this.userInfo = { roles: [], permissions: [] }
    clearAuthToken()
    sessionStorage.removeItem('user')
  },

    // SSO相关方法
    
    // 检查SSO是否启用
    async checkSSOEnabledAction() {
      try {
        const response = await checkSSOEnabled()
        this.ssoEnabled = response.enabled || false
        this.ssoProvider = response.provider || ''
        return response
      } catch (error) {
        console.error('检查SSO状态失败:', error)
        this.ssoEnabled = false
        return { enabled: false }
      }
    },

    // 启动SSO登录
    async startSSOLoginAction() {
      try {
        console.log('启动SSO登录...')
        // 直接重定向到SSO登录端点
        startSSOLogin()
      } catch (error) {
        console.error('启动SSO登录失败:', error)
        ElMessage.error('SSO登录失败: ' + error.message)
        throw error
      }
    },

    // 处理SSO回调
    async handleSSOCallbackAction(code, state) {
      try {
        console.log('处理SSO回调:', { code, state })
        
        // 调用后端处理回调
        const response = await handleSSOCallback(code, state)
        const expiresAt = response.expiresAt || (response.expiresIn ? Date.now() + Number(response.expiresIn) * 1000 : Date.now() + DEFAULT_TOKEN_TTL_MS)
        
        // 保存登录信息
        this.token = response.token
        saveAuthToken(response.token, expiresAt)
        
        // 获取用户信息
        await this.getUserInfoAction()
        
        console.log('SSO登录成功')
        ElMessage.success('SSO登录成功')
        
        // 跳转到首页
        await nextTick()
        await router.push('/')
        
        return response
      } catch (error) {
        console.error('处理SSO回调失败:', error)
        ElMessage.error('SSO登录失败: ' + error.message)
        this.resetState()
        throw error
      }
    },

    // 检查URL参数中的SSO回调信息
    async checkSSOCallbackFromUrl() {
      const urlParams = new URLSearchParams(window.location.search)
      const hash = window.location.hash
      
      // 检查URL参数
      const code = urlParams.get('code')
      const state = urlParams.get('state')
      const error = urlParams.get('error')
      const token = urlParams.get('token')
      const method = urlParams.get('method')
      
      // 检查hash参数（针对重定向回来的情况）
      let hashParams = new URLSearchParams() // 初始化为空的URLSearchParams实例
      if (hash.includes('?')) {
        const hashQuery = hash.split('?')[1]
        hashParams = new URLSearchParams(hashQuery)
      }
      
      const hashToken = hashParams.get('token')
      const hashMethod = hashParams.get('method')
      const hashError = hashParams.get('error')
      
      // 如果有错误信息
      if (error || hashError) {
        const errorMsg = urlParams.get('error_description') || hashParams.get('error_description') || '登录失败'
        ElMessage.error('登录失败: ' + errorMsg)
        return false
      }
      
      // 如果有token（重定向方式的SSO回调）
      if (token || hashToken) {
        const finalToken = token || hashToken
        const loginMethod = method || hashMethod
        const expiresAt = Date.now() + DEFAULT_TOKEN_TTL_MS
        
        try {
          this.token = finalToken
          saveAuthToken(finalToken, expiresAt)
          
          await this.getUserInfoAction()
          
          ElMessage.success('SSO登录成功')
          console.log('SSO登录成功，登录方式:', loginMethod)
          
          // 清理URL参数
          window.history.replaceState({}, document.title, window.location.pathname + window.location.hash.split('?')[0])
          
          await nextTick()
          await router.push('/')
          return true
        } catch (error) {
          console.error('SSO token处理失败:', error)
          ElMessage.error('登录失败: ' + error.message)
          this.resetState()
          return false
        }
      }
      
      // 如果有授权码（传统OAuth2回调）
      if (code && state) {
        try {
          await this.handleSSOCallbackAction(code, state)
          // 清理URL参数
          window.history.replaceState({}, document.title, window.location.pathname)
          return true
        } catch (error) {
          return false
        }
      }
      
      return false
    }
  }
})
