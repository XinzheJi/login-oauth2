<template>
  <div class="power-device-manage-container">
    <h2>电源台账管理</h2>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="form-inline">
        <el-form-item label="设备名称">
          <el-input v-model="searchForm.deviceName" placeholder="设备名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ipAddress" placeholder="IP地址" clearable></el-input>
        </el-form-item>
        <el-form-item label="设备位置">
          <el-input v-model="searchForm.location" placeholder="设备位置" clearable></el-input>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="searchForm.deviceType" placeholder="请选择" clearable>
            <el-option
              v-for="item in deviceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="安装日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button v-permission="'power:device'" type="primary" @click="handleAdd" class="add-btn">
        <el-icon><Plus /></el-icon>添加设备
      </el-button>
    </div>
    
    <!-- 设备列表 -->
    <el-table
      v-loading="loading"
      :data="deviceList"
      border
      style="width: 100%">
      <el-table-column prop="deviceId" label="设备ID" width="120"></el-table-column>
      <el-table-column prop="deviceName" label="设备名称" min-width="150"></el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" min-width="150"></el-table-column>
      <el-table-column prop="location" label="设备位置" min-width="150"></el-table-column>
      <el-table-column prop="deviceType" label="设备类型" width="120"></el-table-column>
      <el-table-column prop="installDate" label="安装日期" width="160"></el-table-column>
      <el-table-column prop="tenantName" label="所属租户" width="120"></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button v-permission="'power:device'" type="primary" link @click="handleView(scope.row)" size="small">
            查看
          </el-button>
          <el-button v-permission="'power:device'" type="primary" link @click="handleEdit(scope.row)" size="small">
            编辑
          </el-button>
          <el-button v-permission="'power:device'" type="danger" link @click="handleDelete(scope.row)" size="small">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页器 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>
    
    <!-- 设备表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加设备' : dialogType === 'edit' ? '编辑设备' : '查看设备'"
      width="50%"
      :close-on-click-modal="false">
      <el-form
        ref="deviceFormRef"
        :model="deviceForm"
        :rules="formRules"
        label-width="100px"
        :disabled="dialogType === 'view'">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称"></el-input>
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="deviceForm.ipAddress" placeholder="请输入IP地址"></el-input>
        </el-form-item>
        <el-form-item label="设备位置" prop="location">
          <el-input v-model="deviceForm.location" placeholder="请输入设备位置"></el-input>
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="deviceForm.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option
              v-for="item in deviceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="安装日期" prop="installDate">
          <el-date-picker
            v-model="deviceForm.installDate"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="deviceForm.remark"
            type="textarea"
            placeholder="请输入备注"
            :rows="3">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="dialogType !== 'view'" type="primary" @click="submitForm">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%">
      <span>确定要删除设备"{{ currentDevice.deviceName }}"吗？此操作不可撤销。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { getDeviceList, getDeviceById, createDevice, updateDevice, deleteDevice } from '@/api/powerDevice'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const deviceList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add', 'edit', 'view'
const deviceFormRef = ref(null)
const deleteDialogVisible = ref(false)
const currentDevice = ref({})

// 日期范围选择器的数据
const dateRange = ref([])

// 设备类型选项
const deviceTypeOptions = [
  { value: 'UPS', label: 'UPS' },
  { value: 'PDU', label: 'PDU' },
  { value: '智能插座', label: '智能插座' },
  { value: '电源模块', label: '电源模块' },
  { value: '蓄电池组', label: '蓄电池组' },
  { value: '配电柜', label: '配电柜' },
  { value: '其他', label: '其他' }
]

// 搜索表单
const searchForm = reactive({
  deviceName: '',
  ipAddress: '',
  location: '',
  deviceType: '',
  installDateStart: '',
  installDateEnd: '',
  pageNum: 1,
  pageSize: 10
})

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    searchForm.installDateStart = val[0]
    searchForm.installDateEnd = val[1]
  } else {
    searchForm.installDateStart = ''
    searchForm.installDateEnd = ''
  }
})

// 设备表单
const deviceForm = reactive({
  deviceId: '',
  deviceName: '',
  ipAddress: '',
  location: '',
  deviceType: '',
  installDate: '',
  remark: ''
})

// 表单校验规则
const formRules = {
  deviceName: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入设备位置', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  installDate: [
    { required: true, message: '请选择安装日期', trigger: 'change' }
  ]
}

// 获取设备列表
const fetchDeviceList = async () => {
  loading.value = true;
  try {
    const queryParams = {
      ...searchForm,
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };
    console.log('发送查询参数:', queryParams);

    const apiResponseData = await getDeviceList(queryParams); 
    console.log('API直接返回的数据 (expected {list, total,...}):', apiResponseData);

    // 检查响应是否具有预期结构，特别是 'list'
    if (apiResponseData && typeof apiResponseData === 'object' && Array.isArray(apiResponseData.list)) {
      // 直接使用后端返回的设备列表，现在数据使用deviceId作为标识符
      deviceList.value = apiResponseData.list;
      
      // 后端可能发送 total: 0 即便列表有内容。
      // 如果后端的 total 是有效正数，则使用它；否则，使用列表长度作为回退。
      if (apiResponseData.total && Number(apiResponseData.total) > 0) {
        total.value = Number(apiResponseData.total);
      } else {
        total.value = apiResponseData.list.length; // 如果 total 无效或为0，则使用实际列表长度
      }
      
      console.log('deviceList.value已更新:', JSON.stringify(deviceList.value));
      console.log('total.value已更新:', total.value);

    } else {
      // 如果 apiResponseData 不是预期格式 (例如，不是对象，或没有 'list' 数组)
      console.error('获取设备列表失败：API响应数据格式不正确。接收到的数据:', apiResponseData);
      ElMessage.error('获取设备列表失败：数据格式错误');
      deviceList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取设备列表时发生异常:', error);
    let errorMessage = '获取设备列表时发生网络或服务器错误';
    // 尝试从Axios错误对象中获取更具体的错误消息
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    ElMessage.error(errorMessage);
    deviceList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDeviceList()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    if (key !== 'pageNum' && key !== 'pageSize') {
      searchForm[key] = ''
    }
  })
  dateRange.value = []
  currentPage.value = 1
  fetchDeviceList()
}

// 添加设备
const handleAdd = () => {
  resetDeviceForm()
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑设备
const handleEdit = (row) => {
  console.log('编辑设备，行数据:', row);
  if (!row || !row.deviceId) {
    ElMessage.error('无效的设备数据，缺少设备ID');
    return;
  }
  dialogType.value = 'edit'
  getDeviceDetails(row.deviceId)
}

// 查看设备
const handleView = (row) => {
  console.log('查看设备，行数据:', row);
  if (!row || !row.deviceId) {
    ElMessage.error('无效的设备数据，缺少设备ID');
    return;
  }
  dialogType.value = 'view'
  getDeviceDetails(row.deviceId)
}

// 获取设备详情
const getDeviceDetails = async (deviceId) => {
  loading.value = true;
  try {
    // 检查ID是否有效
    if (!deviceId) {
      ElMessage.error('设备ID不能为空');
      loading.value = false;
      return;
    }
    
    const deviceData = await getDeviceById(deviceId);
    console.log('Data received from getDeviceById in getDeviceDetails:', deviceData);

    // 检查 deviceData 是否是有效的对象并且包含 deviceId 属性
    if (deviceData && typeof deviceData === 'object' && deviceData.deviceId) {
      // 将设备信息填充到表单
      Object.keys(deviceForm).forEach(key => {
        if (deviceData[key] !== undefined) {
          deviceForm[key] = deviceData[key];
        }
      });
      dialogVisible.value = true;
    } else {
      console.error('获取设备详情失败: API未返回有效设备数据或数据格式不正确. 收到的数据:', deviceData);
      ElMessage.error('获取设备详情失败或数据无效');
    }
  } catch (error) {
    console.error('获取设备详情时发生异常:', error);
    let errorMessage = '获取设备详情时发生网络或服务器错误';
    // 尝试从Axios错误对象中获取更具体的错误消息
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    ElMessage.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

// 删除设备
const handleDelete = (row) => {
  if (!row || !row.deviceId) {
    ElMessage.error('无效的设备数据，缺少设备ID');
    return;
  }
  currentDevice.value = row
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  try {
    const result = await deleteDevice(currentDevice.value.deviceId);
    console.log('Delete API result:', result);

    // 如果API没有抛出错误，并且 result 不是显式的 false，则认为成功
    if (result !== false) { 
      ElMessage.success('删除成功');
      deleteDialogVisible.value = false;
      fetchDeviceList(); // 重新加载列表
    } else {
      // 即使API调用本身没出错（没进catch），但如果返回了false，也按失败处理
      ElMessage.error('删除失败：操作未成功');
    }
  } catch (error) {
    console.error('删除设备时发生异常:', error);
    let errorMessage = '删除设备时发生网络或服务器错误';
    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message;
    } else if (error.message) {
      errorMessage = error.message;
    }
    ElMessage.error(errorMessage);
  }
};

// 重置设备表单
const resetDeviceForm = () => {
  deviceForm.deviceId = ''
  deviceForm.deviceName = ''
  deviceForm.ipAddress = ''
  deviceForm.location = ''
  deviceForm.deviceType = ''
  deviceForm.installDate = ''
  deviceForm.remark = ''
  
  if (deviceFormRef.value) {
    deviceFormRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!deviceFormRef.value) return
  
  await deviceFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      const formData = { ...deviceForm }
      
      // 根据类型决定是创建还是更新
      if (dialogType.value === 'add') {
        // 创建设备时不需要传递deviceId，后端会自动生成
        const { deviceId, ...createData } = formData;
        const createdDeviceData = await createDevice(createData);
        console.log('Create API result:', createdDeviceData);

        // 如果API没有抛出错误，并且 createdDeviceData 被认为是有效的
        if (createdDeviceData && (typeof createdDeviceData === 'object' || typeof createdDeviceData === 'number' || typeof createdDeviceData === 'string')) {
          ElMessage.success('添加成功');
          dialogVisible.value = false;
          fetchDeviceList(); // 重新加载列表
        } else if (createdDeviceData === true) {
           ElMessage.success('添加成功');
           dialogVisible.value = false;
           fetchDeviceList();
        } else {
          ElMessage.error('添加失败：未收到有效确认');
        }
      } else if (dialogType.value === 'edit') {
        // 更新时需要传递deviceId
        const deviceId = formData.deviceId;
        const { deviceId: _, ...updateData } = formData; // 解构出deviceId，剩余属性作为更新数据
        const updatedDeviceData = await updateDevice(deviceId, updateData);
        console.log('Update API result:', updatedDeviceData);
        
        // 与创建逻辑类似
        if (updatedDeviceData && (typeof updatedDeviceData === 'object' || typeof updatedDeviceData === 'number' || typeof updatedDeviceData === 'string')) {
          ElMessage.success('更新成功');
          dialogVisible.value = false;
          fetchDeviceList(); // 重新加载列表
        } else if (updatedDeviceData === true) {
           ElMessage.success('更新成功');
           dialogVisible.value = false;
           fetchDeviceList();
        } else {
          ElMessage.error('更新失败：未收到有效确认');
        }
      }
    } catch (error) {
      console.error('保存设备时发生异常:', error);
      let errorMessage = '保存设备时发生网络或服务器错误';
      if (error.response && error.response.data && error.response.data.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      ElMessage.error(errorMessage);
    } finally {
      loading.value = false
    }
  })
};

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDeviceList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDeviceList()
}

// 页面加载时获取数据
onMounted(() => {
  fetchDeviceList()
})
</script>

<style scoped>
.power-device-manage-container {
  padding: 20px;
}

.search-bar {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 