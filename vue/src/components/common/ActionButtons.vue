<template>
  <div class="action-buttons">
    <button 
      v-if="showEdit && checkPermission(editPermission)" 
      @click="$emit('edit')" 
      class="edit-btn"
    >
      {{ editText }}
    </button>
    
    <button 
      v-if="showDelete && checkPermission(deletePermission)" 
      @click="$emit('delete')" 
      class="delete-btn"
    >
      {{ deleteText }}
    </button>
    
    <slot></slot>
  </div>
</template>

<script>
import { useUserStore } from '@/store/user';
import { inject } from 'vue';

export default {
  name: 'ActionButtons',
  props: {
    showEdit: {
      type: Boolean,
      default: true
    },
    showDelete: {
      type: Boolean,
      default: true
    },
    editPermission: {
      type: String,
      default: ''
    },
    deletePermission: {
      type: String,
      default: ''
    },
    editText: {
      type: String,
      default: '编辑'
    },
    deleteText: {
      type: String,
      default: '删除'
    }
  },
  emits: ['edit', 'delete'],
  setup() {
    const userStore = useUserStore();
    
    const checkPermission = (permission) => {
      if (!permission) return true;
      return userStore.hasPermission(permission);
    };
    
    return {
      checkPermission
    };
  }
};
</script>

<style scoped>
.action-buttons {
  display: flex;
  gap: 8px;
}

.edit-btn {
  background-color: #42b983;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: white;
}

.delete-btn {
  background-color: #e53935;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: white;
}

.edit-btn:hover {
  background-color: #3da776;
}

.delete-btn:hover {
  background-color: #d32f2f;
}
</style> 