<template>
  <div class="tab-navigation-test">
    <h2>标签页跳转测试</h2>
    
    <div class="test-info">
      <h3>测试说明：</h3>
      <ul>
        <li>点击下方按钮访问不同页面</li>
        <li>观察标签页的创建和激活</li>
        <li>点击已存在的标签页，测试是否能正常跳转</li>
        <li>检查浏览器控制台的调试信息</li>
      </ul>
    </div>
    
    <div class="test-buttons">
      <button @click="goToRoute('/about')" class="test-btn">访问关于页面</button>
      <button @click="goToRoute('/users')" class="test-btn">访问用户管理</button>
      <button @click="goToRoute('/roles')" class="test-btn">访问角色管理</button>
      <button @click="goToRoute('/dashboard')" class="test-btn">访问数据大屏</button>
      <button @click="goToRoute('/health-check')" class="test-btn">访问健康检查</button>
    </div>
    
    <div class="current-info">
      <h3>当前状态：</h3>
      <p><strong>当前路由：</strong>{{ $route.path }}</p>
      <p><strong>当前时间：</strong>{{ currentTime }}</p>
    </div>
    
    <div class="test-steps">
      <h3>测试步骤：</h3>
      <ol>
        <li>点击上方按钮访问不同页面，创建多个标签页</li>
        <li>点击已存在的标签页，观察是否能正常跳转</li>
        <li>如果跳转失败，检查浏览器控制台的错误信息</li>
        <li>测试标签页的关闭功能</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TabNavigationTestView',
  data() {
    return {
      currentTime: ''
    }
  },
  mounted() {
    this.updateTime()
    setInterval(this.updateTime, 1000)
  },
  methods: {
    updateTime() {
      this.currentTime = new Date().toLocaleString()
    },
    goToRoute(path) {
      console.log('测试页面跳转:', path)
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.tab-navigation-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  color: #1890ff;
  margin-bottom: 20px;
  text-align: center;
}

.test-info, .current-info, .test-steps {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
  border: 1px solid #e9ecef;
}

.test-info h3, .current-info h3, .test-steps h3 {
  margin: 0 0 15px 0;
  color: #495057;
  font-size: 16px;
}

.test-info ul, .test-steps ol {
  margin: 0;
  padding-left: 20px;
}

.test-info li, .test-steps li {
  margin: 8px 0;
  color: #333;
  line-height: 1.5;
}

.test-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 20px 0;
  justify-content: center;
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
}

.test-btn:hover {
  background: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.current-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
}

.current-info strong {
  color: #333;
}
</style>
