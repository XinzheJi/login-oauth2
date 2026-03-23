<template>
  <Modal 
    v-model="isOpen"
    :title="title"
    :show-default-footer="false"
    size="small"
    @cancel="onCancel"
  >
    <div class="confirm-content">
      <p>{{ message }}</p>
    </div>
    <template #footer>
      <button 
        class="btn-cancel" 
        :disabled="loading" 
        @click="onCancel"
      >
        {{ cancelText }}
      </button>
      <button 
        class="btn-confirm" 
        :class="{ 'btn-danger': type === 'danger' }"
        :disabled="loading" 
        @click="onConfirm"
      >
        <span v-if="loading" class="loading"></span>
        {{ confirmText }}
      </button>
    </template>
  </Modal>
</template>

<script>
import { ref, watch } from 'vue';
import Modal from './Modal.vue';

export default {
  name: 'ConfirmDialog',
  components: {
    Modal
  },
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '确认'
    },
    message: {
      type: String,
      default: '您确定要执行此操作吗？'
    },
    confirmText: {
      type: String,
      default: '确认'
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    type: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'danger'].includes(value)
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'confirm', 'cancel'],
  setup(props, { emit }) {
    const isOpen = ref(props.modelValue);
    
    watch(() => props.modelValue, (val) => {
      isOpen.value = val;
    });
    
    watch(isOpen, (val) => {
      emit('update:modelValue', val);
    });
    
    const onConfirm = () => {
      emit('confirm');
    };
    
    const onCancel = () => {
      isOpen.value = false;
      emit('cancel');
    };
    
    return {
      isOpen,
      onConfirm,
      onCancel
    };
  }
};
</script>

<style scoped>
.confirm-content {
  margin: 10px 0;
  text-align: center;
}

.btn-cancel {
  background-color: #f0f0f0;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: #333;
}

.btn-confirm {
  background-color: #42b983;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  margin-left: 8px;
  font-size: 14px;
  cursor: pointer;
  color: white;
  position: relative;
}

.btn-danger {
  background-color: #e53935;
}

.btn-cancel:hover {
  background-color: #e0e0e0;
}

.btn-confirm:hover {
  background-color: #3da776;
}

.btn-danger:hover {
  background-color: #d32f2f;
}

.btn-cancel:disabled,
.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid #fff;
  border-radius: 50%;
  border-top-color: transparent;
  animation: spin 1s linear infinite;
  margin-right: 6px;
}

@keyframes spin {
  to { transform: rotate(360deg) }
}
</style> 