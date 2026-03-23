<template>
  <div class="app-wrapper">
    <app-header />
    
    <div class="main-container">
      <div class="content-wrapper">
        <tabs-container v-model="activeTab" @tab-change="handleTabChange" @tab-close="handleTabClose">
          <router-view />
        </tabs-container>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, getCurrentInstance, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import AppHeader from './Header.vue'
import TabsContainer from '../common/TabsContainer.vue'

export default {
  name: 'AppLayout',
  components: {
    AppHeader,
    TabsContainer
  },
  setup() {
    const userStore = useUserStore()
    const route = useRoute()
    const router = useRouter()
    const activeTab = ref('/')
    
    // 监听路由变化，更新激活标签
    watch(route, (newRoute) => {
      console.log('AppLayout 路由变化:', newRoute.path)
      activeTab.value = newRoute.path
    }, { immediate: true })
    
    const handleTabChange = (tabId) => {
      // 标签切换逻辑已经在 TabsContainer 中处理，这里不需要重复处理
      console.log('AppLayout 处理标签切换:', tabId)
    }
    
    const handleTabClose = (tabId) => {
      // 如果关闭的是当前路由，跳转到首页
      if (tabId === route.path) {
        router.push('/')
      }
    }
    
    return {
      userStore,
      activeTab,
      handleTabChange,
      handleTabClose
    }
  }
}
</script>

<style scoped>
.app-wrapper {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.content-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 确保标签页容器占满剩余空间 */
.content-wrapper :deep(.tabs-container) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 调整标签页内容区域 */
.content-wrapper :deep(.tabs-content) {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #fff;
}
</style> 