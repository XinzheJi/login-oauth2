<template>
  <div class="permission-manage-container">
    <!-- 页面标题和描述 -->
    <el-card class="page-header">
      <template #header>
        <div class="card-header">
          <h2>权限管理</h2>
          <div class="page-description">
            管理系统权限和权限分配，控制用户对系统功能的访问权限。
          </div>
        </div>
      </template>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-permission="'permission:create'" @click="showAddPermissionForm = true" type="primary">
          <el-icon><Plus /></el-icon>添加权限
        </el-button>
        <el-button v-permission="'permission:assign'" @click="showBatchRoleAssign" type="success">
          <el-icon><SwitchButton /></el-icon>批量分配角色
        </el-button>
      </div>
      
      <!-- 权限表格 -->
      <el-table
        :data="permissions"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="code" label="权限标识" width="150" />
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="description" label="权限描述" />
        <el-table-column label="已分配角色" width="200">
          <template #default="scope">
            <div class="assigned-roles">
              <el-tag
                v-for="role in getAssignedRoles(scope.row)"
                :key="role.id"
                size="small"
                class="mx-1"
                type="info"
                effect="light"
              >
                {{ role.name }}
              </el-tag>
              <span v-if="getAssignedRoles(scope.row).length === 0" class="no-roles">无</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button 
              v-permission="'permission:update'"
              @click="editPermission(scope.row)" 
              type="primary"
              size="small"
              text
            >
              编辑
            </el-button>
            <el-button 
              v-permission="'permission:assign'"
              @click="showRoleAssign(scope.row)" 
              type="success"
              size="small"
              text
            >
              分配角色
            </el-button>
            <el-button 
              v-permission="'permission:delete'"
              @click="confirmDelete(scope.row)" 
              type="danger"
              size="small"
              text
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="showEditPermissionForm ? '编辑权限' : '添加权限'"
      width="30%"
      destroy-on-close
    >
      <el-form :model="permissionForm" label-width="100px">
        <el-form-item label="权限标识">
          <el-input v-model="permissionForm.code" placeholder="请输入权限标识，例如：user:create" />
          <div class="field-hint">权限标识格式为: 资源:操作 (例如 user:create)</div>
        </el-form-item>
        <el-form-item label="权限名称">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限描述">
          <el-input v-model="permissionForm.description" placeholder="请输入权限描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelPermissionForm">取消</el-button>
          <el-button type="primary" @click="submitPermissionForm">
            {{ showEditPermissionForm ? '保存' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 角色分配对话框 -->
    <el-dialog
      v-model="showRoleModal"
      title="分配角色"
      width="30%"
      destroy-on-close
    >
      <div v-if="currentPermission.name" class="dialog-permission-name">
        当前权限: {{ currentPermission.name }}
      </div>
      <el-checkbox-group v-model="selectedRoles">
        <div class="role-checkboxes">
          <el-checkbox 
            v-for="role in allRoles" 
            :key="role.id" 
            :label="role.id"
          >
            {{ role.name }} <span class="role-desc">({{ role.description }})</span>
          </el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRoleModal = false">取消</el-button>
          <el-button type="primary" @click="savePermissionRoles">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 批量角色分配对话框 -->
    <el-dialog
      v-model="showBatchRoleModal"
      title="批量分配角色"
      width="30%"
      destroy-on-close
    >
      <el-alert
        v-if="selectedPermissions.length === 0"
        type="warning"
        title="请先选择要分配角色的权限"
        show-icon
      />
      <div v-else>
        <div class="selected-permissions-info">
          已选择 {{ selectedPermissions.length }} 个权限
        </div>
        <el-checkbox-group v-model="batchSelectedRoles">
          <div class="role-checkboxes">
            <el-checkbox 
              v-for="role in allRoles" 
              :key="role.id" 
              :label="role.id"
            >
              {{ role.name }} <span class="role-desc">({{ role.description }})</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <div class="role-assignment-mode">
          <el-radio-group v-model="assignmentMode">
            <el-radio label="append">追加模式 - 保留已有角色分配</el-radio>
            <el-radio label="replace">替换模式 - 覆盖已有角色分配</el-radio>
          </el-radio-group>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchRoleModal = false">取消</el-button>
          <el-button type="primary" @click="saveBatchPermissionRoles" :disabled="selectedPermissions.length === 0">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="showDeleteConfirm"
      title="确认删除"
      width="30%"
      destroy-on-close
    >
      <div class="delete-confirm-message">
        <el-icon class="warning-icon"><WarningFilled /></el-icon>
        <p>您确定要删除权限 "{{ currentPermission.name }}" 吗？此操作不可撤销。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDeleteConfirm = false">取消</el-button>
          <el-button type="danger" @click="deletePermission">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { Plus, SwitchButton, WarningFilled } from '@element-plus/icons-vue'
import { getAllPermissions, createPermission, updatePermission, deletePermission as apiDeletePermission, getPermissionRoles, assignRoles, getAllRoles } from '@/api/permission'
import { ElMessage } from 'element-plus'

export default {
  name: 'PermissionManageView',
  components: {
    Plus,
    SwitchButton,
    WarningFilled
  },
  setup() {
    const userStore = useUserStore()

    
    
    // 权限列表
    const permissions = ref([])
    
    // 所有角色
    const allRoles = ref([])
    
    // 权限-角色映射
    const permissionRoles = ref({})
    
    // 获取所有权限
    const fetchPermissions = async () => {
      try {
        const data = await getAllPermissions()
        permissions.value = data
        // 初始化权限-角色映射
        data.forEach(permission => {
          fetchPermissionRoles(permission.id)
        })
      } catch (error) {
        console.error('获取权限列表失败:', error)
        ElMessage.error('获取权限列表失败')
      }
    }
    
    // 获取权限的角色
    const fetchPermissionRoles = async (permissionId) => {
      try {
        const roles = await getPermissionRoles(permissionId)
        permissionRoles.value[permissionId] = roles.map(role => role.id)
      } catch (error) {
        console.error(`获取权限ID ${permissionId} 的角色失败:`, error)
      }
    }
    
    // 获取权限已分配的角色
    const getAssignedRoles = (permission) => {
      const roleIds = permissionRoles.value[permission.id] || []
      return allRoles.value.filter(role => roleIds.includes(role.id))
    }
    
    // 表单状态
    const showAddPermissionForm = ref(false)
    const showEditPermissionForm = ref(false)
    const showRoleModal = ref(false)
    const showBatchRoleModal = ref(false)
    const showDeleteConfirm = ref(false)
    
    // 对话框显示状态
    const dialogVisible = computed(() => showAddPermissionForm.value || showEditPermissionForm.value)
    
    // 当前操作的权限
    const currentPermission = ref({})
    
    // 权限表单
    const permissionForm = reactive({
      id: null,
      code: '',
      name: '',
      description: ''
    })
    
    // 批量操作相关
    const selectedPermissions = ref([])
    const batchSelectedRoles = ref([])
    const assignmentMode = ref('append')
    
    // 处理表格选择变化
    const handleSelectionChange = (val) => {
      selectedPermissions.value = val.map(item => item.id)
    }
    
    // 选中的角色
    const selectedRoles = ref([])
    
    // 编辑权限
    const editPermission = (permission) => {
      currentPermission.value = permission
      Object.assign(permissionForm, permission)
      showEditPermissionForm.value = true
    }
    
    // 取消表单
    const cancelPermissionForm = () => {
      showAddPermissionForm.value = false
      showEditPermissionForm.value = false
      Object.assign(permissionForm, {
        id: null,
        code: '',
        name: '',
        description: ''
      })
    }
    
    // 提交表单
    const submitPermissionForm = async () => {
      try {
        if (showEditPermissionForm.value) {
          // 更新权限
          await updatePermission(permissionForm.id, permissionForm)
          ElMessage.success('更新权限成功')
        } else {
          // 添加权限
          await createPermission(permissionForm)
          ElMessage.success('添加权限成功')
        }
        // 重新获取权限列表
        fetchPermissions()
        // 重置表单
        cancelPermissionForm()
      } catch (error) {
        console.error('提交权限表单失败:', error)
        ElMessage.error('操作失败: ' + error.message)
      }
    }
    
    // 确认删除
    const confirmDelete = (permission) => {
      currentPermission.value = permission
      showDeleteConfirm.value = true
    }
    
    // 删除权限
    const deletePermission = async () => {
      try {
        await apiDeletePermission(currentPermission.value.id)
        ElMessage.success('删除权限成功')
        // 重新获取权限列表
        fetchPermissions()
        showDeleteConfirm.value = false
      } catch (error) {
        console.error('删除权限失败:', error)
        ElMessage.error('删除失败: ' + error.message)
      }
    }
    
    // 显示角色分配
    const showRoleAssign = async (permission) => {
      currentPermission.value = permission
      try {
        // 获取权限当前角色
        const roles = await getPermissionRoles(permission.id)
        selectedRoles.value = roles.map(role => role.id)
        showRoleModal.value = true
      } catch (error) {
        console.error('获取权限角色失败:', error)
        ElMessage.error('获取角色信息失败')
        selectedRoles.value = []
      }
    }
    
    // 保存权限角色分配
    const savePermissionRoles = async () => {
      try {
        // 保存权限角色分配
        await assignRoles(currentPermission.value.id, selectedRoles.value)
        ElMessage.success('角色分配成功')
        // 更新本地数据
        permissionRoles.value[currentPermission.value.id] = [...selectedRoles.value]
        showRoleModal.value = false
      } catch (error) {
        console.error('保存权限角色失败:', error)
        ElMessage.error('角色分配失败: ' + error.message)
      }
    }
    
    // 显示批量角色分配
    const showBatchRoleAssign = () => {
      batchSelectedRoles.value = []
      showBatchRoleModal.value = true
    }
    
    // 保存批量权限角色分配
    const saveBatchPermissionRoles = async () => {
      if (selectedPermissions.value.length === 0) return
      
      try {
        const promises = selectedPermissions.value.map(permissionId => {
          let roleIds = [...batchSelectedRoles.value]
          
          if (assignmentMode.value === 'append') {
            // 追加模式
            const currentRoles = permissionRoles.value[permissionId] || []
            roleIds = [...new Set([...currentRoles, ...batchSelectedRoles.value])]
          }
          
          // 调用API保存角色分配
          return assignRoles(permissionId, roleIds).then(() => {
            // 更新本地数据
            permissionRoles.value[permissionId] = [...roleIds]
          })
        })
        
        await Promise.all(promises)
        ElMessage.success('批量角色分配成功')
        showBatchRoleModal.value = false
      } catch (error) {
        console.error('批量分配角色失败:', error)
        ElMessage.error('批量角色分配失败: ' + error.message)
      }
    }
    
    // 组件挂载时获取数据
    onMounted(async () => {
      // 获取所有权限
      await fetchPermissions()
      
      // 获取所有角色
      try {
        const data = await getAllRoles() // 使用正确的API获取所有角色
        allRoles.value = data
      } catch (error) {
        console.error('获取角色列表失败:', error)
        // 使用默认角色数据 (如果需要)
        // allRoles.value = [
        //   { id: 1, name: 'ADMIN', description: '系统管理员' },
        //   { id: 2, name: 'USER', description: '普通用户' },
        //   { id: 3, name: 'GUEST', description: '访客' },
        // ]
        ElMessage.error('获取角色列表失败')
      }
    })
    
    return {
      permissions,
      allRoles,
      showAddPermissionForm,
      showEditPermissionForm,
      showRoleModal,
      showBatchRoleModal,
      showDeleteConfirm,
      currentPermission,
      permissionForm,
      selectedRoles,
      selectedPermissions,
      batchSelectedRoles,
      assignmentMode,
      getAssignedRoles,
      handleSelectionChange,
      editPermission,
      cancelPermissionForm,
      submitPermissionForm,
      confirmDelete,
      deletePermission,
      showRoleAssign,
      savePermissionRoles,
      showBatchRoleAssign,
      saveBatchPermissionRoles,
      dialogVisible
    }
  }
}
</script>

<style scoped>
.permission-manage-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.card-header {
  margin-bottom: 15px;
}

.card-header h2 {
  margin: 0 0 5px 0;
}

.page-description {
  color: #666;
  margin-top: 5px;
}

.action-bar {
  margin: 15px 0;
  display: flex;
  gap: 10px;
}

.assigned-roles {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.no-roles {
  color: #909399;
  font-style: italic;
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-permission-name {
  font-weight: bold;
  margin-bottom: 15px;
  color: #409EFF;
}

.role-checkboxes {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 300px;
  overflow-y: auto;
  padding: 5px;
}

.role-desc {
  font-size: 13px;
  color: #909399;
}

.role-assignment-mode {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.selected-permissions-info {
  margin-bottom: 15px;
  color: #409EFF;
  font-weight: bold;
}

.delete-confirm-message {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
}

.warning-icon {
  font-size: 24px;
  color: #E6A23C;
}

.delete-confirm-message p {
  margin: 0;
  color: #606266;
}
</style>