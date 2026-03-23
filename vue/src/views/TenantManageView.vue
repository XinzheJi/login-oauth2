<template>
  <div class="tenant-manage-container">
    <h2>租户管理</h2>
    
    <!-- 操作按钮 -->
    <div class="action-bar">
      <button v-permission="'tenant:create'" @click="showAddTenantForm = true" class="add-btn">添加租户</button>
    </div>
    
    <!-- 租户列表 -->
    <DataTable :columns="columns" :data="tenants">
      <template #status="{ item }">
        <span :class="['status-badge', item.status === 'active' ? 'active' : 'inactive']">
          {{ item.status === 'active' ? '启用' : '禁用' }}
        </span>
      </template>
      
      <template #actions="{ item }">
        <ActionButtons
          edit-permission="tenant:update"
          delete-permission="tenant:delete"
          @edit="editTenant(item)"
          @delete="confirmDelete(item)"
        >
          <button 
            v-permission="'tenant:update'" 
            @click="toggleStatus(item)" 
            :class="['toggle-btn', item.status === 'active' ? 'deactivate' : 'activate']"
          >
            {{ item.status === 'active' ? '禁用' : '启用' }}
          </button>
        </ActionButtons>
      </template>
    </DataTable>
    
    <!-- 添加/编辑租户表单模态框 -->
    <Modal
      v-model="showFormModal"
      :title="showEditTenantForm ? '编辑租户' : '添加租户'"
      @confirm="submitTenantForm"
      @cancel="cancelTenantForm"
    >
      <form @submit.prevent="submitTenantForm">
        <FormGroup
          id="tenant-name"
          label="租户名称"
          v-model="tenantForm.name"
          required
        />
        <FormGroup
          id="tenant-code"
          label="租户代码"
          v-model="tenantForm.code"
          required
          :disabled="showEditTenantForm"
        />
        <FormGroup
          id="tenant-status"
          label="状态"
          type="select"
          v-model="tenantForm.status"
          :options="statusOptions"
        />
      </form>
    </Modal>
    
    <!-- 删除确认对话框 -->
    <ConfirmDialog
      v-model="showDeleteConfirm"
      title="确认删除"
      :message="`您确定要删除租户 '${currentTenant.name}' 吗？此操作不可撤销，并且会删除该租户下的所有用户和数据。`"
      confirm-text="删除"
      type="danger"
      @confirm="deleteTenantConfirmed"
    />
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllTenants, createTenant, updateTenant, deleteTenant, toggleTenantStatus } from '@/api/tenant'
import { useUserStore } from '@/store/user'
import { DataTable, Modal, FormGroup, ConfirmDialog, ActionButtons } from '@/components/common'

export default {
  name: 'TenantManageView',
  components: {
    DataTable,
    Modal,
    FormGroup,
    ConfirmDialog,
    ActionButtons
  },
  setup() {
    const userStore = useUserStore()
    const tenants = ref([])
    const showAddTenantForm = ref(false)
    const showEditTenantForm = ref(false)
    const showDeleteConfirm = ref(false)
    const currentTenant = ref({})
    
    // 计算属性：表单模态框是否显示
    const showFormModal = computed({
      get: () => showAddTenantForm.value || showEditTenantForm.value,
      set: (value) => {
        if (!value) {
          showAddTenantForm.value = false
          showEditTenantForm.value = false
        }
      }
    })
    
    // 表格列定义
    const columns = [
      { key: 'id', label: 'ID' },
      { key: 'name', label: '租户名称' },
      { key: 'code', label: '租户代码' },
      { key: 'status', label: '状态', slot: 'status' },
      { key: 'actions', label: '操作', slot: 'actions' }
    ]
    
    // 状态选项
    const statusOptions = [
      { value: 'active', label: '启用' },
      { value: 'inactive', label: '禁用' }
    ]
    
    const tenantForm = reactive({
      id: null,
      name: '',
      code: '',
      status: 'active'
    })
    
    // 获取所有租户
    const fetchTenants = async () => {
      try {
        // 检查用户是否有访问租户列表的权限
        if (!userStore.hasPermission('tenant:view')) {
          throw new Error('您没有访问租户列表的权限')
        }
        
        const response = await getAllTenants()
        // 转换后端字段名到前端字段名
        tenants.value = response.map(tenant => ({
          id: tenant.id,
          name: tenant.tenantName,
          code: tenant.tenantCode,
          status: tenant.status || 'active' // 默认为active
        }))
      } catch (error) {
        console.error('获取租户列表失败:', error)
        alert('获取租户列表失败: ' + error.message)
      }
    }
    
    // 编辑租户
    const editTenant = (tenant) => {
      currentTenant.value = tenant
      tenantForm.id = tenant.id
      tenantForm.name = tenant.name
      tenantForm.code = tenant.code
      tenantForm.status = tenant.status
      showEditTenantForm.value = true
    }
    
    // 确认删除租户
    const confirmDelete = (tenant) => {
      currentTenant.value = tenant
      showDeleteConfirm.value = true
    }
    
    // 执行删除租户
    const deleteTenantConfirmed = async () => {
      try {
        await deleteTenant(currentTenant.value.id)
        showDeleteConfirm.value = false
        fetchTenants() // 刷新租户列表
        alert('租户删除成功')
      } catch (error) {
        console.error('删除租户失败:', error)
        alert('删除租户失败: ' + error.message)
      }
    }
    
    // 切换租户状态
    const toggleStatus = async (tenant) => {
      try {
        const newStatus = tenant.status === 'active' ? 'inactive' : 'active'
        await toggleTenantStatus(tenant.id, newStatus)
        fetchTenants() // 刷新租户列表
        alert(`租户${newStatus === 'active' ? '启用' : '禁用'}成功`)
      } catch (error) {
        console.error('更新租户状态失败:', error)
        alert('更新租户状态失败: ' + error.message)
      }
    }
    
    // 提交租户表单
    const submitTenantForm = async () => {
      try {
        if (showAddTenantForm.value) {
          // 创建新租户
          await createTenant(tenantForm)
          alert('租户创建成功')
        } else {
          // 更新租户
          await updateTenant(tenantForm.id, tenantForm)
          alert('租户更新成功')
        }
        
        // 重置表单并关闭模态框
        cancelTenantForm()
        fetchTenants() // 刷新租户列表
      } catch (error) {
        console.error('保存租户失败:', error)
        alert('保存租户失败: ' + error.message)
      }
    }
    
    // 取消租户表单
    const cancelTenantForm = () => {
      tenantForm.id = null
      tenantForm.name = ''
      tenantForm.code = ''
      tenantForm.status = 'active'
      showAddTenantForm.value = false
      showEditTenantForm.value = false
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      // 只有管理员或有权限的用户可以查看所有租户
      if (userStore.userInfo.roles && 
          (userStore.userInfo.roles.includes('ADMIN') || 
           userStore.hasPermission('tenant:view'))) {
        fetchTenants()
      }
    })
    
    return {
      tenants,
      tenantForm,
      currentTenant,
      showAddTenantForm,
      showEditTenantForm,
      showFormModal,
      showDeleteConfirm,
      columns,
      statusOptions,
      editTenant,
      confirmDelete,
      deleteTenantConfirmed,
      toggleStatus,
      submitTenantForm,
      cancelTenantForm
    }
  }
}
</script>

<style scoped>
.tenant-manage-container {
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
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  color: white;
}

.add-btn:hover {
  background-color: #3da776;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.active {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-badge.inactive {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

.toggle-btn {
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  color: white;
}

.toggle-btn.activate {
  background-color: #52c41a;
}

.toggle-btn.deactivate {
  background-color: #faad14;
}

.toggle-btn.activate:hover {
  background-color: #389e0d;
}

.toggle-btn.deactivate:hover {
  background-color: #d48806;
}
</style>