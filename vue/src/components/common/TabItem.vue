<template>
  <div 
    class="tab-item"
    :class="{ 'active': active, 'closable': tab.closable }"
    @click="handleClick"
  >
    <div class="tab-content">
      <span class="tab-title" :title="tab.title">{{ tab.title }}</span>
      <button 
        v-if="tab.closable"
        class="close-btn"
        @click.stop="handleClose"
        title="关闭标签"
      >
        <i class="icon-close">×</i>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TabItem',
  props: {
    tab: {
      type: Object,
      required: true,
      validator: (value) => {
        return value && typeof value.id === 'string' && typeof value.title === 'string'
      }
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.tab.id)
    },
    
    handleClose() {
      this.$emit('close', this.tab.id)
    }
  }
}
</script>

<style scoped>
.tab-item {
  display: flex;
  align-items: center;
  min-width: 120px;
  max-width: 200px;
  height: 40px;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  margin-right: 4px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tab-item:hover {
  background: #e8f4fd;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.tab-item.active {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
  z-index: 2;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: #1890ff;
  border-radius: 0 0 2px 2px;
}

.tab-content {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 16px;
  height: 100%;
  position: relative;
}

.tab-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
  line-height: 1.4;
  font-weight: 500;
  transition: all 0.2s ease;
}

.tab-item.active .tab-title {
  font-weight: 600;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: none;
  background: transparent;
  border-radius: 50%;
  cursor: pointer;
  margin-left: 8px;
  transition: all 0.2s ease;
  opacity: 0;
  transform: scale(0.8);
}

.tab-item:hover .close-btn {
  opacity: 0.7;
  transform: scale(1);
}

.close-btn:hover {
  background: #ff4d4f;
  color: #fff;
  opacity: 1;
  transform: scale(1.1);
}

.icon-close {
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
}

.tab-item.closable .tab-content {
  padding-right: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tab-item {
    min-width: 100px;
    max-width: 150px;
    height: 36px;
    border-radius: 6px 6px 0 0;
  }
  
  .tab-content {
    padding: 0 12px;
  }
  
  .tab-title {
    font-size: 13px;
  }
  
  .close-btn {
    width: 16px;
    height: 16px;
    margin-left: 6px;
  }
  
  .icon-close {
    font-size: 10px;
  }
}

/* 深色主题支持 */
@media (prefers-color-scheme: dark) {
  .tab-item {
    background: #2a2a2a;
    border-color: #404040;
    color: #e0e0e0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
  
  .tab-item:hover {
    background: #3a3a3a;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  }
  
  .tab-item.active {
    background: #1890ff;
    border-color: #1890ff;
    color: #fff;
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
  }
}

/* 动画效果 */
.tab-item {
  animation: slideIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

/* 标签切换动画 */
.tab-item.active {
  animation: activateTab 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes activateTab {
  0% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-3px) scale(1.02);
  }
  100% {
    transform: translateY(-2px) scale(1);
  }
}

/* 关闭按钮动画 */
.close-btn {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-item:hover .close-btn {
  animation: fadeInScale 0.2s ease-out;
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 0.7;
    transform: scale(1);
  }
}
</style>
