<template>
  <div v-if="modelValue" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content" :class="[size]">
      <div class="modal-header">
        <h3>{{ title }}</h3>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>
      <div class="modal-body">
        <slot></slot>
      </div>
      <div class="modal-footer" v-if="$slots.footer">
        <slot name="footer"></slot>
      </div>
      <div class="modal-footer" v-else-if="showDefaultFooter">
        <button class="cancel-btn" @click="closeModal">{{ cancelText }}</button>
        <button class="submit-btn" @click="confirm">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Modal',
  props: {
    modelValue: {
      type: Boolean,
      required: true
    },
    title: {
      type: String,
      default: '标题'
    },
    showDefaultFooter: {
      type: Boolean,
      default: true
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    confirmText: {
      type: String,
      default: '确定'
    },
    size: {
      type: String,
      default: 'medium',
      validator: value => ['small', 'medium', 'large'].includes(value)
    }
  },
  emits: ['update:modelValue', 'confirm', 'cancel'],
  setup(props, { emit }) {
    const closeModal = () => {
      emit('update:modelValue', false)
      emit('cancel')
    }

    const confirm = () => {
      emit('confirm')
    }

    return {
      closeModal,
      confirm
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  overflow: hidden;
}

.modal-content.small {
  width: 300px;
}

.modal-content.medium {
  width: 500px;
}

.modal-content.large {
  width: 800px;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-button {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #e8e8e8;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: #333;
}

.submit-btn {
  background-color: #42b983;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: white;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.submit-btn:hover {
  background-color: #3da776;
}
</style> 