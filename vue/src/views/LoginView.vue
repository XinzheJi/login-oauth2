<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>系统登录</h2>
        </div>
      </template>
      
      <!-- 登录方式选择 -->
      <div class="login-method-tabs">
        <el-tabs v-model="activeLoginMethod" @tab-change="handleLoginMethodChange">
          <el-tab-pane label="本地登录" name="local">
            <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top" @submit.prevent="handleLocalLogin">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
              </el-form-item>
              
              <el-form-item label="密码" prop="password">
                <el-input 
                  v-model="loginForm.password" 
                  placeholder="请输入密码" 
                  prefix-icon="Lock"
                  type="password" 
                  show-password 
                />
              </el-form-item>
              
              <div class="login-tips">
                <p>管理员账号: admin / admin</p>
                <p>普通用户: user / user</p>
              </div>
              
              <el-form-item>
                <el-button type="primary" native-type="submit" :loading="loading" class="login-button">
                  本地登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="单点登录" name="sso" v-if="ssoEnabled">
            <div class="sso-login-container">
              <div class="sso-description">
                <el-icon><Connection /></el-icon>
                <p>使用 {{ ssoProvider || '第三方系统' }} 账号登录</p>
                <p class="sso-subtitle">安全、便捷的企业统一认证</p>
              </div>
              
              <el-button 
                type="primary" 
                :loading="ssoLoading" 
                @click="handleSSOLogin"
                class="sso-login-button"
                size="large"
              >
                <el-icon><User /></el-icon>
                通过 {{ ssoProvider || 'SSO' }} 登录
              </el-button>
              
              <div class="sso-tips">
                <p>将跳转到统一认证平台进行身份验证</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Connection, User } from '@element-plus/icons-vue'

export default {
  name: 'LoginView',
  components: {
    Connection,
    User
  },
  setup() {
    const userStore = useUserStore()
    const loginFormRef = ref(null)
    const loading = ref(false)
    const ssoLoading = ref(false)
    const activeLoginMethod = ref('local')
    
    // 计算属性
    const ssoEnabled = computed(() => userStore.ssoEnabled)
    const ssoProvider = computed(() => userStore.ssoProvider)
    
    // 登录表单
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    // 表单校验规则
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度应在3到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 3, max: 20, message: '密码长度应在3到20个字符之间', trigger: 'blur' }
      ]
    }
    
    // 处理本地登录
    const handleLocalLogin = async () => {
      if (!loginFormRef.value) return
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            await userStore.loginAction(loginForm.username, loginForm.password)
            ElMessage.success('登录成功')
          } catch (error) {
            ElMessage.error(error.message || '登录失败，请重试')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    // 处理SSO登录
    const handleSSOLogin = async () => {
      ssoLoading.value = true
      try {
        await userStore.startSSOLoginAction()
      } catch (error) {
        ElMessage.error(error.message || 'SSO登录失败，请重试')
      } finally {
        ssoLoading.value = false
      }
    }
    
    // 处理登录方式切换
    const handleLoginMethodChange = (methodName) => {
      console.log('切换登录方式:', methodName)
      activeLoginMethod.value = methodName
    }
    
    // 初始化
    onMounted(async () => {
      // 检查SSO是否启用
      await userStore.checkSSOEnabledAction()
      
      // 检查URL参数中是否有SSO回调信息
      const hasSSOCallback = await userStore.checkSSOCallbackFromUrl()
      
      // 如果处理了SSO回调，直接返回，不再显示登录页面
      if (hasSSOCallback) {
        return
      }
      
      // 如果SSO启用且没有已登录用户，默认显示SSO登录
      if (userStore.ssoEnabled && !userStore.isLoggedIn) {
        activeLoginMethod.value = 'sso'
      }
    })
    
    return {
      loginForm,
      loginFormRef,
      rules,
      loading,
      ssoLoading,
      activeLoginMethod,
      ssoEnabled,
      ssoProvider,
      handleLocalLogin,
      handleSSOLogin,
      handleLoginMethodChange
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-card {
  width: 420px;
  max-width: 90%;
}

.login-header {
  text-align: center;
}

.login-method-tabs {
  margin-top: 10px;
}

.login-button {
  width: 100%;
}

.login-tips {
  margin: 15px 0;
  font-size: 13px;
  color: #909399;
}

.login-tips p {
  margin: 5px 0;
}

/* SSO登录样式 */
.sso-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.sso-description {
  text-align: center;
  margin-bottom: 30px;
}

.sso-description .el-icon {
  font-size: 48px;
  color: #409EFF;
  margin-bottom: 16px;
}

.sso-description p {
  margin: 8px 0;
  font-size: 16px;
  color: #303133;
}

.sso-subtitle {
  font-size: 14px !important;
  color: #909399 !important;
}

.sso-login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  margin-bottom: 20px;
}

.sso-login-button .el-icon {
  margin-right: 8px;
}

.sso-tips {
  text-align: center;
}

.sso-tips p {
  font-size: 12px;
  color: #C0C4CC;
  margin: 0;
}

/* Tab样式优化 */
:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-tabs__content) {
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: 95%;
    margin: 10px;
  }
  
  .sso-description .el-icon {
    font-size: 36px;
  }
  
  .sso-description p {
    font-size: 14px;
  }
  
  .sso-login-button {
    font-size: 14px;
    height: 44px;
  }
}
</style>