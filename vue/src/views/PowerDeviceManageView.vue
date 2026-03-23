<template>
  <div class="power-device-manage-container">
    <h2 class="page-title" style="font-weight: normal;">电源台账管理</h2>

    <!-- 主操作区 -->
    <div class="main-operator-block">
      <!-- 左侧操作按钮 -->
      <div class="left-button">
        <el-button v-permission="'power:device'" type="primary" :icon="Plus" @click="handleAdd">
          新增设备
        </el-button>
        <!-- <el-button @click="batchModifyDevice">批量修改</el-button>
        <el-button @click="gotoTemplate">设备属性模版</el-button>
        <el-button @click="goToImportData">导入数据</el-button> -->
      </div>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <!-- 第一行：四个输入组件 -->
        <div class="search-row">
          <el-input v-model="searchForm.deviceName" class="search-item" placeholder="请输入设备名称" clearable></el-input>
          <el-input v-model="searchForm.ipAddress" class="search-item" placeholder="请输入设备IP" clearable></el-input>
          <el-input v-model="searchForm.location" class="search-item" placeholder="请输入设备位置" clearable></el-input>
          <el-select v-model="searchForm.deviceType" class="search-item" placeholder="请选择设备类型" clearable>
            <el-option
              v-for="item in deviceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </div>
        
        <!-- 第二行：日期选择器 -->
        <div class="search-row" >
             <el-date-picker
            v-model="dateRange"
            class="search-item date-range-picker"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD">
          </el-date-picker>
        </div>
        
        <!-- 第三行：查询和重置按钮 -->
        <div class="button-row">
          <div class="query-button-group">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="RefreshLeft" @click="resetSearch">重置</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 设备列表 -->
    <el-table
      v-loading="loading"
      :data="deviceList"
      border
      stripe
      
      :header-cell-style="{background:'#f8f8f9',color:'rgb(80 78 78)'}"
      style="width: 100%"
      @sort-change="sortChangeHandler">
      <el-table-column type="index" label="序号" align="center" width="60" />
      <el-table-column prop="deviceId" label="设备ID" align="center" width="120"></el-table-column>
      <el-table-column prop="deviceName" label="设备名称" min-width="150" align="center" sortable="custom">
        <template #default="scope">
          <a
            class="table-column-link has-icon"
            href="javascript:void(0)"
            style="margin-left: 5px"
            @click="handleToDirtyDataMonitor(scope.row)"
          >
            <i
              class="el-icon-circle-check device-status-icon"
              :class="getDeviceStatusClass(scope.row.onlineStatus)"
            />
            <span>{{ scope.row.deviceName }}</span>
          </a>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" align="center" min-width="150" sortable="custom"></el-table-column>
      <el-table-column prop="location" label="设备位置" align="center" min-width="150"></el-table-column>
      <el-table-column prop="deviceType" label="设备类型" align="center" width="120"></el-table-column>
      <el-table-column prop="installDate" label="安装日期" align="center" width="160"></el-table-column>
      <el-table-column prop="tenantName" label="所属租户" align="center" width="120"></el-table-column>
      <el-table-column label="操作" fixed="right" width="180px" align="center">
        <template #default="scope">
          <div class="operation-buttons">
            <el-button v-permission="'power:device'" link type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button v-permission="'power:device'" link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button v-permission="'power:device'" link type="primary" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页器 -->
    <div class="paging-operation">
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
    
    <!-- 设备监控对话框 -->
    <el-dialog
      v-model="monitorDialogVisible"
      title="设备实时监控"
      width="85%"
      :close-on-click-modal="false">
      <el-descriptions :column="3" border style="margin-bottom: 20px;">
        <el-descriptions-item label="设备名称">{{ currentDevice.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentDevice.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="设备位置">{{ currentDevice.location }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ currentDevice.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="安装日期">{{ currentDevice.installDate }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentDevice.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 设备实时监控组件 -->
      <PowerDeviceMonitor 
        :realtime-data="mockRealtimeData"
        :cycle-count="mockCycleCount"
        :battery-capacity="mockBatteryCapacity"
        :device-status-data="mockDeviceStatus"
      />
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="monitorDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDeviceList, getDeviceById, createDevice, updateDevice, deleteDevice } from '@/api/powerDevice'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, RefreshLeft } from '@element-plus/icons-vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import PowerDeviceMonitor from '@/components/common/PowerDeviceMonitor.vue'

const userStore = useUserStore()
const router = useRouter()

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
const monitorDialogVisible = ref(false)
const currentDevice = ref({})

// 设备监控模拟数据
const mockRealtimeData = reactive({
  acVoltage: 'AC220V',
  dcVoltage: 'DC24V',
  loadCurrent: '450mA',
  batteryLevel: '85%'
})
const mockCycleCount = ref(186)
const mockBatteryCapacity = reactive({
  capacity1: '20000mAh',
  capacity2: '20000mAh'
})
const mockDeviceStatus = reactive({
  warningCount: 2,
  faultCount: 0,
  alarmRecoverCount: 5,
  normalCount: 12
})

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
  pageSize: 10,
  sort: '',
  order: ''
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

// 排序处理
const sortChangeHandler = (sort) => {
  if (sort.prop) {
    // 前端排序，不涉及后端接口
    const sortedList = [...deviceList.value].sort((a, b) => {
      let aValue = a[sort.prop]
      let bValue = b[sort.prop]
      
      // 处理字符串比较
      if (typeof aValue === 'string' && typeof bValue === 'string') {
        aValue = aValue.toLowerCase()
        bValue = bValue.toLowerCase()
      }
      
      if (aValue < bValue) {
        return sort.order === 'ascending' ? -1 : 1
      }
      if (aValue > bValue) {
        return sort.order === 'ascending' ? 1 : -1
      }
      return 0
    })
    
    deviceList.value = sortedList
  }
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

// 跳转到脏数据监控页
const handleToDirtyDataMonitor = (row) => {
  if (!row || !row.deviceId) return
  router.push({
    path: '/dirty-data-monitor',
    query: {
      deviceId: row.deviceId,
      deviceName: row.deviceName
    }
  })
}

// 查看设备 - 打开监控对话框
const handleView = (row) => {
  console.log('查看设备，行数据:', row);
  if (!row || !row.deviceId) {
    ElMessage.error('无效的设备数据，缺少设备ID');
    return;
  }
  // 设置当前设备信息
  currentDevice.value = { ...row };
  
  // 根据设备ID生成不同的模拟监控数据
  const deviceIdNum = parseInt(row.deviceId) || 1;
  mockRealtimeData.acVoltage = `AC${218 + (deviceIdNum % 5)}V`;
  mockRealtimeData.dcVoltage = `DC${23 + (deviceIdNum % 3)}V`;
  mockRealtimeData.loadCurrent = `${300 + (deviceIdNum * 50) % 400}mA`;
  mockRealtimeData.batteryLevel = `${70 + (deviceIdNum * 7) % 30}%`;
  mockCycleCount.value = 100 + (deviceIdNum * 23) % 200;
  
  // 打开监控对话框
  monitorDialogVisible.value = true;
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

// 新增方法
const batchModifyDevice = () => {
  ElMessage.info('批量修改功能开发中...')
}

const gotoTemplate = () => {
  ElMessage.info('设备属性模版功能开发中...')
}

const goToImportData = () => {
  ElMessage.info('导入数据功能开发中...')
}

const getDeviceStatusClass = (status) => {
  return status === 1 ? 'online-icon' : 'offline-icon'
}

const gotoTopology = (command, row) => {
  ElMessage.info(`定位功能开发中... 命令: ${command}, 设备: ${row.deviceName}`)
}
</script>

<style scoped>
.power-device-manage-container {
  padding: 20px;
}

/* 页面标题样式 */
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

/* 主操作区样式 */
/* 主操作区样式 */
.main-operator-block {
  margin-top: 10px;
  margin-bottom: 10px;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.left-button {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: -0px;
}

.left-button .el-button {
  margin: 0;
}

.search-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: flex-start;
}


.search-item {
  width: 190px;
}

.search-item .el-input,
.search-item .el-select,
.search-item .el-date-picker {
  width: 100%;
}

/* 搜索区域样式 */
.search-area {
  margin-top: 0;
  flex: 1;
  max-width: 780px;
}

.search-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.search-item {
  width: 190px;
}

.date-range-picker {
  width: 390px; /* 两个输入框的宽度加上间距 */
}

.search-item .el-input,
.search-item .el-select,
.search-item .el-date-picker {
  width: 100%;
}

.query-button-group {
  display: flex;
  gap: 5px;
}

.button-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.query-button-group .el-button {
  margin: 0;
}

/* 按钮样式 */
:deep(.el-button) {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
}

:deep(.el-button--primary) {
  background-color: #409EFF;
  border-color: #409EFF;
  font-weight: 500;
}

:deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

/* 表格样式 */
:deep(.el-table) {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 15px;
}

:deep(.el-table th) {
  background-color: #f8f8f9 !important;
  color: rgb(80 78 78) !important;
  font-weight: 600 !important;
  font-size: 14px;
}

:deep(.el-table td) {
  font-size: 14px;
  color: #606266;
}

/* 分页样式 */
.paging-operation {
  padding: 10px;
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  justify-content: center;
  gap: 5px;
  flex-wrap: nowrap;
}

.operation-buttons .el-button {
  margin: 0;
}

/* 表格链接样式 */
.table-column-link {
  color: #409EFF;
  text-decoration: none;
}

.table-column-link:hover {
  color: #66b1ff;
}

.device-status-icon {
  margin-right: 5px;
}

.online-icon {
  color: #67C23A;
}

.offline-icon {
  color: #F56C6C;
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-operator-block {
    flex-direction: column;
    align-items: flex-start;
    flex-wrap: wrap;
  }
  
  .search-block {
    width: 100%;
    min-width: auto;
    flex-wrap: wrap;
  }
  
  .left-button {
    width: 100%;
    margin-bottom: 10px;
  }
  
.search-item .el-input,
.search-item .el-select,
.search-item .el-date-picker  {
  width: 100%;
}
.date-range-picker {
  width: 390px; /* 两个输入框的宽度加上间距 */
}


}

@media (max-width: 768px) {
  .search-item {
    margin-bottom: 10px;
  }
  
  .search-item .el-input,
  .search-item .el-select {
    width: 100%;
  }
  .search-item .el-date-picker{
    width: 100%;
  }
  
  .query-button-group {
    width: 100%;
    justify-content: flex-end;
    margin-top: 10px;
  }
  .date-range-picker {
  width: 390px; /* 两个输入框的宽度加上间距 */
}

}



</style>