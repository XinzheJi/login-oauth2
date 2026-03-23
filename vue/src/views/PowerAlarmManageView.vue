<template>
  <div class="alarm-manage-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>告警管理</h3>
          <el-button type="primary" @click="refreshAlarms">刷新</el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <el-form :model="queryParams" label-width="100px" class="search-form">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="设备名称">
              <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="设备IP">
              <el-input v-model="queryParams.ipAddress" placeholder="请输入设备IP" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="设备位置">
              <el-input v-model="queryParams.location" placeholder="请输入设备位置" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="告警类型">
              <el-select v-model="queryParams.alarmType" placeholder="请选择告警类型" clearable style="width:100%">
                <el-option v-for="item in alarmTypeOptions" :key="item.code" :label="item.name" :value="item.name"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="告警级别">
              <el-select v-model="queryParams.alarmLevel" placeholder="请选择告警级别" clearable style="width:100%">
                <el-option v-for="item in alarmLevelOptions" :key="item.code" :label="item.name" :value="item.name"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="处理状态">
              <el-select v-model="queryParams.isProcessed" placeholder="请选择处理状态" clearable style="width:100%">
                <el-option label="未处理" :value="false"></el-option>
                <el-option label="已处理" :value="true"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="告警时间">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD HH:mm:ss"
                :default-time="['00:00:00', '23:59:59']"
                style="width:100%"
              ></el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24" style="text-align: right">
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button type="info" @click="$router.push('/power-alarm-statistics')">告警统计</el-button>
          </el-col>
        </el-row>
      </el-form>
      
      <!-- 告警表格 -->
      <el-table
        v-loading="loading"
        :data="alarmList"
        stripe
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="deviceName" label="设备名称" width="150"></el-table-column>
        <el-table-column prop="ipAddress" label="设备IP" width="120"></el-table-column>
        <el-table-column prop="location" label="设备位置" width="150"></el-table-column>
        <el-table-column prop="monitorName" label="告警类型" width="150"></el-table-column>
        <el-table-column label="告警级别" width="100">
          <template #default="scope">
            <el-tag
              :type="getAlarmLevelType(scope.row.alarmLevel)"
              :style="{ color: '#fff', backgroundColor: getAlarmLevelColor(scope.row.alarmLevel) }"
            >{{ scope.row.alarmLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmDesc" label="告警描述" min-width="200"></el-table-column>
        <el-table-column prop="collectTime" label="告警时间" width="180"></el-table-column>
        <el-table-column label="处理状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isProcessed ? 'success' : 'danger'">
              {{ scope.row.isProcessed ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button
              link
              type="primary"
              size="small"
              @click="handleViewDetail(scope.row)"
            >详情</el-button>
            <el-button
              v-if="!scope.row.isProcessed"
              link
              type="primary"
              size="small"
              @click="handleProcess(scope.row)"
            >处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 告警处理对话框 -->
    <el-dialog
      v-model="processDialogVisible"
      title="告警处理"
      width="500px"
    >
      <el-form :model="processForm" label-width="100px">
        <el-form-item label="处理方式">
          <el-select v-model="processForm.processMethod" placeholder="请选择处理方式" style="width:100%">
            <el-option label="确认" value="确认"></el-option>
            <el-option label="忽略" value="忽略"></el-option>
            <el-option label="修复" value="修复"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="processForm.processBy" placeholder="请输入处理人姓名"></el-input>
        </el-form-item>
        <el-form-item label="处理描述">
          <el-input
            v-model="processForm.processDescription"
            type="textarea"
            placeholder="请输入处理描述"
            :rows="4"
          ></el-input>
        </el-form-item>
        <el-form-item label="是否已解决">
          <el-switch v-model="processForm.resolved"></el-switch>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="processForm.remark" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAlarmList, getAlarmTypes, getAlarmLevels, processAlarm } from '../api/powerAlarm'

export default {
  name: 'PowerAlarmManageView',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const alarmList = ref([])
    const total = ref(0)
    const processDialogVisible = ref(false)
    const currentAlarm = ref(null)
    const alarmTypeOptions = ref([])
    const alarmLevelOptions = ref([])
    const dateRange = ref([])
    
    const queryParams = reactive({
      deviceName: '',
      ipAddress: '',
      location: '',
      alarmType: '',
      alarmLevel: '',
      isProcessed: null,
      alarmTimeStart: '',
      alarmTimeEnd: '',
      pageNum: 1,
      pageSize: 10
    })
    
    const processForm = reactive({
      alarmId: null,
      processMethod: '',
      processBy: '',
      processDescription: '',
      resolved: false,
      remark: ''
    })
    
    // 获取告警列表
    const getAlarms = async () => {
      loading.value = true
      try {
        // 处理日期范围
        if (dateRange.value && dateRange.value.length === 2) {
          queryParams.alarmTimeStart = dateRange.value[0]
          queryParams.alarmTimeEnd = dateRange.value[1]
        } else {
          queryParams.alarmTimeStart = ''
          queryParams.alarmTimeEnd = ''
        }
        
        // 确保分页参数是数字类型
        const params = { ...queryParams }
        params.pageNum = Number(params.pageNum)
        params.pageSize = Number(params.pageSize)
        
        console.log('发送告警请求参数:', JSON.stringify(params))
        const res = await getAlarmList(params)
        console.log('获取告警列表响应:', res)
        
        // 直接处理响应数据，后端返回的已经是分页对象
        if (res && Array.isArray(res.list)) {
          // 响应直接就是分页对象的情况
          alarmList.value = res.list
          total.value = Number(res.total || 0)
          // 同步当前页码
          queryParams.pageNum = Number(res.pageNum || queryParams.pageNum)
          console.log('处理后的告警列表(直接分页对象):', alarmList.value)
        } else if (res && res.code === 200 && res.data) {
          // 标准Result包装的情况
          if (res.data.list) {
            alarmList.value = res.data.list
            total.value = Number(res.data.total || 0)
            // 同步当前页码
            queryParams.pageNum = Number(res.data.pageNum || queryParams.pageNum)
          } else if (Array.isArray(res.data)) {
            alarmList.value = res.data
            total.value = res.data.length
          } else {
            alarmList.value = []
            total.value = 0
          }
          console.log('处理后的告警列表(标准Result):', alarmList.value)
        } else if (res && Array.isArray(res)) {
          // 直接返回数组的情况
          alarmList.value = res
          total.value = res.length
          console.log('处理后的告警列表(数组):', alarmList.value)
        } else {
          alarmList.value = []
          total.value = 0
          console.warn('获取告警列表失败，无法识别的响应格式:', res)
          ElMessage.warning('获取告警列表失败，请联系管理员')
        }
      } catch (error) {
        console.error('获取告警列表失败:', error)
        ElMessage.error('获取告警列表失败')
        alarmList.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // 获取告警类型选项
    const fetchAlarmTypes = async () => {
      try {
        const res = await getAlarmTypes()
        if (res && Array.isArray(res)) {
          // 直接返回数组
          alarmTypeOptions.value = res
        } else if (res && res.code === 200 && res.data) {
          // 标准Result包装
          alarmTypeOptions.value = res.data
        } else if (res && Array.isArray(res.data)) {
          // data字段是数组
          alarmTypeOptions.value = res.data
        } else {
          console.warn('获取告警类型失败，无法识别的响应格式:', res)
          alarmTypeOptions.value = []
        }
      } catch (error) {
        console.error('获取告警类型失败:', error)
        alarmTypeOptions.value = []
      }
    }
    
    // 获取告警级别选项
    const fetchAlarmLevels = async () => {
      try {
        const res = await getAlarmLevels()
        if (res && Array.isArray(res)) {
          // 直接返回数组
          alarmLevelOptions.value = res
        } else if (res && res.code === 200 && res.data) {
          // 标准Result包装
          alarmLevelOptions.value = res.data
        } else if (res && Array.isArray(res.data)) {
          // data字段是数组
          alarmLevelOptions.value = res.data
        } else {
          console.warn('获取告警级别失败，无法识别的响应格式:', res)
          alarmLevelOptions.value = []
        }
      } catch (error) {
        console.error('获取告警级别失败:', error)
        alarmLevelOptions.value = []
      }
    }
    
    // 根据告警级别获取标签类型
    const getAlarmLevelType = (level) => {
      if (!level) return ''
      const map = {
        '信息': 'info',
        '警告': 'warning',
        '严重': 'danger',
        '紧急': 'danger',
        'INFO': 'info',
        'WARNING': 'warning',
        'CRITICAL': 'danger',
        'EMERGENCY': 'danger'
      }
      return map[level] || ''
    }
    
    // 根据告警级别获取颜色
    const getAlarmLevelColor = (level) => {
      if (!level) return '#909399' // 默认灰色
      
      const map = {
        '信息': '#2196F3', // 蓝色
        '警告': '#FF9800', // 橙色
        '严重': '#F44336', // 红色
        '紧急': '#9C27B0', // 紫色
        'INFO': '#2196F3',
        'WARNING': '#FF9800',
        'CRITICAL': '#F44336',
        'EMERGENCY': '#9C27B0'
      }
      return map[level] || '#909399' // 默认灰色
    }
    
    // 刷新告警列表
    const refreshAlarms = () => {
      getAlarms()
    }
    
    // 查询按钮
    const handleQuery = () => {
      queryParams.pageNum = 1
      getAlarms()
    }
    
    // 重置查询条件
    const resetQuery = () => {
      Object.keys(queryParams).forEach(key => {
        if (key !== 'pageNum' && key !== 'pageSize') {
          queryParams[key] = ''
        }
      })
      queryParams.isProcessed = null
      dateRange.value = []
      handleQuery()
    }
    
    // 页码改变
    const handleCurrentChange = (page) => {
      queryParams.pageNum = Number(page)
      getAlarms()
    }
    
    // 每页条数改变
    const handleSizeChange = (size) => {
      queryParams.pageSize = Number(size)
      queryParams.pageNum = 1
      getAlarms()
    }
    
    // 查看告警详情
    const handleViewDetail = (row) => {
      router.push(`/power-alarms/${row.id}`)
    }
    
    // 处理告警
    const handleProcess = (row) => {
      currentAlarm.value = row
      processForm.alarmId = row.id
      processForm.processMethod = '确认'
      processForm.processBy = ''
      processForm.processDescription = ''
      processForm.resolved = false
      processForm.remark = ''
      processDialogVisible.value = true
    }
    
    // 提交告警处理
    const submitProcess = async () => {
      if (!processForm.processBy) {
        ElMessage.warning('请输入处理人')
        return
      }
      if (!processForm.processDescription) {
        ElMessage.warning('请输入处理描述')
        return
      }
      
      try {
        const res = await processAlarm(processForm)
        if (res && res.success === true) {
          // 直接返回 {success: true} 的情况
          ElMessage.success('告警处理成功')
          processDialogVisible.value = false
          getAlarms()
        } else if (res && res.code === 200) {
          // 标准Result包装的情况
          ElMessage.success(res.message || '告警处理成功')
          processDialogVisible.value = false
          getAlarms()
        } else {
          ElMessage.error((res && res.message) || '告警处理失败')
        }
      } catch (error) {
        console.error('告警处理失败:', error)
        ElMessage.error('告警处理失败')
      }
    }
    
    onMounted(() => {
      getAlarms()
      fetchAlarmTypes()
      fetchAlarmLevels()
    })
    
    return {
      loading,
      alarmList,
      total,
      queryParams,
      dateRange,
      processDialogVisible,
      processForm,
      alarmTypeOptions,
      alarmLevelOptions,
      getAlarmLevelType,
      getAlarmLevelColor,
      refreshAlarms,
      handleQuery,
      resetQuery,
      handleCurrentChange,
      handleSizeChange,
      handleViewDetail,
      handleProcess,
      submitProcess
    }
  }
}
</script>

<style scoped>
.alarm-manage-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
</style> 