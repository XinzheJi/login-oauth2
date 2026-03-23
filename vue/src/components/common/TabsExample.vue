<template>
  <div class="tabs-example">
    <h2>标签页组件使用示例</h2>
    
    <TabsContainer v-model="activeTab" @tab-change="handleTabChange" @tab-close="handleTabClose">
      <!-- 首页内容 -->
      <div v-if="activeTab === 'home'" class="tab-content">
        <h3>首页</h3>
        <p>这是首页内容</p>
        <button @click="addNewTab" class="add-tab-btn">添加新标签</button>
      </div>
      
      <!-- 动态标签内容 -->
      <div v-for="tab in dynamicTabs" :key="tab.id" v-show="activeTab === tab.id" class="tab-content">
        <h3>{{ tab.title }}</h3>
        <p>这是 {{ tab.title }} 的内容</p>
        <p>标签ID: {{ tab.id }}</p>
      </div>
    </TabsContainer>
  </div>
</template>

<script>
import TabsContainer from './TabsContainer.vue'

export default {
  name: 'TabsExample',
  components: {
    TabsContainer
  },
  data() {
    return {
      activeTab: 'home',
      dynamicTabs: [],
      tabCounter: 0
    }
  },
  methods: {
    addNewTab() {
      this.tabCounter++
      const newTab = {
        id: `tab-${this.tabCounter}`,
        title: `标签 ${this.tabCounter}`,
        closable: true,
        component: null
      }
      
      this.dynamicTabs.push(newTab)
      
      // 通过 provide/inject 访问父组件方法
      if (this.$parent && this.$parent.addTab) {
        this.$parent.addTab(newTab)
      }
    },
    
    handleTabChange(tabId) {
      console.log('切换到标签:', tabId)
    },
    
    handleTabClose(tabId) {
      console.log('关闭标签:', tabId)
      // 从动态标签列表中移除
      const index = this.dynamicTabs.findIndex(tab => tab.id === tabId)
      if (index > -1) {
        this.dynamicTabs.splice(index, 1)
      }
    }
  }
}
</script>

<style scoped>
.tabs-example {
  width: 100%;
  height: 500px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

h2 {
  margin: 0;
  padding: 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
  font-size: 18px;
  color: #333;
}

.tab-content {
  padding: 20px;
  height: calc(100% - 60px);
  overflow-y: auto;
}

.tab-content h3 {
  margin: 0 0 16px 0;
  color: #1890ff;
  font-size: 16px;
}

.tab-content p {
  margin: 8px 0;
  color: #666;
  line-height: 1.5;
}

.add-tab-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease;
}

.add-tab-btn:hover {
  background: #40a9ff;
}
</style>

