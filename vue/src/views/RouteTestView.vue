<template>
  <div class="route-test-view">
    <h2>路由跳转测试</h2>
    
    <div class="current-info">
      <h3>当前页面信息：</h3>
      <p><strong>路由路径：</strong>{{ $route.path }}</p>
      <p><strong>路由名称：</strong>{{ $route.name }}</p>
      <p><strong>页面标题：</strong>{{ pageTitle }}</p>
      <p><strong>当前时间：</strong>{{ currentTime }}</p>
    </div>
    
    <div class="test-buttons">
      <h3>测试跳转：</h3>
      <button @click="goToRoute('/about')" class="test-btn">关于页面</button>
      <button @click="goToRoute('/users')" class="test-btn">用户管理</button>
      <button @click="goToRoute('/roles')" class="test-btn">角色管理</button>
      <button @click="goToRoute('/permissions')" class="test-btn">权限管理</button>
      <button @click="goToRoute('/dashboard')" class="test-btn">数据大屏</button>
    </div>
    
    <div class="instructions">
      <h3>测试说明：</h3>
      <ol>
        <li>点击上方按钮访问不同页面</li>
        <li>观察顶部标签页的创建和激活</li>
        <li>点击已存在的标签页，测试页面内容是否跳转</li>
        <li>检查浏览器控制台的调试信息</li>
        <li>如果页面内容没有跳转，说明路由跳转有问题</li>
      </ol>
    </div>
    
    <div class="debug-info">
      <h3>调试信息：</h3>
      <p>如果标签页能切换但页面内容不跳转，可能的原因：</p>
      <ul>
        <li>路由跳转被阻止或失败</li>
        <li>组件没有正确响应路由变化</li>
        <li>路由守卫阻止了跳转</li>
        <li>Vue Router 配置有问题</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RouteTestView',
  data() {
    return {
      currentTime: '',
      pageTitle: '路由测试页面'
    }
  },
  mounted() {
    this.updateTime()
    setInterval(this.updateTime, 1000)
    console.log('RouteTestView 已挂载')
  },
  methods: {
    updateTime() {
      this.currentTime = new Date().toLocaleString()
    },
    goToRoute(path) {
      console.log('RouteTestView 跳转到:', path)
      this.$router.push(path)
    }
  },
  watch: {
    '$route'(newRoute, oldRoute) {
      console.log('RouteTestView 路由变化:', {
        from: oldRoute.path,
        to: newRoute.path,
        name: newRoute.name
      })
    }
  }
}
</script>

<style scoped>
.route-test-view {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  color: #1890ff;
  text-align: center;
  margin-bottom: 30px;
}

.current-info, .test-buttons, .instructions, .debug-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  border: 1px solid #e9ecef;
}

.current-info h3, .test-buttons h3, .instructions h3, .debug-info h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.current-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.current-info strong {
  color: #333;
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.test-btn {
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  align-self: flex-start;
}

.test-btn:hover {
  background: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.instructions ol, .debug-info ul {
  margin: 0;
  padding-left: 20px;
}

.instructions li, .debug-info li {
  margin: 8px 0;
  color: #333;
  line-height: 1.5;
}
</style>
