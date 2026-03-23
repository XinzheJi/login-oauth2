<template>
  <div class="app-header">
    <!-- 顶部品牌区域 -->
    <div class="brand-section">
      <div class="brand-icon">
        <el-icon><Monitor /></el-icon>
      </div>
      <div class="brand-title">配电网通信运行管控平台</div>
    </div>
    
    <!-- 顶部导航菜单 -->
    <div class="nav-section" v-if="userStore.isLoggedIn">
      <nav class="main-nav">
        <template v-for="item in mainMenuItems" :key="item.path">
          <!-- 有子菜单的项目 -->
          <el-dropdown v-if="item.children" trigger="hover" placement="bottom-start">
            <div class="nav-item dropdown-item" :class="{ active: isActiveMenu(item) }">
              {{ item.name }}
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-for="child in item.children" 
                  :key="child.path"
                  @click="$router.push(child.path)"
                  :class="{ active: $route.path === child.path }"
                >
                  {{ child.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 无子菜单的项目 -->
          <router-link 
            v-else-if="!item.external"
            :to="item.path"
            class="nav-item"
            :class="{ active: $route.path === item.path }"
          >
            {{ item.name }}
          </router-link>
          
          <!-- 外部链接项目 -->
          <span 
            v-else
            class="nav-item external-link"
            @click="handleExternalClick(item)"
          >
            {{ item.name }}
            <el-icon class="external-icon"><ExternalLink /></el-icon>
          </span>
        </template>
      </nav>
    </div>
    
    <!-- 右侧功能区 -->
    <div class="action-section">
      <div class="notification-area">
        <el-icon class="notification-icon"><Bell /></el-icon>
      </div>
      <div class="user-info" v-if="userStore.isLoggedIn">
        <el-dropdown trigger="click">
          <div class="user-dropdown-link">
            <span class="username">{{ userStore.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
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
import { useRoute } from 'vue-router'
import { Monitor, ArrowDown, Bell, ExternalLink } from '@element-plus/icons-vue'

export default {
  name: 'AppHeader',
  components: {
    Monitor,
    ArrowDown,
    Bell,
    ExternalLink
  },
  setup() {
    const userStore = useUserStore()
    const route = useRoute()
    
    // 主要导航菜单项（精简版：只保留核心页面入口）
    const mainMenuItems = [
      { name: '综合监控', path: '/dashboard' },
      { 
        name: '资源管理', 
        path: '/power-devices',
        children: [
          { name: '电源台账管理', path: '/power-devices' },
          { name: '设备老化与故障感知', path: '/device-aging-fault' }
        ]
      },
      { 
        name: '报表管理', 
        path: '/power-alarm-statistics',
        children: [
          { name: '告警统计分析', path: '/power-alarm-statistics' },
          { name: '电源告警管理', path: '/power-alarms' }
        ]
      },
      { 
        name: '系统管理', 
        path: '/users',
        children: [
          { name: '系统概述', path: '/home' },
          { name: '用户管理', path: '/users' },
          { name: '角色管理', path: '/roles' },
          { name: '权限管理', path: '/permissions' },
          { name: '租户管理', path: '/tenants' },
          { name: '系统健康检查', path: '/health-check' }
        ]
      },
      { name: '配网管控', path: 'https://www.baidu.com', external: true },
      //修改网址改变跳转网页
      { name: '问题反馈', path: '/about' }
    ]

    // 判断菜单是否激活
    const isActiveMenu = (item) => {
      if (item.children) {
        return item.children.some(child => child.path === route.path)
      }
      return item.path === route.path
    }
    
    const logout = async () => {
      await userStore.logoutAction()
      // 退出后会自动跳转到登录页
    }
    
    // 处理外部链接点击
    const handleExternalClick = (item) => {
      console.log('点击外部链接:', item.name, 'URL:', item.path)
      // 在新窗口中打开外部链接
      window.open(item.path, '_blank')
    }
    
    return {
      userStore,
      mainMenuItems,
      isActiveMenu,
      logout,
      handleExternalClick
    }
  }
}
</script>

<style scoped>
.app-header {
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 品牌区域 */
.brand-section {
  background-color: #1e3a8a;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 100%;
  min-width: 280px;
}

.brand-icon {
  margin-right: 12px;
  font-size: 20px;
}

.brand-title {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

/* 导航区域 */
.nav-section {
  flex: 1;
  background-color: #ffffff;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-item {
  color: #666666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.nav-item:hover {
  color: #1e3a8a;
}

.nav-item.active {
  color: #1e3a8a;
  border-bottom-color: #1e3a8a;
}

.external-link {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.external-icon {
  font-size: 12px;
  opacity: 0.7;
}

.dropdown-item {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.dropdown-arrow {
  font-size: 12px;
  transition: transform 0.3s ease;
}

.dropdown-item:hover .dropdown-arrow {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.el-dropdown-menu {
  border: 1px solid #e0e0e0 !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  border-radius: 6px !important;
  padding: 8px 0 !important;
}

.el-dropdown-menu__item {
  padding: 8px 16px !important;
  font-size: 14px !important;
  color: #333333 !important;
  transition: all 0.3s ease !important;
}

.el-dropdown-menu__item:hover {
  background-color: #f5f5f5 !important;
  color: #1e3a8a !important;
}

.el-dropdown-menu__item.active {
  background-color: #e6f3ff !important;
  color: #1e3a8a !important;
  font-weight: 500 !important;
}

/* 右侧功能区 */
.action-section {
  background-color: #ffffff;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 100%;
  gap: 16px;
}

.notification-area {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.notification-area:hover {
  background-color: #f5f5f5;
}

.notification-icon {
  font-size: 18px;
  color: #666666;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.user-dropdown-link:hover {
  background-color: #f5f5f5;
}

.username {
  color: #333333;
  font-size: 14px;
  font-weight: 500;
  margin-right: 8px;
}

.dropdown-icon {
  font-size: 12px;
  color: #666666;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .brand-section {
    min-width: 240px;
  }
  
  .brand-title {
    font-size: 14px;
  }
  
  .main-nav {
    gap: 24px;
  }
  
  .nav-item {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .app-header {
    flex-direction: column;
    height: auto;
  }
  
  .brand-section {
    width: 100%;
    min-width: auto;
    justify-content: center;
  }
  
  .nav-section {
    width: 100%;
    padding: 10px 20px;
  }
  
  .main-nav {
    justify-content: center;
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .action-section {
    width: 100%;
    justify-content: center;
    padding: 10px 20px;
    border-top: 1px solid #e0e0e0;
  }
}
</style> 
