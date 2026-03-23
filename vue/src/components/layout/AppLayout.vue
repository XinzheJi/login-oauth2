<template>
  <div class="app-wrapper">
    <!-- <app-header />  顶部菜单栏 -->
    
    <div class="main-container">
      <div class="content-wrapper">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, watch, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import AppHeader from './Header.vue'

export default {
  name: 'AppLayout',
  components: {
    AppHeader
  },
  setup() {
    const userStore = useUserStore()
    const route = useRoute()
    const router = useRouter()
    let allowedDashboardOrigins = null
    
    // 全局跳转方法：供iframe中的子页面调用
    const handleDashboardJump = (path, query = {}) => {
      try {
        console.log('=== handleDashboardJump 被调用 ===')
        console.log('目标路径:', path)
        console.log('查询参数:', query)
        console.log('当前路由:', route.path)
        
        // 使用 router.push 进行跳转
        router.push({
          path: path,
          query: query
        }).then(() => {
          console.log('✓ 路由跳转成功，当前路由:', router.currentRoute.value.path)
        }).catch((error) => {
          console.error('× 路由跳转失败:', error)
          console.error('错误详情:', error.message)
          if (error.name) {
            console.error('错误名称:', error.name)
          }
        })
      } catch (error) {
        console.error('× handleDashboardJump 执行异常:', error)
        console.error('错误详情:', error.message)
        if (error.stack) {
          console.error('错误堆栈:', error.stack)
        }
      }
    }
    
    // 监听来自 iframe 的 postMessage
    const handleMessage = (event) => {
      const data = event && event.data ? event.data : null
      if (!data || data.type !== 'DASHBOARD_JUMP') {
        return
      }

      // 安全检查：仅接受来自允许来源的消息（默认仅同源）
      if (!allowedDashboardOrigins || !allowedDashboardOrigins.has(event.origin)) {
        console.warn('拒绝来自未授权 origin 的大屏消息:', event.origin)
        return
      }

      const path = typeof data.path === 'string' ? data.path : ''
      if (!path || !path.startsWith('/')) {
        console.warn('拒绝非法跳转路径:', path)
        return
      }

      const query = (data.query && typeof data.query === 'object') ? data.query : {}
      console.log('收到来自 iframe 的跳转请求:', { path })
      handleDashboardJump(path, query)
    }
    
    // 组件挂载时，将跳转方法挂载到 window 对象上（用于同源情况）
    // 同时监听 postMessage（用于跨域情况）
    onMounted(() => {
      // 允许的 dashboard origins：VUE_APP_DASHBOARD_ORIGINS 逗号分隔；默认仅同源
      try {
        const raw = (process.env.VUE_APP_DASHBOARD_ORIGINS || '').trim()
        const list = raw ? raw.split(',').map(s => s.trim()).filter(Boolean) : []
        const dashboardUrl = (process.env.VUE_APP_DASHBOARD_URL || '').trim()
        if (dashboardUrl) {
          try {
            list.push(new URL(dashboardUrl, window.location.origin).origin)
          } catch (e) {
            // ignore
          }
        }
        list.push(window.location.origin)
        allowedDashboardOrigins = new Set(list)
      } catch (e) {
        allowedDashboardOrigins = new Set([window.location.origin])
      }

      window.handleDashboardJump = handleDashboardJump
      console.log('全局跳转方法已挂载到 window.handleDashboardJump')
      
      // 监听 postMessage
      window.addEventListener('message', handleMessage)
      console.log('已监听 postMessage 事件')
    })
    
    // 组件卸载时，清理全局方法和事件监听
    onUnmounted(() => {
      if (window.handleDashboardJump) {
        delete window.handleDashboardJump
        console.log('全局跳转方法已清理')
      }
      window.removeEventListener('message', handleMessage)
      console.log('已移除 postMessage 事件监听')
    })
    
    return {
      userStore
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
  overflow-y: auto;
  background: #fff;
}
</style>