<template>
  <div class="tabs-container">
    <TabsBar 
      :tabs="tabs" 
      :active-tab="activeTab"
      @tab-click="handleTabClick"
      @tab-close="handleTabClose"
    />
    <div class="tabs-content">
      <slot></slot>
    </div>
  </div>
</template>

<script>
import TabsBar from './TabsBar.vue'

export default {
  name: 'TabsContainer',
  components: {
    TabsBar
  },
  props: {
    modelValue: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      tabs: [],
      activeTab: ''
    }
  },
  mounted() {
    // 初始化时添加综合监控标签
    this.addTab({
      id: '/dashboard',
      title: '综合监控',
      path: '/dashboard',
      closable: false,
      component: null
    })
    this.activeTab = '/dashboard'
    
    // 监听路由变化，自动添加标签
    this.$watch('$route', (newRoute) => {
      console.log('路由变化:', newRoute.path)
      
      // 更新激活标签
      this.activeTab = newRoute.path
      this.$emit('update:modelValue', newRoute.path)
      
      // 检查标签是否存在，如果不存在则添加
      const existingTab = this.tabs.find(tab => tab.id === newRoute.path)
      if (!existingTab && newRoute.path !== '/' && newRoute.path !== '/login' && newRoute.path !== '/dashboard') {
        console.log('添加新标签:', newRoute.path)
        this.addTabFromRoute(newRoute)
      } else if (existingTab) {
        console.log('激活已存在的标签:', newRoute.path)
      }
    }, { immediate: true })
  },
  methods: {
    addTab(tab) {
      // 检查标签是否已存在
      const existingTab = this.tabs.find(t => t.id === tab.id)
      if (!existingTab) {
        // 确保标签页对象包含path字段
        const tabWithPath = {
          ...tab,
          path: tab.path || tab.id // 如果没有path，使用id作为path
        }
        this.tabs.push(tabWithPath)
        console.log('添加标签页:', tabWithPath)
      }
      this.activeTab = tab.id
      this.$emit('update:modelValue', tab.id)
    },
    
    removeTab(tabId) {
      const index = this.tabs.findIndex(t => t.id === tabId)
      if (index > -1) {
        this.tabs.splice(index, 1)
        
        // 如果关闭的是当前激活标签，切换到其他标签
        if (this.activeTab === tabId) {
          if (this.tabs.length > 0) {
            // 优先切换到右侧标签，否则切换到左侧标签
            const nextIndex = index < this.tabs.length ? index : index - 1
            this.activeTab = this.tabs[nextIndex].id
            this.$emit('update:modelValue', this.activeTab)
            
            // 跳转到新的激活标签
            if (this.$router) {
              this.$router.push(this.activeTab)
            }
          } else {
            this.activeTab = ''
            this.$emit('update:modelValue', '')
            
            // 如果没有标签了，跳转到综合监控
            if (this.$router) {
              this.$router.push('/dashboard')
            }
          }
        }
      }
    },
    
    handleTabClick(tabId) {
      console.log('=== TabsContainer标签点击处理 ===')
      console.log('点击的标签ID:', tabId)
      console.log('当前路由:', this.$route.path)
      console.log('当前激活标签:', this.activeTab)
      
      // 找到对应的标签页对象
      const tab = this.tabs.find(t => t.id === tabId)
      if (!tab) {
        console.warn('未找到对应的标签页:', tabId)
        return
      }
      
      // 如果点击的是当前激活的标签，不需要跳转
      if (tabId === this.$route.path) {
        console.log('点击的是当前激活标签，无需跳转')
        return
      }
      
      // 更新激活状态（路由跳转由TabItem组件处理）
      this.activeTab = tabId
      this.$emit('update:modelValue', tabId)
      this.$emit('tab-change', tabId)
      
      console.log('=== TabsContainer标签点击处理完成 ===')
    },
    
    handleTabClose(tabId) {
      // 检查是否为综合监控标签，综合监控标签不可关闭
      if (tabId === '/dashboard') {
        console.log('综合监控标签不可关闭')
        return
      }
      
      console.log('关闭标签:', tabId)
      this.removeTab(tabId)
      this.$emit('tab-close', tabId)
    },
    
    addTabFromRoute(route) {
      const tabId = route.path
      const tabTitle = this.getTabTitle(route)
      
      this.addTab({
        id: tabId,
        title: tabTitle,
        path: tabId,
        closable: tabId !== '/dashboard',
        component: null
      })
    },
    
    getTabTitle(route) {
      // 根据路由路径返回对应的标题
      const titleMap = {
        '/': '首页',
        '/home': '系统概述',
        '/about': '关于',
        '/dashboard': '综合监控',
        '/users': '用户管理',
        '/roles': '角色管理',
        '/permissions': '权限管理',
        '/tenants': '租户管理',
        '/health-check': '系统健康检查',
        '/power-devices': '电源台账管理',
        '/power-alarms': '电源告警管理',
        '/power-alarm-statistics': '告警统计分析',
        '/switch-list': '交换机管理',
        '/model-type': '设备型号管理',
        '/device-aging-fault': '设备老化与故障感知',
        '/page-status': '页面状态检查'
      }
      return titleMap[route.path] || route.meta?.title || route.name || '未知页面'
    }
  },
  
  provide() {
    return {
      tabsContainer: this
    }
  }
}
</script>

<style scoped>
.tabs-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tabs-content {
  flex: 1;
  overflow: hidden;
  background: #fff;
  border-radius: 0 0 8px 8px;
}

/* 确保内容区域有适当的内边距 */
.tabs-content :deep(*) {
  box-sizing: border-box;
}
</style>