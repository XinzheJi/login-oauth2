<template>
  <div class="role-manage-container">
    <h2>角色管理</h2>
    
    <!-- 操作按钮 -->
    <div class="action-bar">
      <button v-permission="'role:create'" @click="showAddRoleForm = true" class="add-btn">添加角色</button>
    </div>
    
    <!-- 角色列表 -->
    <DataTable :columns="columns" :data="roles">
      <template #actions="{ item }">
        <ActionButtons
          edit-permission="role:update"
          delete-permission="role:delete"
          @edit="editRole(item)"
          @delete="confirmDelete(item)"
        >
          <button 
            v-permission="'role:assign-permission'" 
            @click="showPermissionAssign(item)" 
            class="perm-btn"
          >
            分配权限
          </button>
        </ActionButtons>
      </template>
    </DataTable>
    
    <!-- 添加/编辑角色表单模态框 -->
    <Modal
      v-model="showFormModal"
      :title="showEditRoleForm ? '编辑角色' : '添加角色'"
      @confirm="submitRoleForm"
      @cancel="cancelRoleForm"
    >
      <form @submit.prevent="submitRoleForm">
        <FormGroup
          id="role-name"
          label="角色名称"
          v-model="roleForm.name"
          required
        />
        <FormGroup
          id="role-code"
          label="角色编码"
          v-model="roleForm.code"
          required
        />
        <FormGroup
          id="role-description"
          label="角色描述"
          v-model="roleForm.description"
        />
      </form>
    </Modal>
    
    <!-- 权限分配模态框 -->
    <Modal
      v-model="showPermissionModal"
      :title="`分配权限 - ${currentRole.name}`"
      @confirm="saveRolePermissions"
      @cancel="showPermissionModal = false"
    >
      <div class="permission-list">
        <div v-for="permission in allPermissions" :key="permission.id" class="permission-item">
          <input 
            type="checkbox" 
            :id="`permission-${permission.id}`" 
            :value="permission.id" 
            v-model="selectedPermissions"
          >
          <label :for="`permission-${permission.id}`">{{ permission.name }} ({{ permission.code }})</label>
        </div>
      </div>
    </Modal>
    
    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model="showDeleteConfirm"
      title="确认删除"
      :message="`您确定要删除角色 '${currentRole.name}' 吗？此操作不可撤销。`"
      confirm-text="删除"
      type="danger"
      @confirm="deleteRoleConfirmed"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllRoles, createRole, updateRole, deleteRole, getRolePermissions, assignPermissions } from '@/api/role'
import { DataTable, Modal, FormGroup, ConfirmDialog, ActionButtons } from '@/components/common'

export default {
  name: 'RoleManageView',
  components: {
    DataTable,
    Modal,
    FormGroup,
    ConfirmDialog,
    ActionButtons
  },
  setup() {
    const roles = ref([])
    const allPermissions = ref([
      { id: 1, name: '查看用户', code: 'user:view' },
      { id: 2, name: '创建用户', code: 'user:create' },
      { id: 3, name: '更新用户', code: 'user:update' },
      { id: 4, name: '删除用户', code: 'user:delete' },
      { id: 5, name: '分配角色', code: 'user:assign-role' },
      { id: 6, name: '移除角色', code: 'user:remove-role' },
      { id: 7, name: '查看角色', code: 'role:view' },
      { id: 8, name: '创建角色', code: 'role:create' },
      { id: 9, name: '更新角色', code: 'role:update' },
      { id: 10, name: '删除角色', code: 'role:delete' },
      { id: 11, name: '分配权限', code: 'role:assign-permission' }
    ])
    const showAddRoleForm = ref(false)
    const showEditRoleForm = ref(false)
    const showPermissionModal = ref(false)
    const showDeleteConfirm = ref(false)
    const currentRole = ref({})
    const selectedPermissions = ref([])
    
    // 计算属性：表单模态框是否显示
    const showFormModal = computed({
      get: () => showAddRoleForm.value || showEditRoleForm.value,
      set: (value) => {
        if (!value) {
          showAddRoleForm.value = false
          showEditRoleForm.value = false
        }
      }
    })
    
    // 表格列定义
    const columns = [
      { key: 'id', label: 'ID' },
      { key: 'name', label: '角色名称' },
      { key: 'code', label: '角色编码' },
      { key: 'description', label: '角色描述' },
      { key: 'actions', label: '操作', slot: 'actions' }
    ]
    
    const roleForm = reactive({
      id: null,
      name: '',
      code: '',
      description: ''
    })
    
    // 获取所有角色
    const fetchRoles = async () => {
      try {
        const response = await getAllRoles()
        roles.value = response
      } catch (error) {
        console.error('获取角色列表失败:', error)
        alert('获取角色列表失败: ' + error.message)
      }
    }
    
    // 编辑角色
    const editRole = (role) => {
      currentRole.value = role
      roleForm.id = role.id
      roleForm.name = role.name
      roleForm.code = role.code || ''
      roleForm.description = role.description || ''
      showEditRoleForm.value = true
    }
    
    // 确认删除角色
    const confirmDelete = (role) => {
      currentRole.value = role
      showDeleteConfirm.value = true
    }
    
    // 执行删除角色
    const deleteRoleConfirmed = async () => {
      try {
        await deleteRole(currentRole.value.id)
        showDeleteConfirm.value = false
        fetchRoles() // 刷新角色列表
        alert('角色删除成功')
      } catch (error) {
        console.error('删除角色失败:', error)
        alert('删除角色失败: ' + error.message)
      }
    }
    
    // 显示权限分配模态框
    const showPermissionAssign = async (role) => {
      currentRole.value = role
      try {
        // 获取角色当前权限
        const permissions = await getRolePermissions(role.id)
        selectedPermissions.value = permissions.map(p => p.id)
      } catch (error) {
        console.error('获取角色权限失败:', error)
        selectedPermissions.value = []
      }
      showPermissionModal.value = true
    }
    
    // 保存角色权限
    const saveRolePermissions = async () => {
      try {
        await assignPermissions(currentRole.value.id, selectedPermissions.value)
        showPermissionModal.value = false
        fetchRoles() // 刷新角色列表
        alert('权限分配成功')
      } catch (error) {
        console.error('权限分配失败:', error)
        alert('权限分配失败: ' + error.message)
      }
    }
    
    // 提交角色表单
    const submitRoleForm = async () => {
      try {
        if (showAddRoleForm.value) {
          // 创建新角色
          await createRole(roleForm)
          alert('角色创建成功')
        } else {
          // 更新角色
          await updateRole(roleForm.id, roleForm)
          alert('角色更新成功')
        }
        
        // 重置表单并关闭模态框
        cancelRoleForm()
        fetchRoles() // 刷新角色列表
      } catch (error) {
        console.error('保存角色失败:', error)
        alert('保存角色失败: ' + error.message)
      }
    }
    
    // 取消角色表单
    const cancelRoleForm = () => {
      roleForm.id = null
      roleForm.name = ''
      roleForm.code = ''
      roleForm.description = ''
      showAddRoleForm.value = false
      showEditRoleForm.value = false
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchRoles()
    })
    
    return {
      roles,
      allPermissions,
      roleForm,
      currentRole,
      selectedPermissions,
      showAddRoleForm,
      showEditRoleForm,
      showFormModal,
      showPermissionModal,
      showDeleteConfirm,
      columns,
      editRole,
      confirmDelete,
      deleteRoleConfirmed,
      showPermissionAssign,
      saveRolePermissions,
      submitRoleForm,
      cancelRoleForm
    }
  }
}
</script>

<style scoped>
.role-manage-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.add-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.perm-btn {
  background-color: #2196f3;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: white;
}

.permission-list {
  max-height: 300px;
  overflow-y: auto;
}

.permission-item {
  margin-bottom: 10px;
}

.permission-item input {
  margin-right: 10px;
}
</style>