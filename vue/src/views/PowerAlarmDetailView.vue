<template>
  <div class="alarm-detail-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3 style="font-weight: normal;">告警详情</h3>
          <div>
            <el-button @click="$router.back()" plain>返回</el-button>
            <el-button v-if="!alarmDetail?.isProcessed" type="primary" @click="handleProcess">处理告警</el-button>
          </div>
        </div>
      </template>
      
      <div v-loading="loading">
        <el-descriptions title="基本信息" :column="2" border style="font-weight: normal;">
          <el-descriptions-item label="设备名称">{{ alarmDetail?.deviceName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备IP">{{ alarmDetail?.ipAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="设备位置">{{ alarmDetail?.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="告警类型">{{ alarmDetail?.monitorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <span :style="{color: getAlarmLevelColor(alarmDetail?.alarmLevel)}">
              {{ alarmDetail?.alarmLevel || '-' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="告警时间">{{ alarmDetail?.collectTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="告警描述" :span="2">{{ alarmDetail?.alarmDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="监控值">{{ alarmDetail?.value || '-' }}</el-descriptions-item>
          <el-descriptions-item label="阈值范围">{{ alarmDetail?.thresholdLower || '-' }} ~ {{ alarmDetail?.thresholdUpper || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <el-descriptions v-if="alarmDetail?.isProcessed" title="处理信息" :column="2" border>
          <el-descriptions-item label="处理状态">
            <el-tag type="success">已处理</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否已解决">
            <el-tag :type="alarmDetail?.resolved ? 'success' : 'warning'">
              {{ alarmDetail?.resolved ? '已解决' : '未解决' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ alarmDetail?.processBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ alarmDetail?.processTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理方式">{{ alarmDetail?.processMethod || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理描述" :span="2">{{ alarmDetail?.processDescription || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-else class="unprocessed-tip">
          <el-alert
            title="此告警尚未处理"
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>请及时处理此告警，避免设备安全风险</p>
            </template>
          </el-alert>
        </div>
      </div>
    </el-card>
    
    <!-- 设备实时监控区域 - 独立卡片 -->
    <PowerDeviceMonitor
      :realtime-data="monitorData.realtimeData"
      :cycle-count="monitorData.cycleCount"
      :battery-capacity="monitorData.batteryCapacity"
      :charge-current-data="monitorData.chargeCurrentData"
      :load-data="monitorData.loadData"
      :charge-discharge-data="monitorData.chargeDischargeData"
      :device-status-data="monitorData.deviceStatusData"
    />
    
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAlarmDetail, processAlarm } from '../api/powerAlarm'
import PowerDeviceMonitor from '@/components/common/PowerDeviceMonitor.vue'

export default {
  name: 'PowerAlarmDetailView',
  components: {
    PowerDeviceMonitor
  },
  props: {
    id: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(false)
    const alarmDetail = ref({})
    const processDialogVisible = ref(false)
    
    const processForm = reactive({
      alarmId: null,
      processMethod: '',
      processBy: '',
      processDescription: '',
      resolved: false,
      remark: ''
    })
    
    // 设备监控数据（mock数据，后续可从接口获取）
    const monitorData = reactive({
      realtimeData: {
        acVoltage: 'AC230V',
        dcVoltage: 'DC24V',
        loadCurrent: '340mA',
        batteryLevel: '100%'
      },
      cycleCount: 150,
      batteryCapacity: {
        capacity1: '20000mAh',
        capacity2: '20000mAh'
      },
      chargeCurrentData: [],
      loadData: [],
      chargeDischargeData: {
        charge: [],
        discharge: []
      },
      deviceStatusData: {
        warningCount: 2,
        faultCount: 1,
        alarmRecoverCount: 5,
        normalCount: 10
      }
    })
    
    // 生成示例监控数据
    const generateMonitorData = () => {
      // 充电电流数据（1-5月）
      const months = ['1月', '2月', '3月', '4月', '5月']
      monitorData.chargeCurrentData = months.map(month => ({
        month,
        value: Math.floor(Math.random() * 500) + 200
      }))
      
      // 24小时负载数据
      const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
      monitorData.loadData = hours.map(hour => ({
        hour,
        value: Math.floor(Math.random() * 100) + 50
      }))
      
      // 充放电数据
      monitorData.chargeDischargeData = {
        charge: hours.map(hour => ({
          hour,
          value: Math.floor(Math.random() * 200) + 100
        })),
        discharge: hours.map(hour => ({
          hour,
          value: Math.floor(Math.random() * 150) + 50
        }))
      }
    }
    
    // 获取告警详情
    const fetchAlarmDetail = async () => {
      const alarmId = props.id || route.params.id
      if (!alarmId) {
        ElMessage.error('缺少告警ID')
        router.push('/power-alarms')
        return
      }
      
      loading.value = true
      try {
        const res = await getAlarmDetail(alarmId)
        console.log('获取告警详情响应:', res)

        // 检查是否为错误响应
        if (res && res.code && res.code !== 200) {
          const errorMsg = res.message || '获取告警详情失败'
          ElMessage.error(errorMsg)
          router.push('/power-alarms')
          return
        }

        // 处理成功响应 - 直接使用数据对象（由响应拦截器处理过）
        if (res && res.id) {
          alarmDetail.value = res
          console.log('告警详情数据:', alarmDetail.value)
        } else {
          ElMessage.error('获取告警详情失败：数据格式错误')
          router.push('/power-alarms')
        }
      } catch (error) {
        console.error('获取告警详情失败:', error)
        ElMessage.error('获取告警详情失败')
        router.push('/power-alarms')
      } finally {
        loading.value = false
      }
    }
    
    // 打开处理对话框
    const handleProcess = () => {
      processForm.alarmId = alarmDetail.value?.id
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
        console.log('告警处理响应:', res)

        // 检查是否为错误响应
        if (res && res.code && res.code !== 200) {
          const errorMsg = res.message || '告警处理失败'
          ElMessage.error(errorMsg)
          return
        }

        // 处理成功响应
        if (res && res.success) {
          ElMessage.success(res.message || '告警处理成功')
          processDialogVisible.value = false
          fetchAlarmDetail()
        } else {
          ElMessage.error('告警处理失败')
        }
      } catch (error) {
        console.error('告警处理失败:', error)
        ElMessage.error('告警处理失败')
      }
    }
    
    onMounted(() => {
      fetchAlarmDetail()
      generateMonitorData()
    })
    
    // 根据告警级别获取标签类型
    const getAlarmLevelType = (level) => {
      if (!level) return 'info' // 返回默认类型，避免空字符串
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
      return map[level] || 'info' // 返回默认类型
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

    return {
      loading,
      alarmDetail,
      processDialogVisible,
      processForm,
      monitorData,
      getAlarmLevelType,
      getAlarmLevelColor,
      handleProcess,
      submitProcess
    }
  }
}
</script>

<style scoped>
.alarm-detail-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.unprocessed-tip {
  margin-top: 20px;
}
.el-descriptions {
  margin-bottom: 20px;
}

:deep(.el-descriptions__title) {
  font-weight: normal !important;
}

:deep(.el-descriptions__label) {
  font-weight: normal !important;
}

:deep(.el-descriptions__content) {
  font-weight: normal !important;
}
</style>