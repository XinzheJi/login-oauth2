<template>
  <div class="user-manage-container">
    <h2>用户管理</h2>
    
    <!-- 操作按钮 -->
    <div class="action-bar">
      <button v-permission="'user:create'" @click="showAddUserForm = true" class="add-btn">添加用户</button>
    </div>
    
    <!-- 用户列表 -->
    <DataTable :columns="columns" :data="users">
      <template #roles="{ item }">
        <span v-for="(role, index) in item.roles" :key="role.id" class="role-tag">
          {{ role.name }}{{ index < item.roles.length - 1 ? ',' : '' }}
        </span>
      </template>
      
      <template #actions="{ item }">
        <ActionButtons
          edit-permission="user:update"
          delete-permission="user:delete"
          @edit="editUser(item)"
          @delete="confirmDelete(item)"
        >
          <button 
            v-permission="'user:assign-role'" 
            @click="showRoleAssign(item)" 
            class="role-btn"
          >
            分配角色
          </button>
        </ActionButtons>
      </template>
    </DataTable>
    
    <!-- 添加/编辑用户表单模态框 -->
    <Modal
      v-model="showFormModal"
      :title="showEditUserForm ? '编辑用户' : '添加用户'"
      @confirm="submitUserForm"
      @cancel="cancelUserForm"
    >
      <form @submit.prevent="submitUserForm">
        <FormGroup
          id="username"
          label="用户名"
          v-model="userForm.username"
          required
          :disabled="showEditUserForm"
        />
        <FormGroup
          id="name"
          label="姓名"
          v-model="userForm.name"
          required
        />
        <FormGroup
          id="password"
          label="密码"
          type="password"
          v-model="userForm.password"
          required
          v-if="showAddUserForm"
        />
        <FormGroup
          id="tenantId"
          label="租户ID"
          type="text"
          v-model="userForm.tenantId"
          required
        />
      </form>
    </Modal>
    
    <!-- 角色分配模态框 -->
    <Modal
      v-model="showRoleModal"
      title="分配角色"
      @confirm="saveUserRoles"
      @cancel="showRoleModal = false"
    >
      <div class="role-list">
        <div v-for="role in allRoles" :key="role.id" class="role-item">
          <input 
            type="checkbox" 
            :id="`role-${role.id}`" 
            :value="role.id" 
            v-model="selectedRoles"
            :checked="userHasRole(currentUser, role.id)"
          >
          <label :for="`role-${role.id}`">{{ role.name }}</label>
        </div>
      </div>
    </Modal>
    
    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model="showDeleteConfirm"
      title="确认删除"
      :message="`您确定要删除用户 '${currentUser.username}' 吗？此操作不可撤销。`"
      confirm-text="删除"
      type="danger"
      @confirm="deleteUserConfirmed"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllUsers, createUser, updateUser, deleteUser, assignRole, removeRole } from '@/api/user'
import { getAllRoles } from '@/api/role'
import { useUserStore } from '@/store/user'
import { DataTable, Modal, FormGroup, ConfirmDialog, ActionButtons } from '@/components/common'

export default {
  name: 'UserManageView',
  components: {
    DataTable,
    Modal,
    FormGroup,
    ConfirmDialog,
    ActionButtons
  },
  setup() {
    const userStore = useUserStore()
    const users = ref([])
    const allRoles = ref([])
    const showAddUserForm = ref(false)
    const showEditUserForm = ref(false)
    const showRoleModal = ref(false)
    const showDeleteConfirm = ref(false)
    const currentUser = ref({})
    const selectedRoles = ref([])
    
    // 计算属性：表单模态框是否显示
    const showFormModal = computed({
      get: () => showAddUserForm.value || showEditUserForm.value,
      set: (value) => {
        if (!value) {
          showAddUserForm.value = false
          showEditUserForm.value = false
        }
      }
    })
    
    // 表格列定义
    const columns = [
      { key: 'id', label: 'ID' },
      { key: 'username', label: '用户名' },
      { key: 'name', label: '姓名' },
      { key: 'tenantId', label: '租户ID' },
      { key: 'roles', label: '角色', slot: 'roles' },
      { key: 'actions', label: '操作', slot: 'actions' }
    ]
    
    const userForm = reactive({
      id: null,
      username: '',
      name: '',
      password: '',
      tenantId: userStore.userInfo.tenantId || null
    })
    
    // 获取所有用户
    const fetchUsers = async () => {
      try {
        const response = await getAllUsers()
        users.value = response
      } catch (error) {
        console.error('获取用户列表失败:', error)
        alert('获取用户列表失败: ' + error.message)
      }
    }
    
    // 获取所有角色
    const fetchRoles = async () => {
      try {
        const response = await getAllRoles()
        allRoles.value = response
      } catch (error) {
        console.error('获取角色列表失败:', error)
        alert('获取角色列表失败: ' + error.message)
      }
    }
    
    // 编辑用户
    const editUser = (user) => {
      currentUser.value = user
      userForm.id = user.id
      userForm.username = user.username
      userForm.name = user.name || ''
      userForm.tenantId = user.tenantId
      showEditUserForm.value = true
    }
    
    // 确认删除用户
    const confirmDelete = (user) => {
      currentUser.value = user
      showDeleteConfirm.value = true
    }
    
    // 执行删除用户
    const deleteUserConfirmed = async () => {
      try {
        await deleteUser(currentUser.value.id)
        showDeleteConfirm.value = false
        fetchUsers() // 刷新用户列表
        alert('用户删除成功')
      } catch (error) {
        console.error('删除用户失败:', error)
        alert('删除用户失败: ' + error.message)
      }
    }
    
    // 显示角色分配模态框
    const showRoleAssign = (user) => {
      currentUser.value = user
      selectedRoles.value = user.roles ? user.roles.map(role => role.id) : []
      showRoleModal.value = true
    }
    
    // 检查用户是否拥有某个角色
    const userHasRole = (user, roleId) => {
      if (!user.roles) return false
      return user.roles.some(role => role.id === roleId)
    }
    
    // 保存用户角色
    const saveUserRoles = async () => {
      try {
        // 获取用户当前角色ID列表
        const currentRoleIds = currentUser.value.roles ? 
          currentUser.value.roles.map(role => role.id) : []
        
        // 找出需要添加的角色
        const rolesToAdd = selectedRoles.value.filter(
          roleId => !currentRoleIds.includes(roleId)
        )
        
        // 找出需要移除的角色
        const rolesToRemove = currentRoleIds.filter(
          roleId => !selectedRoles.value.includes(roleId)
        )
        
        // 添加新角色
        for (const roleId of rolesToAdd) {
          await assignRole(currentUser.value.id, roleId)
        }
        
        // 移除旧角色
        for (const roleId of rolesToRemove) {
          await removeRole(currentUser.value.id, roleId)
        }
        
        showRoleModal.value = false
        fetchUsers() // 刷新用户列表
        alert('角色分配成功')
      } catch (error) {
        console.error('角色分配失败:', error)
        alert('角色分配失败: ' + error.message)
      }
    }
    
    // 提交用户表单
    const submitUserForm = async () => {
      try {
        // 验证表单数据
        if (!userForm.username) {
          alert('请输入用户名')
          return
        }
        if (!userForm.name) {
          alert('请输入姓名')
          return
        }
        if (showAddUserForm.value && !userForm.password) {
          alert('请输入密码')
          return
        }
        
        if (showAddUserForm.value) {
          // 创建新用户
          const response = await createUser(userForm)
          
          // 等待一小段时间，确保后端完成角色分配
          await new Promise(resolve => setTimeout(resolve, 500))
          
          alert('用户创建成功，已自动分配普通用户角色')
        } else {
          // 更新用户
          const { id, password, ...updateData } = userForm
          await updateUser(id, updateData)
          alert('用户更新成功')
        }
        
        // 重置表单并关闭模态框
        cancelUserForm()
        fetchUsers() // 刷新用户列表
      } catch (error) {
        console.error('保存用户失败:', error)
        alert('保存用户失败: ' + error.message)
      }
    }
    
    // 取消用户表单
    const cancelUserForm = () => {
      userForm.id = null
      userForm.username = ''
      userForm.name = ''
      userForm.password = ''
      userForm.tenantId = userStore.userInfo.tenantId || null
      showAddUserForm.value = false
      showEditUserForm.value = false
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchUsers()
      fetchRoles()
    })
    
    return {
      users,
      allRoles,
      userForm,
      currentUser,
      selectedRoles,
      showAddUserForm,
      showEditUserForm,
      showFormModal,
      showRoleModal,
      showDeleteConfirm,
      columns,
      editUser,
      confirmDelete,
      deleteUserConfirmed,
      showRoleAssign,
      userHasRole,
      saveUserRoles,
      submitUserForm,
      cancelUserForm
    }
  }
}
</script>

<style scoped>
.user-manage-container {
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

.role-tag {
  display: inline-block;
  background-color: #e0e0e0;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 5px;
  font-size: 12px;
}

.role-btn {
  background-color: #2196f3;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: white;
}

.role-list {
  max-height: 300px;
  overflow-y: auto;
}

.role-item {
  margin-bottom: 10px;
}

.role-item input {
  margin-right: 10px;
}
</style>