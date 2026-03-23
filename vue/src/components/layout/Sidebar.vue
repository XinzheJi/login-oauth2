<template>
  <el-aside width="220px" class="app-sidebar">
    <el-menu
      class="el-menu-vertical"
      :default-active="activeMenu"
      router
      :collapse="isCollapse"
      background-color="#f5f5f5"
      text-color="#2c3e50"
      active-text-color="#409EFF"
    >
      <div class="menu-section">
        <div class="menu-title">主要菜单</div>
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/about">
          <el-icon><InfoFilled /></el-icon>
          <template #title>关于</template>
        </el-menu-item>
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>数据大屏</template>
        </el-menu-item>
      </div>
      
      <div class="menu-section">
        <div class="menu-title">系统管理</div>
        <el-menu-item v-permission="'user:view'" index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'role:view'" index="/roles">
          <el-icon><Key /></el-icon>
          <template #title>角色管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'permission:view'" index="/permissions">
          <el-icon><Lock /></el-icon>
          <template #title>权限管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'tenant:view'" index="/tenants">
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>租户管理</template>
        </el-menu-item>
        <el-menu-item index="/health-check">
          <el-icon><Monitor /></el-icon>
          <template #title>系统健康检查</template>
        </el-menu-item>
      </div>

      <div class="menu-section">
        <div class="menu-title">设备管理</div>
        <el-menu-item v-permission="'power:device'" index="/power-devices">
          <el-icon><Cpu /></el-icon>
          <template #title>电源台账管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'power:alarm'" index="/power-alarms">
          <el-icon><BellFilled /></el-icon>
          <template #title>电源告警管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'power:alarm:statistics'" index="/power-alarm-statistics">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>告警统计分析</template>
        </el-menu-item>
        <!-- 权限还未设置 -->
        <el-menu-item v-permission="'power:device'" index="/switch-list">
          <el-icon><Guide /></el-icon>
          <template #title>交换机管理</template>
        </el-menu-item>
        <!-- 权限还未设置 -->
        <el-menu-item v-permission="'power:device'" index="/model-type">
          <el-icon><CollectionTag /></el-icon>
          <template #title>设备型号管理</template>
        </el-menu-item>
        <el-menu-item v-permission="'power:device'" index="/device-aging-fault">
          <el-icon><TrendCharts /></el-icon>
          <template #title>设备老化与故障感知</template>
        </el-menu-item>
       </div>
    </el-menu>
    
    <div class="sidebar-footer">
      <el-button 
        type="primary" 
        circle
        size="small"
        class="collapse-btn"
        @click="toggleCollapse"
      >
        <el-icon>
          <component :is="isCollapse ? 'ArrowRight' : 'ArrowLeft'" />
        </el-icon>
      </el-button>
    </div>
  </el-aside>
</template>

<script>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  InfoFilled,
  User,
  Key,
  Lock,
  OfficeBuilding,
  ArrowRight,
  ArrowLeft,
  Cpu,
  BellFilled,
  DataAnalysis,
  Monitor,
  Guide,
  CollectionTag,
  TrendCharts
} from '@element-plus/icons-vue'

export default {
  name: 'AppSidebar',
  components: {
    HomeFilled,
    InfoFilled,
    User,
    Key,
    Lock,
    OfficeBuilding,
    ArrowRight,
    ArrowLeft,
    Cpu,
    BellFilled,
    DataAnalysis,
    Monitor,
    Guide,
    CollectionTag,
    TrendCharts
  },
  setup() {
    const route = useRoute()
    const isCollapse = ref(false)
    
    // 获取当前激活的菜单项
    const activeMenu = computed(() => {
      return route.path
    })
    
    const toggleCollapse = () => {
      isCollapse.value = !isCollapse.value
    }
    
    return {
      activeMenu,
      isCollapse,
      toggleCollapse
    }
  }
}
</script>

<style scoped>
.app-sidebar {
  background-color: #f5f5f5;
  border-right: 1px solid #e0e0e0;
  padding: 0;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  transition: width 0.3s;
}

.el-menu-vertical {
  border-right: none;
  flex-grow: 1;
}

.menu-section {
  margin-bottom: 20px;
}

.menu-title {
  padding: 15px 20px 5px;
  font-size: 12px;
  color: #909399;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.sidebar-footer {
  padding: 10px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #e0e0e0;
}

.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style> 