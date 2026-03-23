<template>
  <div class="simple-tab-test">
    <h2>顶部标签页跳转测试</h2>
    <p>当前页面：{{ $route.path }}</p>
    <p>当前时间：{{ currentTime }}</p>
    
    <div class="instructions">
      <h3>测试步骤：</h3>
      <ol>
        <li>点击左侧菜单访问不同页面，观察顶部标签页的创建</li>
        <li>点击顶部已存在的标签页，测试是否能正常跳转</li>
        <li>观察浏览器控制台的调试信息</li>
        <li>测试标签页的关闭功能</li>
      </ol>
    </div>
    
    <div class="quick-links">
      <h3>快速访问：</h3>
      <button @click="$router.push('/about')" class="link-btn">关于页面</button>
      <button @click="$router.push('/users')" class="link-btn">用户管理</button>
      <button @click="$router.push('/roles')" class="link-btn">角色管理</button>
      <button @click="$router.push('/dashboard')" class="link-btn">数据大屏</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleTabTestView',
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
    }
  }
}
</script>

<style scoped>
.simple-tab-test {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

h2 {
  color: #1890ff;
  text-align: center;
  margin-bottom: 20px;
}

p {
  margin: 10px 0;
  color: #666;
}

.instructions {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
}

.instructions h3 {
  margin: 0 0 15px 0;
  color: #495057;
}

.instructions ol {
  margin: 0;
  padding-left: 20px;
}

.instructions li {
  margin: 8px 0;
  color: #333;
  line-height: 1.5;
}

.quick-links {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 8px;
  padding: 20px;
  margin: 20px 0;
}

.quick-links h3 {
  margin: 0 0 15px 0;
  color: #1890ff;
}

.link-btn {
  display: inline-block;
  margin: 5px 10px 5px 0;
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.link-btn:hover {
  background: #40a9ff;
  transform: translateY(-1px);
}
</style>
