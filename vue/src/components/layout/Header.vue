<template>
  <div class="app-header">
    <!-- 顶部品牌区域 -->
    <div class="header-brand">
      <div class="brand-icon">
        <el-icon><Monitor /></el-icon>
      </div>
      <div class="brand-title">自治自愈运行管控平台</div>
    </div>
    
    <!-- 主导航菜单 -->
    <div class="header-nav" v-if="userStore.isLoggedIn">
      <nav class="main-nav">
        <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
          首页
        </router-link>
        <router-link to="/dashboard" class="nav-item" :class="{ active: $route.path === '/dashboard' }">
          综合监控
        </router-link>
        <div class="nav-dropdown">
          <span class="nav-item dropdown-trigger">资源管理</span>
          <div class="dropdown-menu">
            <router-link to="/power-devices" class="dropdown-item" v-permission="'power:device'">
              电源台账管理
            </router-link>
            <router-link to="/switch-list" class="dropdown-item" v-permission="'power:device'">
              交换机管理
            </router-link>
            <router-link to="/model-type" class="dropdown-item" v-permission="'power:device'">
              设备型号管理
            </router-link>
            <router-link to="/device-aging-fault" class="dropdown-item" v-permission="'power:device'">
              设备老化与故障感知
            </router-link>
          </div>
        </div>
        <div class="nav-dropdown">
          <span class="nav-item dropdown-trigger">报表管理</span>
          <div class="dropdown-menu">
            <router-link to="/power-alarms" class="dropdown-item" v-permission="'power:alarm'">
              电源告警管理
            </router-link>
            <router-link to="/power-alarm-statistics" class="dropdown-item" v-permission="'power:alarm:statistics'">
              告警统计分析
            </router-link>
          </div>
        </div>
        <div class="nav-dropdown">
          <span class="nav-item dropdown-trigger">系统管理</span>
          <div class="dropdown-menu">
            <router-link to="/users" class="dropdown-item" v-permission="'user:view'">
              用户管理
            </router-link>
            <router-link to="/roles" class="dropdown-item" v-permission="'role:view'">
              角色管理
            </router-link>
            <router-link to="/permissions" class="dropdown-item" v-permission="'permission:view'">
              权限管理
            </router-link>
            <router-link to="/tenants" class="dropdown-item" v-permission="'tenant:view'">
              租户管理
            </router-link>
            <router-link to="/health-check" class="dropdown-item">
              系统健康检查
            </router-link>
          </div>
        </div>
        <router-link to="/about" class="nav-item" :class="{ active: $route.path === '/about' }">
          问题反馈
        </router-link>
      </nav>
    </div>
    
    <!-- 右侧功能区 -->
    <div class="header-actions">
      <div class="notification-icon">
        <el-icon><Bell /></el-icon>
      </div>
      <div class="user-info" v-if="userStore.isLoggedIn">
        <el-dropdown trigger="click">
          <div class="user-dropdown-link">
            <span class="username">{{ userStore.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item>修改密码</el-dropdown-item>
              <el-dropdown-item divided @click="logout">
                <span style="color: #f56c6c;">退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '@/store/user'
import { Monitor, Bell, ArrowDown } from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    Monitor,
    Bell,
    ArrowDown
  },
  setup() {
    const userStore = useUserStore()
    
    const logout = async () => {
      await userStore.logoutAction()
      // 退出后会自动跳转到登录页
    }
    
    return {
      userStore,
      logout
    }
  }
}
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  display: flex;
  align-items: center;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;
}

/* 品牌区域 */
.header-brand {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.1);
  height: 100%;
  min-width: 280px;
}

.brand-icon {
  font-size: 24px;
  margin-right: 12px;
  color: white;
}

.brand-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

/* 主导航区域 */
.header-nav {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  background: white;
  height: 100%;
}

.main-nav {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 100%;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
  position: relative;
}

.nav-item:hover {
  color: #409EFF;
  background-color: #f5f7fa;
}

.nav-item.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
  background-color: #f0f9ff;
}

/* 下拉菜单 */
.nav-dropdown {
  position: relative;
  height: 100%;
}

.dropdown-trigger {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 100%;
  color: #666;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
}

.dropdown-trigger:hover {
  color: #409EFF;
  background-color: #f5f7fa;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s ease;
  z-index: 1001;
}

.nav-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: block;
  padding: 12px 20px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f0f0f0;
}

.dropdown-item:hover {
  color: #409EFF;
  background-color: #f0f9ff;
}

.dropdown-item:last-child {
  border-bottom: none;
}

/* 右侧功能区 */
.header-actions {
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: white;
  height: 100%;
  gap: 20px;
}

.notification-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: color 0.3s ease;
}

.notification-icon:hover {
  color: #409EFF;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: color 0.3s ease;
}

.user-dropdown-link:hover {
  color: #409EFF;
}

.username {
  margin-right: 8px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-brand {
    min-width: 200px;
  }
  
  .brand-title {
    font-size: 16px;
  }
  
  .nav-item {
    padding: 0 15px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    height: auto;
  }
  
  .header-brand {
    width: 100%;
    justify-content: center;
    min-width: auto;
  }
  
  .header-nav {
    width: 100%;
    overflow-x: auto;
  }
  
  .main-nav {
    min-width: max-content;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
    padding: 10px 20px;
  }
}
</style> 