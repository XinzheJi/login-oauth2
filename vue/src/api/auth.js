import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // API基础URL
  timeout: 300000, // 请求超时时间（5分钟）
})

const TOKEN_EXPIRES_AT_KEY = 'token_expires_at'
const LAST_ACTIVITY_KEY = 'last_activity_ts'
export const SESSION_TIMEOUT_MS = 5 * 60 * 1000
export const REFRESH_THRESHOLD_MS = 60 * 1000

/**
 * 脱敏处理函数，将敏感信息替换为星号
 * @param {string} str 需要脱敏的字符串
 * @param {number} prefixLength 保留前缀长度
 * @param {number} suffixLength 保留后缀长度
 * @returns {string} 脱敏后的字符串
 */
const maskSensitiveInfo = (str, prefixLength = 4, suffixLength = 4) => {
  if (!str || str.length <= prefixLength + suffixLength) {
    return '******';
  }
  const prefix = str.substring(0, prefixLength);
  const suffix = str.substring(str.length - suffixLength);
  const mask = '*'.repeat(6);
  return `${prefix}${mask}${suffix}`;
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 会话超时检查
    const lastActive = Number(sessionStorage.getItem(LAST_ACTIVITY_KEY) || 0)
    if (lastActive && Date.now() - lastActive > SESSION_TIMEOUT_MS) {
      sessionStorage.removeItem('token')
      localStorage.removeItem('token')
      sessionStorage.removeItem(TOKEN_EXPIRES_AT_KEY)
      sessionStorage.removeItem(LAST_ACTIVITY_KEY)
    }
    // 记录当前用户操作
    markUserActivity()

    // 获取token，注意不要修改token原始内容
    const token = getToken();

    // 调试日志（非生产环境）
    if (process.env.NODE_ENV !== 'production') {
      if (token) {
        console.log('正在发送请求，包含授权信息');
        // 打印token格式（不打印内容）
        if (token.startsWith('Bearer ')) {
          console.log('Token格式: 已包含Bearer前缀');
        } else {
          console.log('Token格式: 纯Token，将添加Bearer前缀');
        }
      } else {
        console.log('正在发送请求，无授权信息');
      }
    }

    // 添加token到header，确保格式正确
    if (token) {
      // 确保token格式正确，避免重复添加Bearer前缀
      let tokenToUse = token;
      if (!token.startsWith('Bearer ')) {
        tokenToUse = `Bearer ${token}`;
      }

      // 设置授权头
      config.headers['Authorization'] = tokenToUse;
    }

    return config;
  },
  error => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 将令牌状态信息同步到本地
    const headers = response.headers || {}
    const expiresAtHeader = headers['x-token-expires-at'] || headers['X-Token-Expires-At']
    if (expiresAtHeader) {
      setTokenExpiresAt(Number(expiresAtHeader))
    }
    const remainingHeader = headers['x-token-remaining-seconds'] || headers['X-Token-Remaining-Seconds']
    if (remainingHeader) {
      sessionStorage.setItem('token_remaining_seconds', remainingHeader)
    }

    // Conditional logging for RAW server response - can be enabled for deep debugging
    /*
    if (process.env.NODE_ENV !== 'production') {
      console.log('%cRAW Server Response Data:', 'color: blue; font-weight: bold;', JSON.stringify(response.data, null, 2));
      if (response.data && typeof response.data.token !== 'undefined') { 
          console.log('%cRAW Server Token field (from response.data.token):', 'color: blue; font-weight: bold;', response.data.token);
          console.log('%cRAW Server Token field typeof:', 'color: blue;', typeof response.data.token);
          if (typeof response.data.token === 'string') {
            console.log('%cRAW Server Token field length:', 'color: blue;', response.data.token.length);
          }
      } else if (response.data && response.data.data && typeof response.data.data.token !== 'undefined') {
          console.log('%cRAW Server Token field (from response.data.data.token):', 'color: blue; font-weight: bold;', response.data.data.token);
          console.log('%cRAW Server Token field (nested) typeof:', 'color: blue;', typeof response.data.data.token);
          if (typeof response.data.data.token === 'string') {
            console.log('%cRAW Server Token field (nested) length:', 'color: blue;', response.data.data.token.length);
          }
      } else {
        console.log('%cRAW Server Token field: Not found directly in response.data or response.data.data', 'color: orange;');
      }
    }
    */

    const res = response.data;

    if (process.env.NODE_ENV !== 'production') {
      let loggableRes = JSON.parse(JSON.stringify(res));
      if (loggableRes.token) {
        loggableRes.token = maskSensitiveInfo(String(loggableRes.token));
      } else if (loggableRes.data && loggableRes.data.token) {
        loggableRes.data.token = maskSensitiveInfo(String(loggableRes.data.token));
      }
      // Reduced verbosity for general API responses, still masked.
      // console.log('API响应数据 (masked for logging):', loggableRes);
    }

    // Process the original 'res' object from here on.
    // The 'res' object should NOT have been modified by the logging above.

    if (res.state !== undefined) {
      if (res.state === false) {
        return Promise.reject(new Error(res.errormsg || '请求失败'));
      }
      if (res.data !== undefined) {
        // For login, 'res.data' contains the actual payload { token, tokenType, ... }
        // This 'res.data' should be the original, unmodified one.
        if (response.config.url.includes('/auth/login') && res.data.token) {
          return res.data;
        }
        return res.data;
      }
      return res;
    }

    if (res.token) {
      return res;
    }

    if (response.config.url.includes('/auth/logout')) {
      return { success: true };
    }

    if (response.config.url.includes('/auth/user/info') && res.id && res.username) {
      return res;
    }

    if (res.code !== undefined && res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'));
    }

    if (res.code === undefined) {
      return res;
    }

    return res.data;
  },
  error => {
    console.error('请求错误:', error)

    // 特殊处理退出登录失败
    if (error.config && error.config.url.includes('/auth/logout')) {
      console.warn('退出登录请求失败，但仍然清除本地登录状态')
      return { success: true }
    }

    // 401错误处理 - 可能是token过期
    if (error.response && error.response.status === 401) {
      console.warn('认证失败，可能是token已过期')
      // 清除本地存储的token
      sessionStorage.removeItem('token')
      localStorage.removeItem('token')
      sessionStorage.removeItem(TOKEN_EXPIRES_AT_KEY)
      sessionStorage.removeItem(LAST_ACTIVITY_KEY)
      localStorage.removeItem('user')
      // 可以选择重定向到登录页
      if (window.location.hash !== '#/login') {
        window.location.href = '#/login'
      }
    }

    return Promise.reject(new Error(error.response?.data?.message || error.message || '网络请求失败'))
  }
)

/**
 * 认证相关的API
 */

// 模拟的异步API调用
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

/**
 * 登录接口
 * @param {string} username 用户名
 * @param {string} password 密码
 */
export const login = async (username, password) => {
  try {
    const response = await request({
      url: '/auth/login',
      method: 'post',
      data: { username, password }
    });

    // Conditional logging for token length - can be enabled for deep debugging
    /*
    if (process.env.NODE_ENV !== 'production' && response && response.token) {
      console.log('%cInitial token length in login function (response.token):', 'color: green; font-weight: bold;', response.token.length);
    }
    */

    const originalToken = response && response.token ? String(response.token) : null;

    // Minimal logging in production, more detailed (but still masked) in dev
    if (process.env.NODE_ENV !== 'production') {
      // console.log('登录响应状态:', response ? '成功' : '失败'); // Can be verbose
      // console.log('响应数据结构 (keys of response):', Object.keys(response || {}).join(', '));

      // const responseCopy = { ...response }; 
      // if (responseCopy.token) { 
      //   const tokenPrefix = String(responseCopy.token).substring(0, 6);
      //   responseCopy.token = `${tokenPrefix}******${String(responseCopy.token).substring(String(responseCopy.token).length - 6)}`; 
      //   // console.log('Token前缀 (for logging):', tokenPrefix); // Verbose
      // }
      // console.log('登录响应数据结构 (modified for logging):', JSON.stringify(responseCopy, null, 2));
    }

    if (!originalToken) {
      console.error('登录响应没有包含有效的token (originalToken is null or empty)');
      throw new Error('登录失败：无法获取访问令牌');
    }

    // This check can be useful even in production if issues arise, but less verbose.
    // if (!originalToken.startsWith('eyJ')) {
    //   console.warn('警告: 原始token可能不是标准JWT格式（应该以eyJ开头）');
    // }

    // More critical logs can remain, but less verbose ones commented out for now
    if (process.env.NODE_ENV !== 'production') {
      // console.log('登录成功，将返回的授权令牌'); // Verbose
      // console.log(`原始Token长度 (originalToken.length): ${originalToken.length}字符`);
      // console.log(`确认 response.token 长度在返回前: ${response.token ? response.token.length : 'N/A'}字符`);
    }

    // 保存过期时间
    const expiresAt = response.expiresAt || (response.expiresIn ? Date.now() + Number(response.expiresIn) * 1000 : 0)
    if (expiresAt) {
      setTokenExpiresAt(expiresAt)
    }
    markUserActivity()

    return response;

  } catch (error) {
    console.error('登录失败:', error.message);
    throw error;
  }
}

/**
 * 刷新令牌
 */
export const refreshToken = async () => {
  try {
    const response = await request({
      url: '/auth/refresh-token',
      method: 'post'
    })
    const expiresAt = response.expiresAt || (response.expiresIn ? Date.now() + Number(response.expiresIn) * 1000 : 0)
    if (expiresAt) {
      setTokenExpiresAt(expiresAt)
    }
    markUserActivity()
    return response
  } catch (error) {
    console.error('刷新令牌失败:', error.message)
    throw error
  }
}

/**
 * 获取用户信息
 */
export const getUserInfo = async () => {
  try {
    const response = await request({
      url: '/auth/user/info',
      method: 'get'
    });

    if (!response) {
      throw new Error('获取用户信息失败: 服务器未返回数据');
    }

    // 调试日志（非生产环境）
    if (process.env.NODE_ENV !== 'production') {
      const responseCopy = { ...response };
      // 移除敏感字段
      delete responseCopy.password;
      console.log('用户信息响应:', JSON.stringify(responseCopy, null, 2));
    }

    return response;
  } catch (error) {
    // 如果是401或403错误，可能是token问题
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.error('获取用户信息失败: 授权已过期或无效');

      // 在非生产环境下，打印调试信息
      if (process.env.NODE_ENV !== 'production') {
        const token = getToken();
        if (token) {
          console.log(`当前token长度: ${token.length}`);
          if (token.startsWith('Bearer ')) {
            console.log('Token包含Bearer前缀');
          }
        } else {
          console.log('未找到有效token');
        }
      }
    } else {
      console.error('获取用户信息失败:', error.message);
    }

    throw error;
  }
};

/**
 * 退出登录接口
 */
export const logout = async () => {
  await delay(200)
  return { success: true }
}

/**
 * 开发环境下的模拟用户数据
 * 仅在开发环境中使用，当无法从后端获取用户信息时作为回退机制
 */
const mockUserInfo = () => {
  return {
    id: 1,
    username: 'admin',
    roles: ['ADMIN'],
    permissions: [
      'user:view', 'user:create', 'user:update', 'user:delete',
      'role:view', 'role:create', 'role:update', 'role:delete',
      'permission:view', 'permission:create', 'permission:update', 'permission:delete',
      'permission:assign',
      'tenant:view', 'tenant:create', 'tenant:update', 'tenant:delete',
      'power:device', 'power:alarm', 'power:alarm:statistics'
    ]
  };
}

/**
 * 获取存储的token
 * 优先从sessionStorage获取，没有则从localStorage获取
 * 不对token做任何处理，保持原始格式
 */
export function getToken() {
  // 优先从sessionStorage获取token
  let token = sessionStorage.getItem('token');
  // 若旧版本残留localStorage，清理之
  try {
    if (localStorage.getItem('token')) {
      localStorage.removeItem('token');
    }
  } catch (e) {
    console.warn('清理旧localStorage token失败');
  }
  return token;
}

/**
 * 保存token过期时间（毫秒时间戳）
 */
export function setTokenExpiresAt(expiresAt) {
  if (!expiresAt) return
  try {
    sessionStorage.setItem(TOKEN_EXPIRES_AT_KEY, String(expiresAt))
  } catch (e) {
    console.warn('保存token过期时间失败', e)
  }
}

/**
 * 获取token过期时间（毫秒时间戳）
 */
export function getTokenExpiresAt() {
  const val = sessionStorage.getItem(TOKEN_EXPIRES_AT_KEY)
  if (!val) return 0
  const parsed = Number(val)
  return Number.isNaN(parsed) ? 0 : parsed
}

/**
 * 记录用户最近活跃时间
 */
export function markUserActivity() {
  try {
    sessionStorage.setItem(LAST_ACTIVITY_KEY, String(Date.now()))
  } catch (e) {
    console.warn('记录活跃时间失败', e)
  }
}

/**
 * 是否会话超时
 */
export function hasSessionTimedOut() {
  const last = Number(sessionStorage.getItem(LAST_ACTIVITY_KEY) || 0)
  if (!last) return false
  return Date.now() - last > SESSION_TIMEOUT_MS
}

/**
 * 检查JWT格式是否有效
 * 仅用于调试目的，不会暴露任何敏感信息
 */
export function debugJwtToken() {
  // 仅在非生产环境中运行
  if (process.env.NODE_ENV === 'production') {
    console.warn('调试函数不应在生产环境中使用');
    return { valid: true, parts: 0 };
  }

  const token = getToken();

  if (!token) {
    console.warn('未找到token，可能未登录');
    return { valid: false, parts: 0 };
  }

  // 分析token格式
  console.log(`Token长度: ${token.length}字符`);

  // 检查是否有Bearer前缀
  const hasBearer = token.startsWith('Bearer ');
  console.log(`Token前缀: ${hasBearer ? '包含Bearer ' : '无Bearer前缀'}`);

  // 分析JWT部分
  const actualToken = hasBearer ? token.substring(7) : token;
  const parts = actualToken.split('.');
  console.log(`JWT部分数量: ${parts.length} (标准JWT应有3部分)`);

  // 检查格式有效性
  let isValid = parts.length === 3;

  if (!isValid) {
    console.warn('JWT格式异常，标准JWT应有3个部分（header.payload.signature）');

    // 提供可能的原因
    if (parts.length < 3) {
      console.warn('可能原因: JWT不完整或分隔符(.)丢失');

      // 检查是否使用了其他分隔符
      const otherDelimiters = [',', ';', '-', '_'];
      otherDelimiters.forEach(delimiter => {
        const testParts = actualToken.split(delimiter);
        if (testParts.length > 1) {
          console.warn(`发现可能使用了不同的分隔符: "${delimiter}" (${testParts.length}部分)`);
        }
      });

      // 检查长度是否异常短
      if (actualToken.length < 20) {
        console.warn('JWT长度异常短，可能不是有效JWT');
      }
    } else if (parts.length > 3) {
      console.warn('可能原因: JWT中包含额外的分隔符(.)');
    }
  }

  return { valid: isValid, parts: parts.length };
}

/**
 * SSO相关接口
 */

/**
 * 检查SSO是否启用
 */
export const checkSSOEnabled = async () => {
  try {
    const response = await request({
      url: '/auth/sso/enabled',
      method: 'get'
    })
    return response
  } catch (error) {
    console.error('检查SSO启用状态失败:', error)
    return { enabled: false }
  }
}

/**
 * 获取SSO授权URL
 */
export const getSSOAuthorizeUrl = async () => {
  try {
    const response = await request({
      url: '/auth/sso/authorize-url',
      method: 'get'
    })
    return response
  } catch (error) {
    console.error('获取SSO授权URL失败:', error)
    throw error
  }
}

/**
 * 启动SSO登录
 */
export const startSSOLogin = () => {
  // 直接重定向到后端的SSO登录端点
  window.location.href = '/api/auth/sso/login'
}

/**
 * 处理SSO回调
 */
export const handleSSOCallback = async (code, state) => {
  try {
    const response = await request({
      url: '/auth/oauth2/callback',
      method: 'post',
      data: { code, state }
    })
    return response
  } catch (error) {
    console.error('处理SSO回调失败:', error)
    throw error
  }
}

export default request
