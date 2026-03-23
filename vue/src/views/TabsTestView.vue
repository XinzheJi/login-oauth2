<template>
  <div class="tabs-test">
    <h2>标签页功能测试</h2>
    <p>当前路由: {{ $route.path }}</p>
    <p>当前时间: {{ currentTime }}</p>
    
    <div class="test-buttons">
      <button @click="goToRoute('/about')" class="test-btn">跳转到关于页面</button>
      <button @click="goToRoute('/users')" class="test-btn">跳转到用户管理</button>
      <button @click="goToRoute('/roles')" class="test-btn">跳转到角色管理</button>
      <button @click="goToRoute('/dashboard')" class="test-btn">跳转到数据大屏</button>
      <button @click="goToRoute('/power-devices')" class="test-btn">跳转到电源台账</button>
      <button @click="goToRoute('/health-check')" class="test-btn">跳转到健康检查</button>
    </div>
    
    <div class="test-info">
      <h3>标签页交互功能测试：</h3>
      <ul>
        <li><strong>✅ 标签点击切换：</strong>点击标签可以切换到对应页面</li>
        <li><strong>✅ 标签关闭功能：</strong>点击标签上的 × 按钮可以关闭标签</li>
        <li><strong>✅ 激活状态高亮：</strong>当前激活的标签会显示蓝色背景和白色文字</li>
        <li><strong>✅ 首页标签保护：</strong>首页标签不显示关闭按钮，无法关闭</li>
        <li><strong>✅ 智能标签切换：</strong>关闭当前标签时自动切换到相邻标签</li>
        <li><strong>✅ 本地状态管理：</strong>使用数组存储标签页状态，无需复杂状态管理</li>
      </ul>
      
      <h3>样式效果测试：</h3>
      <ul>
        <li><strong>🎨 激活标签：</strong>蓝色主题背景 (#1890ff)，白色文字，阴影效果</li>
        <li><strong>🎨 非激活标签：</strong>浅灰色背景 (#f5f5f5)，悬停时变为浅蓝色</li>
        <li><strong>🎨 圆角设计：</strong>8px 圆角，现代化外观</li>
        <li><strong>🎨 悬停效果：</strong>悬停时显示关闭按钮，标签轻微上移</li>
        <li><strong>🎨 切换动画：</strong>平滑的过渡动画，激活时有弹跳效果</li>
        <li><strong>🎨 响应式设计：</strong>移动端适配，深色主题支持</li>
      </ul>
      
      <h3>测试步骤：</h3>
      <ol>
        <li>点击上方按钮，观察标签页的创建和激活动画</li>
        <li>悬停在标签上，观察关闭按钮的显示效果</li>
        <li>点击不同标签，观察页面切换和高亮效果</li>
        <li>尝试关闭首页标签，应该无法关闭</li>
        <li>关闭其他标签，观察智能切换效果</li>
        <li>重复点击同一按钮，应该激活已存在的标签</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TabsTestView',
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
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.tabs-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  color: #1890ff;
  margin-bottom: 20px;
}

p {
  margin: 10px 0;
  color: #666;
}

.test-buttons {
  margin: 20px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.test-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease;
}

.test-btn:hover {
  background: #40a9ff;
}

.test-info {
  margin-top: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.test-info h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.test-info ul {
  margin: 0;
  padding-left: 20px;
}

.test-info li {
  margin: 8px 0;
  color: #666;
  line-height: 1.5;
}
</style>
