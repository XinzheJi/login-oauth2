<template>
  <div class="tabs-bar">
    <div class="tabs-wrapper" ref="tabsWrapper">
      <TabItem
        v-for="tab in tabs"
        :key="tab.id"
        :tab="tab"
        :active="activeTab === tab.id"
        @click="handleTabClick"
        @close="handleTabClose"
      />
    </div>
    <div class="tabs-actions">
      <button 
        class="action-btn" 
        @click="scrollLeft"
        :disabled="scrollLeftDisabled"
        title="向左滚动"
      >
        <i class="icon-left">‹</i>
      </button>
      <button 
        class="action-btn" 
        @click="scrollRight"
        :disabled="scrollRightDisabled"
        title="向右滚动"
      >
        <i class="icon-right">›</i>
      </button>
    </div>
  </div>
</template>

<script>
import TabItem from './TabItem.vue'

export default {
  name: 'TabsBar',
  components: {
    TabItem
  },
  props: {
    tabs: {
      type: Array,
      default: () => []
    },
    activeTab: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      scrollLeftDisabled: true,
      scrollRightDisabled: false
    }
  },
  mounted() {
    this.updateScrollButtons()
    window.addEventListener('resize', this.updateScrollButtons)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateScrollButtons)
  },
  methods: {
    handleTabClick(tabId) {
      this.$emit('tab-click', tabId)
    },
    
    handleTabClose(tabId) {
      this.$emit('tab-close', tabId)
    },
    
    scrollLeft() {
      if (this.$refs.tabsWrapper) {
        this.$refs.tabsWrapper.scrollBy({
          left: -200,
          behavior: 'smooth'
        })
        this.updateScrollButtons()
      }
    },
    
    scrollRight() {
      if (this.$refs.tabsWrapper) {
        this.$refs.tabsWrapper.scrollBy({
          left: 200,
          behavior: 'smooth'
        })
        this.updateScrollButtons()
      }
    },
    
    updateScrollButtons() {
      if (this.$refs.tabsWrapper) {
        const wrapper = this.$refs.tabsWrapper
        this.scrollLeftDisabled = wrapper.scrollLeft <= 0
        this.scrollRightDisabled = wrapper.scrollLeft >= wrapper.scrollWidth - wrapper.clientWidth
      }
    }
  },
  watch: {
    tabs: {
      handler() {
        this.$nextTick(() => {
          this.updateScrollButtons()
        })
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.tabs-bar {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #dee2e6;
  height: 44px;
  min-height: 44px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.tabs-wrapper {
  flex: 1;
  display: flex;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tabs-wrapper::-webkit-scrollbar {
  display: none;
}

.tabs-actions {
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-left: 1px solid #dee2e6;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  margin: 0 3px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  color: #6c757d;
}

.action-btn:hover:not(:disabled) {
  background: #1890ff;
  color: #fff;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.action-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  transform: none;
}

.icon-left,
.icon-right {
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s ease;
}

@media (max-width: 768px) {
  .tabs-bar {
    height: 40px;
    min-height: 40px;
  }
  
  .action-btn {
    width: 24px;
    height: 24px;
    margin: 0 2px;
  }
  
  .icon-left,
  .icon-right {
    font-size: 12px;
  }
  
  .tabs-actions {
    padding: 0 8px;
  }
}
</style>
