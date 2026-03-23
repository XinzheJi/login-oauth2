<template>
  <div class="dashboard-iframe-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-loading
        :text="loadingText"
        background="rgba(0, 0, 0, 0.8)"
        element-loading-spinner="el-icon-loading"
        element-loading-text="正在加载大屏数据..."
      />
    </div>
    
    <!-- 错误状态 -->
    <div v-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="errorMessage"
      >
        <template #extra>
          <el-button type="primary" @click="retryLoad">重新加载</el-button>
          <el-button @click="openInNewTab">在新窗口中打开</el-button>
        </template>
      </el-result>
    </div>
    
    <!-- iframe容器 -->
    <div v-show="!loading && !error" class="iframe-wrapper">
      <iframe
        ref="dashboardIframe"
        :src="dashboardUrl"
        class="dashboard-iframe"
        frameborder="0"
        allowfullscreen
        @load="onIframeLoad"
        @error="onIframeError"
      />
    </div>
    
    <!-- 工具栏 -->
    <div class="iframe-toolbar">
      <el-button-group>
        <el-button 
          size="small" 
          :icon="isFullscreen ? 'FullScreen' : 'Aim'" 
          @click="toggleFullscreen"
        >
          {{ isFullscreen ? '退出全屏' : '全屏显示' }}
        </el-button>
        <el-button 
          size="small" 
          icon="Refresh" 
          @click="refreshIframe"
        >
          刷新
        </el-button>
        <el-button 
          size="small" 
          icon="Link" 
          @click="openInNewTab"
        >
          新窗口打开
        </el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'DashboardIframe',
  props: {
    // 大屏项目地址
    url: {
      type: String,
      default: 'http://192.168.56.1:3000'
    },
    // 加载超时时间（毫秒）
    timeout: {
      type: Number,
      default: 10000
    }
  },
  setup(props) {
    const dashboardIframe = ref(null)
    const loading = ref(true)
    const error = ref(false)
    const errorMessage = ref('')
    const loadingText = ref('正在连接大屏系统...')
    const isFullscreen = ref(false)
    const timeoutId = ref(null)
    
    // 计算属性：大屏URL
    const dashboardUrl = ref(props.url)
    
    // iframe加载完成
    const onIframeLoad = () => {
      loading.value = false
      error.value = false
      if (timeoutId.value) {
        clearTimeout(timeoutId.value)
        timeoutId.value = null
      }
      ElMessage.success('大屏加载成功')
    }
    
    // iframe加载错误
    const onIframeError = () => {
      loading.value = false
      error.value = true
      errorMessage.value = '无法连接到大屏系统，请检查网络连接或联系管理员'
      if (timeoutId.value) {
        clearTimeout(timeoutId.value)
        timeoutId.value = null
      }
      ElMessage.error('大屏加载失败')
    }
    
    // 重新加载
    const retryLoad = () => {
      error.value = false
      loading.value = true
      loadingText.value = '正在重新连接...'
      
      // 重新设置iframe src
      if (dashboardIframe.value) {
        dashboardIframe.value.src = dashboardUrl.value
      }
      
      // 设置超时
      timeoutId.value = setTimeout(() => {
        if (loading.value) {
          onIframeError()
        }
      }, props.timeout)
    }
    
    // 刷新iframe
    const refreshIframe = () => {
      if (dashboardIframe.value) {
        loading.value = true
        error.value = false
        loadingText.value = '正在刷新...'
        dashboardIframe.value.src = dashboardUrl.value
      }
    }
    
    // 在新窗口中打开
    const openInNewTab = () => {
      window.open(dashboardUrl.value, '_blank')
    }
    
    // 切换全屏
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        // 进入全屏
        const container = document.querySelector('.dashboard-iframe-container')
        if (container.requestFullscreen) {
          container.requestFullscreen()
          isFullscreen.value = true
        }
      } else {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen()
          isFullscreen.value = false
        }
      }
    }
    
    // 监听全屏状态变化
    const handleFullscreenChange = () => {
      isFullscreen.value = !!document.fullscreenElement
    }
    
    // 组件挂载
    onMounted(() => {
      // 设置加载超时
      timeoutId.value = setTimeout(() => {
        if (loading.value) {
          onIframeError()
        }
      }, props.timeout)
      
      // 监听全屏状态变化
      document.addEventListener('fullscreenchange', handleFullscreenChange)
    })
    
    // 组件卸载
    onUnmounted(() => {
      if (timeoutId.value) {
        clearTimeout(timeoutId.value)
      }
      document.removeEventListener('fullscreenchange', handleFullscreenChange)
    })
    
    return {
      dashboardIframe,
      loading,
      error,
      errorMessage,
      loadingText,
      isFullscreen,
      dashboardUrl,
      onIframeLoad,
      onIframeError,
      retryLoad,
      refreshIframe,
      openInNewTab,
      toggleFullscreen
    }
  }
}
</script>

<style scoped>
.dashboard-iframe-container {
  position: relative;
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.iframe-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.dashboard-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background-color: #fff;
}

.iframe-toolbar {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 100;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
  padding: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(4px);
}

.iframe-toolbar .el-button-group {
  display: flex;
  gap: 4px;
}

/* 全屏模式样式 */
.dashboard-iframe-container:fullscreen {
  background-color: #000;
}

.dashboard-iframe-container:fullscreen .iframe-toolbar {
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
}

.dashboard-iframe-container:fullscreen .iframe-toolbar .el-button {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.dashboard-iframe-container:fullscreen .iframe-toolbar .el-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .iframe-toolbar {
    top: 5px;
    right: 5px;
    padding: 4px;
  }
  
  .iframe-toolbar .el-button {
    padding: 4px 8px;
    font-size: 12px;
  }
}

/* 加载动画优化 */
:deep(.el-loading-mask) {
  background-color: rgba(0, 0, 0, 0.8) !important;
}

:deep(.el-loading-text) {
  color: #fff !important;
  font-size: 16px;
}

:deep(.el-loading-spinner) {
  color: #409EFF !important;
}
</style>
