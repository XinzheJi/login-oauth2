<template>
  <el-menu
    class="el-menu-vertical"
    :default-active="activeMenu"
    router
    background-color="#f5f5f5"
    text-color="#2c3e50"
    active-text-color="#409EFF"
  >
    <div class="menu-section">
      <div class="menu-title">主要菜单</div>
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>

      <el-sub-menu index="1" v-permission="'USER:VIEW'">
        <template #title>
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </template>
        <el-menu-item index="/users">用户列表</el-menu-item>
        <el-menu-item index="/users/create">新建用户</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="2" v-permission="'ROLE:VIEW'">
        <template #title>
          <el-icon><Key /></el-icon>
          <span>角色管理</span>
        </template>
        <el-menu-item index="/roles">角色列表</el-menu-item>
        <el-menu-item index="/roles/create">新建角色</el-menu-item>
      </el-sub-menu>
    </div>

    <div class="menu-section">
      <div class="menu-title">设备管理</div>
      
      <el-sub-menu index="3" v-permission="'power:device'">
        <template #title>
          <el-icon><Connection /></el-icon>
          <span>电源合账管理</span>
        </template>
        <el-menu-item index="/power/devices">设备列表</el-menu-item>
        <el-menu-item index="/power/devices/create">添加设备</el-menu-item>
        <el-menu-item index="/power/monitor">设备监控</el-menu-item>
      </el-sub-menu>

      <el-sub-menu index="4" v-permission="'power:alarm'">
        <template #title>
          <el-icon><Warning /></el-icon>
          <span>电源告警管理</span>
        </template>
        <el-menu-item index="/power/alarms">告警列表</el-menu-item>
        <el-menu-item index="/power/alarms/statistics">告警统计</el-menu-item>
        <el-menu-item index="/power/alarms/config">告警配置</el-menu-item>
      </el-sub-menu>
    </div>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const { meta, path } = route
  return meta.activeMenu || path
})
</script>

<style scoped>
.menu-section {
  padding: 12px 20px;
}
.menu-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}
</style>