<template>
  <div class="device-aging-fault-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          设备老化与故障感知
        </h1>
        <!-- 设备选择器 -->
        <div class="device-selector">
          <el-select 
            v-model="selectedDeviceId" 
            placeholder="选择设备" 
            size="large"
            @change="onDeviceChange"
            class="device-select"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.name} (${device.location})`"
              :value="device.id"
            >
              <div class="device-option">
                <div class="device-name">{{ device.name }}</div>
                <div class="device-info">{{ device.supplier }} - {{ device.model }}</div>
              </div>
            </el-option>
          </el-select>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="updateData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="success" @click="triggerAIPrediction">
          <el-icon><Magic /></el-icon>
          触发AI预测
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：设备老化感知 -->
      <div class="chart-section left-section">
        <div class="chart-header">
          <h2 class="chart-title">
            <el-icon><LineChart /></el-icon>
            设备老化感知
          </h2>
          <div class="chart-controls">
            <el-select v-model="agingTimeRange" size="small" @change="updateAgingChart">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
        </div>
        <div class="chart-container">
          <div ref="agingChart" class="chart"></div>
        </div>
        <div class="chart-footer">
          <div class="legend-info">
            <span class="legend-item">
              <span class="legend-color aging-line"></span>
              老化评分
            </span>
            <span class="legend-item">
              <span class="legend-color threshold-line"></span>
              阈值线 (80分)
            </span>
            <span class="legend-item">
              <span class="legend-color warning-dot"></span>
              超阈值警告
            </span>
            <span v-if="showAIPrediction" class="legend-item">
              <span class="legend-color ai-prediction"></span>
              AI预测
            </span>
          </div>
          <!-- AI智能提示 -->
          <div v-if="showAIPrediction && aiPredictionTip" class="ai-tip">
            <el-alert
              :title="aiPredictionTip"
              type="warning"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>

      <!-- 右侧：故障趋势感知 -->
      <div class="chart-section right-section">
        <div class="chart-header">
          <h2 class="chart-title">
            <el-icon><DataAnalysis /></el-icon>
            设备故障趋势感知
          </h2>
          <div class="chart-controls">
            <el-select v-model="faultTimeRange" size="small" @change="updateFaultChart">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
        </div>
        <div class="chart-container">
          <div ref="faultChart" class="chart"></div>
        </div>
        <div class="chart-footer">
          <div class="legend-info">
            <span class="legend-item">
              <span class="legend-color risk-line"></span>
              历史风险评分
            </span>
            <span class="legend-item">
              <span class="legend-color predicted-line"></span>
              预测趋势
            </span>
            <span class="legend-item">
              <span class="legend-color high-risk"></span>
              高风险区域
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 设备详情卡片 -->
    <div class="device-info-section">
      <el-card class="device-info-card">
        <template #header>
          <div class="card-header">
            <h3>设备详情信息</h3>
            <el-tag :type="deviceInfo.status === '正常' ? 'success' : 'danger'">
              {{ deviceInfo.status }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="device-basic-info">
              <h4>基础信息</h4>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="设备ID">{{ deviceInfo.deviceId }}</el-descriptions-item>
                <el-descriptions-item label="设备名称">{{ deviceInfo.deviceName }}</el-descriptions-item>
                <el-descriptions-item label="设备型号">{{ deviceInfo.model }}</el-descriptions-item>
                <el-descriptions-item label="供应商">{{ deviceInfo.supplier }}</el-descriptions-item>
                <el-descriptions-item label="安装位置">{{ deviceInfo.location }}</el-descriptions-item>
                <el-descriptions-item label="IP地址">{{ deviceInfo.ipAddress }}</el-descriptions-item>
                <el-descriptions-item label="MAC地址">{{ deviceInfo.macAddress }}</el-descriptions-item>
                <el-descriptions-item label="序列号">{{ deviceInfo.serialNumber }}</el-descriptions-item>
                <el-descriptions-item label="安装日期">{{ deviceInfo.installDate }}</el-descriptions-item>
                <el-descriptions-item label="运行时长">{{ deviceInfo.runningTime }}</el-descriptions-item>
                <el-descriptions-item label="软件版本">{{ deviceInfo.softwareVersion }}</el-descriptions-item>
                <el-descriptions-item label="保修到期">{{ deviceInfo.warrantyExpiry }}</el-descriptions-item>
                <el-descriptions-item label="上次维护">{{ deviceInfo.maintenanceDate }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
          
          <el-col :span="12">
            <div class="device-status-info">
              <h4>实时状态</h4>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="当前老化评分">
                  <span :class="getAgingLevelClass(deviceInfo.currentAgingScore)">
                    {{ deviceInfo.currentAgingScore }}分
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="当前风险评分">
                  <span :class="getRiskLevelClass(deviceInfo.currentRiskScore)">
                    {{ deviceInfo.currentRiskScore }}%
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="电压">{{ deviceInfo.voltage }}V</el-descriptions-item>
                <el-descriptions-item label="温度">{{ deviceInfo.temperature }}°C</el-descriptions-item>
                <el-descriptions-item label="内存使用率">{{ deviceInfo.memoryUsage }}%</el-descriptions-item>
                <el-descriptions-item label="告警数量">
                  <el-tag :type="deviceInfo.alarmCount > 0 ? 'danger' : 'success'">
                    {{ deviceInfo.alarmCount }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
        
        <!-- AI智能建议 -->
        <div class="ai-suggestions">
          <h4>AI智能建议</h4>
          <div class="suggestions-list">
            <el-alert
              :title="aiSuggestions"
              type="info"
              :closable="false"
              show-icon
              class="suggestion-item"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { ref, onMounted, onUnmounted, nextTick, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { useDeviceStore } from '@/store/device'
import { 
  TrendCharts, 
  Refresh, 
  Download, 
  LineChart, 
  DataAnalysis,
  Magic
} from '@element-plus/icons-vue'

export default {
  name: 'DeviceAgingFaultView',
  props: {
    deviceId: {
      type: String,
      default: ''
    }
  },
  components: {
    TrendCharts,
    Refresh,
    Download,
    LineChart,
    DataAnalysis,
    Magic
  },
  setup(props) {
    const deviceStore = useDeviceStore()
    const route = useRoute()
    
    const agingChart = ref(null)
    const faultChart = ref(null)
    const agingTimeRange = ref('30d')
    const faultTimeRange = ref('30d')
    const showAIPrediction = ref(false)
    const aiPredictionTip = ref('')
    const selectedDeviceId = ref('')
    
    let agingChartInstance = null
    let faultChartInstance = null
    
    // 写死的模拟设备数据
    const deviceList = ref([
      { id: '1', name: '中港变电站S2', model: 'Cisco-2950', supplier: 'Cisco', location: '深圳南山', runtime: '820天' },
      { id: '2', name: '三角电公司S1', model: 'Huawei-S5700', supplier: 'Huawei', location: '广州黄埔', runtime: '560天' },
      { id: '3', name: '富豪变电站S3', model: 'ZTE-ZXR10', supplier: 'ZTE', location: '东莞松山湖', runtime: '1200天' },
      { id: '4', name: '科技园变电站S4', model: 'H3C-S5560', supplier: 'H3C', location: '深圳科技园', runtime: '450天' },
      { id: '5', name: '工业区变电站S5', model: 'Ruijie-RG-S5750', supplier: 'Ruijie', location: '佛山南海', runtime: '680天' }
    ])
    
    // 动态设备数据映射（用于从交换机管理页面跳转过来的设备）
    const dynamicDeviceMap = ref(new Map())
    
    // 生成动态设备数据
    const generateDynamicDeviceData = (deviceName, modelName) => {
      const deviceId = `${deviceName}_${modelName}`.replace(/[^a-zA-Z0-9_]/g, '_')
      
      // 如果已经生成过，直接返回
      if (dynamicDeviceMap.value.has(deviceId)) {
        return dynamicDeviceMap.value.get(deviceId)
      }
      
      // 生成老化数据（基于设备名称的哈希值确保数据一致性）
      const nameHash = deviceName.split('').reduce((a, b) => {
        a = ((a << 5) - a) + b.charCodeAt(0)
        return a & a
      }, 0)
      
      const agingData = []
      const riskData = { history: [], forecast: [] }
      
      // 生成30天的老化数据
      for (let i = 0; i < 30; i++) {
        const date = new Date()
        date.setDate(date.getDate() - 29 + i)
        const timeStr = date.toISOString().split('T')[0]
        
        // 基于设备名称哈希生成一致的老化趋势
        const baseScore = Math.abs(nameHash) % 40 + 20 // 20-60之间的基础分数
        const trend = i * 1.5 + Math.sin(i * 0.2) * 5 // 上升趋势加波动
        const randomFactor = Math.sin(nameHash + i) * 3 // 基于名称的"随机"因子
        
        const score = Math.max(0, Math.min(100, Math.round(baseScore + trend + randomFactor)))
        agingData.push({ time: timeStr, score })
        
        // 生成风险数据
        const riskScore = Math.max(0, Math.min(100, Math.round(score * 0.8 + Math.sin(i * 0.3) * 10)))
        riskData.history.push(riskScore)
      }
      
      // 生成预测数据
      for (let i = 0; i < 7; i++) {
        const lastRisk = riskData.history[riskData.history.length - 1]
        const trend = Math.sin(i * 0.5) * 5
        const predictedRisk = Math.max(0, Math.min(100, Math.round(lastRisk + trend + i * 2)))
        riskData.forecast.push(predictedRisk)
      }
      
      // 生成设备信息
      const deviceInfo = {
        deviceId: deviceId,
        deviceName: deviceName,
        model: modelName,
        supplier: getSupplierByModel(modelName),
        location: getLocationByDeviceName(deviceName),
        ipAddress: generateIPAddress(nameHash),
        macAddress: generateMACAddress(nameHash),
        serialNumber: `SN${Math.abs(nameHash).toString(16).toUpperCase().padStart(8, '0')}`,
        installDate: new Date(Date.now() - Math.abs(nameHash) % 1000 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        runningTime: `${Math.abs(nameHash) % 1000}天`,
        softwareVersion: `V${Math.floor(Math.random() * 5) + 1}.${Math.floor(Math.random() * 10)}.${Math.floor(Math.random() * 10)}`,
        warrantyExpiry: new Date(Date.now() + (365 - Math.abs(nameHash) % 365) * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        maintenanceDate: new Date(Date.now() - Math.abs(nameHash) % 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        voltage: (220 + Math.abs(nameHash) % 20).toFixed(1),
        temperature: (25 + Math.abs(nameHash) % 15).toFixed(1),
        memoryUsage: Math.abs(nameHash) % 80 + 20,
        alarmCount: Math.abs(nameHash) % 5,
        currentAgingScore: agingData[agingData.length - 1].score,
        currentRiskScore: riskData.history[riskData.history.length - 1],
        status: riskData.history[riskData.history.length - 1] > 70 ? '异常' : '正常'
      }
      
      // 生成AI建议
      const aiSuggestion = generateAISuggestion(deviceInfo)
      
      const deviceData = {
        device: {
          id: deviceId,
          name: deviceName,
          model: modelName,
          supplier: deviceInfo.supplier,
          location: deviceInfo.location,
          runtime: deviceInfo.runningTime
        },
        agingData,
        riskData,
        deviceInfo,
        aiSuggestion
      }
      
      dynamicDeviceMap.value.set(deviceId, deviceData)
      return deviceData
    }
    
    // 根据型号获取供应商
    const getSupplierByModel = (model) => {
      if (model.includes('Cisco')) return 'Cisco'
      if (model.includes('Huawei')) return 'Huawei'
      if (model.includes('ZTE')) return 'ZTE'
      if (model.includes('H3C')) return 'H3C'
      if (model.includes('Ruijie')) return 'Ruijie'
      return 'Unknown'
    }
    
    // 根据设备名称获取位置
    const getLocationByDeviceName = (name) => {
      if (name.includes('中港')) return '深圳南山'
      if (name.includes('三角')) return '广州黄埔'
      if (name.includes('富豪')) return '东莞松山湖'
      if (name.includes('科技园')) return '深圳科技园'
      if (name.includes('工业区')) return '佛山南海'
      return '未知位置'
    }
    
    // 生成IP地址
    const generateIPAddress = (hash) => {
      const a = Math.abs(hash) % 255
      const b = Math.abs(hash >> 8) % 255
      const c = Math.abs(hash >> 16) % 255
      const d = Math.abs(hash >> 24) % 255
      return `${a}.${b}.${c}.${d}`
    }
    
    // 生成MAC地址
    const generateMACAddress = (hash) => {
      const bytes = []
      for (let i = 0; i < 6; i++) {
        bytes.push(Math.abs(hash >> (i * 4)) % 256)
      }
      return bytes.map(b => b.toString(16).padStart(2, '0').toUpperCase()).join(':')
    }
    
    // 生成AI建议
    const generateAISuggestion = (deviceInfo) => {
      const riskScore = deviceInfo.currentRiskScore
      const agingScore = deviceInfo.currentAgingScore
      
      if (riskScore > 80 || agingScore > 90) {
        return `设备${deviceInfo.deviceName}风险等级较高，建议立即检查电源模块和散热系统，考虑计划性维护。`
      } else if (riskScore > 60 || agingScore > 70) {
        return `设备${deviceInfo.deviceName}运行状态需要关注，建议加强监控并准备维护计划。`
      } else {
        return `设备${deviceInfo.deviceName}运行状态良好，建议继续正常监控。`
      }
    }
    
    // 写死的老化感知数据
    const agingData = {
      '1': [
        { time: '2025-09-01', score: 30 },
        { time: '2025-09-02', score: 32 },
        { time: '2025-09-03', score: 35 },
        { time: '2025-09-04', score: 38 },
        { time: '2025-09-05', score: 42 },
        { time: '2025-09-06', score: 45 },
        { time: '2025-09-07', score: 48 },
        { time: '2025-09-08', score: 52 },
        { time: '2025-09-09', score: 55 },
        { time: '2025-09-10', score: 58 },
        { time: '2025-09-11', score: 62 },
        { time: '2025-09-12', score: 65 },
        { time: '2025-09-13', score: 68 },
        { time: '2025-09-14', score: 72 },
        { time: '2025-09-15', score: 75 },
        { time: '2025-09-16', score: 78 },
        { time: '2025-09-17', score: 82 },
        { time: '2025-09-18', score: 85 },
        { time: '2025-09-19', score: 88 },
        { time: '2025-09-20', score: 92 },
        { time: '2025-09-21', score: 95 },
        { time: '2025-09-22', score: 98 },
        { time: '2025-09-23', score: 100 },
        { time: '2025-09-24', score: 98 },
        { time: '2025-09-25', score: 95 },
        { time: '2025-09-26', score: 92 },
        { time: '2025-09-27', score: 88 },
        { time: '2025-09-28', score: 85 },
        { time: '2025-09-29', score: 82 },
        { time: '2025-09-30', score: 78 }
      ],
      '2': [
        { time: '2025-09-01', score: 25 },
        { time: '2025-09-02', score: 28 },
        { time: '2025-09-03', score: 30 },
        { time: '2025-09-04', score: 33 },
        { time: '2025-09-05', score: 35 },
        { time: '2025-09-06', score: 38 },
        { time: '2025-09-07', score: 40 },
        { time: '2025-09-08', score: 43 },
        { time: '2025-09-09', score: 45 },
        { time: '2025-09-10', score: 48 },
        { time: '2025-09-11', score: 50 },
        { time: '2025-09-12', score: 53 },
        { time: '2025-09-13', score: 55 },
        { time: '2025-09-14', score: 58 },
        { time: '2025-09-15', score: 60 },
        { time: '2025-09-16', score: 63 },
        { time: '2025-09-17', score: 65 },
        { time: '2025-09-18', score: 68 },
        { time: '2025-09-19', score: 70 },
        { time: '2025-09-20', score: 73 },
        { time: '2025-09-21', score: 75 },
        { time: '2025-09-22', score: 78 },
        { time: '2025-09-23', score: 80 },
        { time: '2025-09-24', score: 78 },
        { time: '2025-09-25', score: 75 },
        { time: '2025-09-26', score: 73 },
        { time: '2025-09-27', score: 70 },
        { time: '2025-09-28', score: 68 },
        { time: '2025-09-29', score: 65 },
        { time: '2025-09-30', score: 63 }
      ],
      '3': [
        { time: '2025-09-01', score: 45 },
        { time: '2025-09-02', score: 48 },
        { time: '2025-09-03', score: 52 },
        { time: '2025-09-04', score: 55 },
        { time: '2025-09-05', score: 58 },
        { time: '2025-09-06', score: 62 },
        { time: '2025-09-07', score: 65 },
        { time: '2025-09-08', score: 68 },
        { time: '2025-09-09', score: 72 },
        { time: '2025-09-10', score: 75 },
        { time: '2025-09-11', score: 78 },
        { time: '2025-09-12', score: 82 },
        { time: '2025-09-13', score: 85 },
        { time: '2025-09-14', score: 88 },
        { time: '2025-09-15', score: 92 },
        { time: '2025-09-16', score: 95 },
        { time: '2025-09-17', score: 98 },
        { time: '2025-09-18', score: 100 },
        { time: '2025-09-19', score: 98 },
        { time: '2025-09-20', score: 95 },
        { time: '2025-09-21', score: 92 },
        { time: '2025-09-22', score: 88 },
        { time: '2025-09-23', score: 85 },
        { time: '2025-09-24', score: 82 },
        { time: '2025-09-25', score: 78 },
        { time: '2025-09-26', score: 75 },
        { time: '2025-09-27', score: 72 },
        { time: '2025-09-28', score: 68 },
        { time: '2025-09-29', score: 65 },
        { time: '2025-09-30', score: 62 }
      ],
      '4': [
        { time: '2025-09-01', score: 20 },
        { time: '2025-09-02', score: 22 },
        { time: '2025-09-03', score: 25 },
        { time: '2025-09-04', score: 28 },
        { time: '2025-09-05', score: 30 },
        { time: '2025-09-06', score: 33 },
        { time: '2025-09-07', score: 35 },
        { time: '2025-09-08', score: 38 },
        { time: '2025-09-09', score: 40 },
        { time: '2025-09-10', score: 43 },
        { time: '2025-09-11', score: 45 },
        { time: '2025-09-12', score: 48 },
        { time: '2025-09-13', score: 50 },
        { time: '2025-09-14', score: 53 },
        { time: '2025-09-15', score: 55 },
        { time: '2025-09-16', score: 58 },
        { time: '2025-09-17', score: 60 },
        { time: '2025-09-18', score: 63 },
        { time: '2025-09-19', score: 65 },
        { time: '2025-09-20', score: 68 },
        { time: '2025-09-21', score: 70 },
        { time: '2025-09-22', score: 73 },
        { time: '2025-09-23', score: 75 },
        { time: '2025-09-24', score: 73 },
        { time: '2025-09-25', score: 70 },
        { time: '2025-09-26', score: 68 },
        { time: '2025-09-27', score: 65 },
        { time: '2025-09-28', score: 63 },
        { time: '2025-09-29', score: 60 },
        { time: '2025-09-30', score: 58 }
      ],
      '5': [
        { time: '2025-09-01', score: 35 },
        { time: '2025-09-02', score: 38 },
        { time: '2025-09-03', score: 40 },
        { time: '2025-09-04', score: 43 },
        { time: '2025-09-05', score: 45 },
        { time: '2025-09-06', score: 48 },
        { time: '2025-09-07', score: 50 },
        { time: '2025-09-08', score: 53 },
        { time: '2025-09-09', score: 55 },
        { time: '2025-09-10', score: 58 },
        { time: '2025-09-11', score: 60 },
        { time: '2025-09-12', score: 63 },
        { time: '2025-09-13', score: 65 },
        { time: '2025-09-14', score: 68 },
        { time: '2025-09-15', score: 70 },
        { time: '2025-09-16', score: 73 },
        { time: '2025-09-17', score: 75 },
        { time: '2025-09-18', score: 78 },
        { time: '2025-09-19', score: 80 },
        { time: '2025-09-20', score: 83 },
        { time: '2025-09-21', score: 85 },
        { time: '2025-09-22', score: 88 },
        { time: '2025-09-23', score: 90 },
        { time: '2025-09-24', score: 88 },
        { time: '2025-09-25', score: 85 },
        { time: '2025-09-26', score: 83 },
        { time: '2025-09-27', score: 80 },
        { time: '2025-09-28', score: 78 },
        { time: '2025-09-29', score: 75 },
        { time: '2025-09-30', score: 73 }
      ]
    }
    
    // 写死的故障趋势数据
    const riskData = {
      '1': { 
        history: [10, 12, 14, 15, 18, 20, 22, 25, 28, 30, 32, 35, 38, 40, 42, 45, 48, 50, 52, 55, 58, 60, 62, 65, 68, 70, 72, 75, 78, 80],
        forecast: [85, 88, 92, 95, 98, 100, 102]
      },
      '2': { 
        history: [5, 6, 8, 7, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58],
        forecast: [62, 65, 68, 70, 72, 75, 78]
      },
      '3': { 
        history: [15, 18, 20, 22, 25, 28, 30, 32, 35, 38, 40, 42, 45, 48, 50, 52, 55, 58, 60, 62, 65, 68, 70, 72, 75, 78, 80, 82, 85, 88],
        forecast: [92, 95, 98, 100, 102, 105, 108]
      },
      '4': { 
        history: [3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54],
        forecast: [58, 60, 62, 65, 68, 70, 72]
      },
      '5': { 
        history: [8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60, 62, 64, 66],
        forecast: [70, 72, 75, 78, 80, 82, 85]
      }
    }
    
    // 写死的AI智能建议数据
    const aiSuggestionsData = {
      '1': "AI预测：该设备预计在7天后老化评分将超过阈值，建议检查电源模块。",
      '2': "AI预测：该设备未来一周故障概率上升至65%，建议检测光纤链路。",
      '3': "AI预测：设备运行超过1000天，硬件老化风险较高，建议计划性维护。",
      '4': "AI预测：设备运行状态良好，建议继续监控。",
      '5': "AI预测：设备温度偏高，建议检查散热系统。"
    }
    
    
    // 根据路由参数查找设备信息
    const findDeviceByRouteParam = (deviceId) => {
      if (!deviceId) return null
      
      // 在设备列表中查找设备
      const device = deviceList.value.find(d => d.id === deviceId)
      
      if (device) {
        return device
      }
      
      return null
    }
    
    
    // 当前设备信息
    const deviceInfo = ref({
      deviceId: '',
      deviceName: '',
      model: '',
      supplier: '',
      location: '',
      runningTime: '',
      status: '正常',
      currentAgingScore: 0,
      currentRiskScore: 0
    })
    
    // AI智能建议
    const aiSuggestions = ref('')
    
    // 设备运行数据存储（按设备ID存储）
    const deviceDataMap = ref(new Map())
    
    // 当前设备数据
    const deviceData = ref([])

    // 获取设备老化数据
    const getAgingData = (deviceId) => {
      return agingData[deviceId] || []
    }
    
    // 获取设备故障风险数据
    const getRiskData = (deviceId) => {
      return riskData[deviceId] || { history: [], forecast: [] }
    }

    // 生成AI预测数据
    const generateAIPrediction = (currentData) => {
      const predictionData = []
      const lastData = currentData[currentData.length - 1]
      
      // 基于当前趋势预测未来7天
      for (let i = 1; i <= 7; i++) {
        const futureDate = new Date()
        futureDate.setDate(futureDate.getDate() + i)
        
        // 预测老化评分：基于当前趋势
        const trendFactor = i * 0.5
        const seasonalFactor = Math.sin(i * 0.3) * 2
        const uncertainty = (Math.random() - 0.5) * 3
        
        const predictedAging = lastData.score + trendFactor + seasonalFactor + uncertainty
        
        predictionData.push({
          time: futureDate.toISOString().split('T')[0],
          aging_score: Math.max(0, Math.min(100, Math.round(predictedAging * 10) / 10))
        })
      }
      
      return predictionData
    }

    // 设备切换处理
    const onDeviceChange = (deviceId) => {
      if (!deviceId) return
      
      selectedDeviceId.value = deviceId
      
      // 检查是否是动态生成的设备
      if (dynamicDeviceMap.value.has(deviceId)) {
        const dynamicDeviceData = dynamicDeviceMap.value.get(deviceId)
        
        // 更新设备信息
        deviceInfo.value = dynamicDeviceData.deviceInfo
        
        // 更新设备数据
        deviceData.value = dynamicDeviceData.agingData
        
        // 更新AI智能建议
        aiSuggestions.value = dynamicDeviceData.aiSuggestion
        
        // 更新老化数据和风险数据
        agingData[deviceId] = dynamicDeviceData.agingData
        riskData[deviceId] = dynamicDeviceData.riskData
        
        // 刷新图表
        updateAgingChart()
        updateFaultChart()
        
        // 重置AI预测状态
        showAIPrediction.value = false
        aiPredictionTip.value = ''
        return
      }
      
      // 处理静态设备数据
      const selectedDevice = deviceList.value.find(d => d.id === deviceId)
      if (selectedDevice) {
        deviceInfo.value = {
          deviceId: selectedDevice.id,
          deviceName: selectedDevice.name,
          model: selectedDevice.model,
          supplier: selectedDevice.supplier,
          location: selectedDevice.location,
          runningTime: selectedDevice.runtime,
          status: '正常',
          currentAgingScore: 0,
          currentRiskScore: 0
        }
      }
      
      // 获取设备数据
      const agingDataForDevice = getAgingData(deviceId)
      deviceData.value = agingDataForDevice
      
      // 更新AI智能建议
      aiSuggestions.value = aiSuggestionsData[deviceId] || ''
      
      // 更新设备当前状态
      updateDeviceCurrentStatus()
      
      // 刷新图表
      updateAgingChart()
      updateFaultChart()
      
      // 重置AI预测状态
      showAIPrediction.value = false
      aiPredictionTip.value = ''
    }
    
    // 更新设备当前状态
    const updateDeviceCurrentStatus = () => {
      if (deviceData.value.length > 0 && selectedDeviceId.value) {
        const latest = deviceData.value[deviceData.value.length - 1]
        const riskDataForDevice = getRiskData(selectedDeviceId.value)
        const latestRisk = riskDataForDevice.history[riskDataForDevice.history.length - 1] || 0
        
        deviceInfo.value.currentAgingScore = latest.score
        deviceInfo.value.currentRiskScore = latestRisk
        deviceInfo.value.status = latestRisk > 70 ? '异常' : '正常'
      }
    }
    
    // 初始化老化图表
    const initAgingChart = () => {
      if (!agingChart.value) return
      
      agingChartInstance = echarts.init(agingChart.value)
      if (deviceData.value.length === 0 && selectedDeviceId.value) {
        const agingDataForDevice = getAgingData(selectedDeviceId.value)
        deviceData.value = agingDataForDevice
      }
      
      // 使用当前设备数据
      const agingDataForChart = deviceData.value
      
      const option = {
        backgroundColor: 'transparent',
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
          backgroundColor: 'transparent',
          borderColor: '#e0e0e0',
          borderWidth: 1
        },
        xAxis: {
          type: 'category',
          data: agingDataForChart.map(item => item.time),
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          axisLabel: {
            color: '#666666',
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '老化评分',
          nameTextStyle: {
            color: '#666666'
          },
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          axisLabel: {
            color: '#666666',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          textStyle: {
            color: '#2c3e50'
          },
          formatter: function(params) {
            const p = Array.isArray(params) ? params[0] : params
            const dataIndex = p && typeof p.dataIndex === 'number' ? p.dataIndex : 0
            const current = (deviceData.value && deviceData.value[dataIndex]) || agingDataForChart[dataIndex] || {}
            const time = current.time || current.timestamp || (p && (p.axisValue || p.axisValueLabel)) || ''
            const score = (current.score ?? current.aging_score ?? (p ? p.data : undefined))
            return `
              <div style="padding: 15px; min-width: 300px;">
                <div style="border-bottom: 1px solid #e0e0e0; padding-bottom: 10px; margin-bottom: 10px;">
                  <div style="font-size: 16px; font-weight: bold; color: #2c3e50; margin-bottom: 5px;">${deviceInfo.value.deviceName}</div>
                  <div style="font-size: 12px; color: #666;">设备ID: ${deviceInfo.value.deviceId}</div>
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px;">
                  <div><strong>设备型号:</strong> ${deviceInfo.value.model}</div>
                  <div><strong>供应商:</strong> ${deviceInfo.value.supplier}</div>
                  <div><strong>安装位置:</strong> ${deviceInfo.value.location}</div>
                  <div><strong>运行时长:</strong> ${deviceInfo.value.runningTime}</div>
                </div>
                <div style="border-top: 1px solid #e0e0e0; padding-top: 10px; margin-top: 10px;">
                  <div style="font-weight: bold; color: #409EFF; margin-bottom: 5px;">老化数据 (${time})</div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px;">
                    <div><strong>老化评分:</strong> <span style="color: ${(score > 80) ? '#ff4444' : (score > 60 ? '#ffaa00' : '#00ff88')}">${score}分</span></div>
                    <div><strong>风险评分:</strong> <span style="color: ${deviceInfo.value.currentRiskScore > 70 ? '#ff4444' : deviceInfo.value.currentRiskScore > 50 ? '#ffaa00' : '#00ff88'}">${deviceInfo.value.currentRiskScore}%</span></div>
                  </div>
                </div>
              </div>
            `
          }
        },
        series: [
          {
            name: '老化评分',
            type: 'line',
            data: agingDataForChart.map(item => item.score),
            smooth: true,
            lineStyle: {
              color: '#00ff88',
              width: 3
            },
            itemStyle: {
              color: '#00ff88'
            },
            markPoint: {
              data: agingDataForChart
                .map((item, index) => ({ value: item.score, xAxis: index }))
                .filter(item => item.value > 80)
                .map(item => ({
                  ...item,
                  itemStyle: {
                    color: '#ff4444'
                  },
                  symbol: 'circle',
                  symbolSize: 8
                }))
            }
          },
          {
            name: '阈值线',
            type: 'line',
            data: new Array(agingDataForChart.length).fill(80),
            lineStyle: {
              color: '#ff4444',
              width: 2,
              type: 'dashed'
            },
            itemStyle: {
              color: 'transparent'
            },
            symbol: 'none'
          }
        ]
      }
      
      agingChartInstance.setOption(option)
    }

    // 初始化故障图表
    const initFaultChart = () => {
      if (!faultChart.value) return
      
      faultChartInstance = echarts.init(faultChart.value)
      const riskDataForDevice = selectedDeviceId.value ? getRiskData(selectedDeviceId.value) : { history: [], forecast: [] }
      
      const option = {
        backgroundColor: 'transparent',
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
          backgroundColor: 'transparent',
          borderColor: '#e0e0e0',
          borderWidth: 1
        },
        xAxis: {
          type: 'category',
          data: [...Array(riskDataForDevice.history.length).keys()].map(i => `Day ${i + 1}`),
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          axisLabel: {
            color: '#666666',
            fontSize: 12
          }
        },
        yAxis: {
          type: 'value',
          name: '风险评分(%)',
          nameTextStyle: {
            color: '#666666'
          },
          axisLine: {
            lineStyle: {
              color: '#666666'
            }
          },
          axisLabel: {
            color: '#666666',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#f0f0f0'
            }
          }
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          textStyle: {
            color: '#2c3e50'
          },
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const riskValue = riskDataForDevice.history[dataIndex]
            return `
              <div style="padding: 15px; min-width: 300px;">
                <div style="border-bottom: 1px solid #e0e0e0; padding-bottom: 10px; margin-bottom: 10px;">
                  <div style="font-size: 16px; font-weight: bold; color: #2c3e50; margin-bottom: 5px;">${deviceInfo.value.deviceName}</div>
                  <div style="font-size: 12px; color: #666;">设备ID: ${deviceInfo.value.deviceId}</div>
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px;">
                  <div><strong>设备型号:</strong> ${deviceInfo.value.model}</div>
                  <div><strong>供应商:</strong> ${deviceInfo.value.supplier}</div>
                  <div><strong>安装位置:</strong> ${deviceInfo.value.location}</div>
                  <div><strong>运行时长:</strong> ${deviceInfo.value.runningTime}</div>
                </div>
                <div style="border-top: 1px solid #e0e0e0; padding-top: 10px; margin-top: 10px;">
                  <div style="font-weight: bold; color: #409EFF; margin-bottom: 5px;">故障风险数据 (Day ${dataIndex + 1})</div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 13px;">
                    <div><strong>历史风险评分:</strong> <span style="color: ${riskValue > 70 ? '#ff4444' : riskValue > 50 ? '#ffaa00' : '#00ff88'}">${riskValue}%</span></div>
                    <div><strong>预测风险评分:</strong> <span style="color: ${riskDataForDevice.forecast[0] > 70 ? '#ff4444' : riskDataForDevice.forecast[0] > 50 ? '#ffaa00' : '#00ff88'}">${riskDataForDevice.forecast[0]}%</span></div>
                  </div>
                </div>
              </div>
            `
          }
        },
        series: [
          {
            name: '历史风险评分',
            type: 'line',
            data: riskDataForDevice.history,
            smooth: true,
            lineStyle: {
              color: '#ffaa00',
              width: 3
            },
            itemStyle: {
              color: '#ffaa00'
            },
            markPoint: {
              data: riskDataForDevice.history
                .map((value, index) => ({ value: value, xAxis: index }))
                .filter(item => item.value > 70)
                .map(item => ({
                  ...item,
                  itemStyle: {
                    color: '#ff4444'
                  },
                  symbol: 'triangle',
                  symbolSize: 12
                }))
            }
          },
          {
            name: '预测趋势',
            type: 'line',
            data: [...riskDataForDevice.history, ...riskDataForDevice.forecast],
            smooth: true,
            lineStyle: {
              color: '#00aaff',
              width: 2,
              type: 'dashed'
            },
            itemStyle: {
              color: '#00aaff'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: 'rgba(0, 170, 255, 0.3)' },
                  { offset: 1, color: 'rgba(0, 170, 255, 0.1)' }
                ]
              }
            }
          },
          {
            name: '高风险区域',
            type: 'line',
            data: new Array(riskDataForDevice.history.length).fill(70),
            lineStyle: {
              color: '#ff4444',
              width: 2,
              type: 'dashed'
            },
            itemStyle: {
              color: 'transparent'
            },
            symbol: 'none',
            markArea: {
              data: [
                [
                  { yAxis: 70 },
                  { yAxis: 100 }
                ]
              ],
              itemStyle: {
                color: 'rgba(255, 68, 68, 0.1)'
              }
            }
          }
        ]
      }
      
      faultChartInstance.setOption(option)
      
      // 添加点击事件
      faultChartInstance.on('click', (params) => {
        if (params.seriesName === '历史风险评分') {
          const dataIndex = params.dataIndex
          const riskValue = riskDataForDevice.history[dataIndex]
          selectedDevice.value = {
            deviceId: deviceInfo.value.deviceId,
            deviceName: deviceInfo.value.deviceName,
            model: deviceInfo.value.model,
            supplier: deviceInfo.value.supplier,
            location: deviceInfo.value.location,
            riskScore: riskValue,
            agingScore: deviceInfo.value.currentAgingScore
          }
          deviceDetailVisible.value = true
        }
      })
    }

    // 更新数据
    const updateData = () => {
      if (!selectedDeviceId.value) {
        ElMessage.warning('请先选择设备')
        return
      }
      
      // 显示加载状态
      const loading = ElMessage({
        message: '正在更新设备数据...',
        type: 'info',
        duration: 0
      })
      
      // 模拟数据加载延迟
      setTimeout(() => {
        const newData = getAgingData(selectedDeviceId.value)
        deviceData.value = newData
        updateDeviceCurrentStatus()
        updateAgingChart()
        updateFaultChart()
        
        loading.close()
        ElMessage.success(`设备 ${deviceInfo.value.deviceName} 数据更新成功`)
      }, 500)
    }
    
    // 触发AI预测
    const triggerAIPrediction = () => {
      if (!selectedDeviceId.value) {
        ElMessage.warning('请先选择设备')
        return
      }
      
      if (deviceData.value.length === 0) {
        ElMessage.warning('请先更新数据')
        return
      }
      
      // 显示AI预测加载状态
      const loading = ElMessage({
        message: 'AI正在分析设备数据...',
        type: 'info',
        duration: 0
      })
      
      // 模拟AI预测延迟
      setTimeout(() => {
        showAIPrediction.value = true
        const predictionData = generateAIPrediction(deviceData.value)
        
        // 生成AI预测提示
        const lastAging = deviceData.value[deviceData.value.length - 1].score
        const predictedAging = predictionData[predictionData.length - 1].aging_score
        const deviceName = deviceInfo.value.deviceName
        
        if (predictedAging > 80) {
          const daysToThreshold = Math.ceil((80 - lastAging) / ((predictedAging - lastAging) / 7))
          aiPredictionTip.value = `设备${deviceName}预计在${daysToThreshold}天后达到老化阈值，建议提前维护`
        } else {
          aiPredictionTip.value = `设备${deviceName}老化趋势正常，建议继续监控`
        }
        
        // 更新老化图表显示AI预测
        updateAgingChartWithPrediction(predictionData)
        
        loading.close()
        ElMessage.success(`设备${deviceName} AI预测完成`)
      }, 1000)
    }
    
    // 更新老化图表（包含AI预测）
    const updateAgingChartWithPrediction = (predictionData) => {
      if (agingChartInstance) {
        const allData = [...deviceData.value, ...predictionData]
        
        const option = {
          xAxis: {
            data: allData.map(item => item.time || item.timestamp)
          },
          series: [
            {
              data: allData.map(item => item.score || item.aging_score),
              markPoint: {
                data: allData
                  .map((item, index) => ({ value: item.score || item.aging_score, xAxis: index }))
                  .filter(item => item.value > 80)
                  .map(item => ({
                    ...item,
                    itemStyle: {
                      color: '#ff4444'
                    },
                    symbol: 'circle',
                    symbolSize: 8
                  }))
              }
            },
            {
              data: new Array(allData.length).fill(80)
            },
            {
              name: 'AI预测',
              type: 'line',
              data: [
                ...new Array(deviceData.value.length).fill(null),
                ...predictionData.map(item => item.aging_score)
              ],
              lineStyle: {
                color: '#9c27b0',
                width: 2,
                type: 'dashed'
              },
              itemStyle: {
                color: '#9c27b0'
              }
            }
          ]
        }
        
        agingChartInstance.setOption(option)
      }
    }
    
    // 更新老化图表
    const updateAgingChart = () => {
      if (agingChartInstance) {
        const agingData = deviceData.value
        
        const option = {
          xAxis: {
            data: agingData.map(item => item.timestamp)
          },
          series: [
            {
              data: agingData.map(item => item.aging_score),
              markPoint: {
                data: agingData
                  .map((item, index) => ({ value: item.aging_score, xAxis: index }))
                  .filter(item => item.value > 80)
                  .map(item => ({
                    ...item,
                    itemStyle: {
                      color: '#ff4444'
                    },
                    symbol: 'circle',
                    symbolSize: 8
                  }))
              }
            },
            {
              data: new Array(agingData.length).fill(80)
            }
          ]
        }
        
        agingChartInstance.setOption(option)
      }
    }

    // 更新故障图表
    const updateFaultChart = () => {
      if (faultChartInstance && selectedDeviceId.value) {
        const riskDataForDevice = getRiskData(selectedDeviceId.value)
        
        const option = {
          xAxis: {
            data: [...Array(riskDataForDevice.history.length).keys()].map(i => `Day ${i + 1}`)
          },
          series: [
            {
              data: riskDataForDevice.history,
              markPoint: {
                data: riskDataForDevice.history
                  .map((value, index) => ({ value: value, xAxis: index }))
                  .filter(item => item.value > 70)
                  .map(item => ({
                    ...item,
                    itemStyle: {
                      color: '#ff4444'
                    },
                    symbol: 'triangle',
                    symbolSize: 12
                  }))
              }
            },
            {
              data: [...riskDataForDevice.history, ...riskDataForDevice.forecast]
            },
            {
              data: new Array(riskDataForDevice.history.length).fill(70)
            }
          ]
        }
        
        faultChartInstance.setOption(option)
      }
    }
    
    
    // 获取建议类型
    const getSuggestionType = (suggestion) => {
      if (suggestion.includes('建议') && (suggestion.includes('更换') || suggestion.includes('检测') || suggestion.includes('处理'))) {
        return 'warning'
      }
      if (suggestion.includes('良好')) {
        return 'success'
      }
      return 'info'
    }

    // 刷新数据
    const refreshData = () => {
      ElMessage.success('数据刷新成功')
      updateAgingChart()
      updateFaultChart()
    }

    // 导出报告
    const exportReport = () => {
      ElMessage.info('报告导出功能开发中...')
    }

    // 关闭设备详情
    const closeDeviceDetail = () => {
      deviceDetailVisible.value = false
      selectedDevice.value = null
    }

    // 获取风险等级样式
    const getRiskLevelClass = (score) => {
      if (score >= 80) return 'risk-high'
      if (score >= 60) return 'risk-medium'
      return 'risk-low'
    }

    // 获取老化等级样式
    const getAgingLevelClass = (score) => {
      if (score >= 80) return 'aging-high'
      if (score >= 60) return 'aging-medium'
      return 'aging-low'
    }

    // 窗口大小改变时重新调整图表
    const handleResize = () => {
      if (agingChartInstance) {
        agingChartInstance.resize()
      }
      if (faultChartInstance) {
        faultChartInstance.resize()
      }
    }

    // 监听路由参数变化
    watch(() => props.deviceId, (newDeviceId) => {
      if (newDeviceId) {
        const device = findDeviceByRouteParam(newDeviceId)
        if (device) {
          selectedDeviceId.value = device.id
          onDeviceChange(selectedDeviceId.value)
        }
      }
    }, { immediate: false })

    onMounted(() => {
      nextTick(() => {
        // 检查路由查询参数
        const queryDeviceId = route.query.deviceId
        if (queryDeviceId) {
          // 从查询参数中解析设备信息
          const deviceName = route.query.deviceName || '未知设备'
          const modelName = route.query.modelName || '未知型号'
          const manufacturer = route.query.manufacturer || '未知厂商'
          const ip = route.query.ip || '0.0.0.0'
          
          // 生成动态设备数据
          const dynamicDeviceData = generateDynamicDeviceData(deviceName, modelName)
          
          // 更新设备信息中的厂商和IP
          dynamicDeviceData.deviceInfo.supplier = manufacturer
          dynamicDeviceData.deviceInfo.ipAddress = ip
          dynamicDeviceData.device.supplier = manufacturer
          
          // 将动态设备添加到设备列表中
          if (!deviceList.value.find(d => d.id === queryDeviceId)) {
            deviceList.value.push(dynamicDeviceData.device)
          }
          
          // 选择该设备
          selectedDeviceId.value = queryDeviceId
          onDeviceChange(selectedDeviceId.value)
        } else if (props.deviceId) {
          // 如果有路由参数，使用路由参数中的设备ID
          const device = findDeviceByRouteParam(props.deviceId)
          if (device) {
            selectedDeviceId.value = device.id
            onDeviceChange(selectedDeviceId.value)
          }
        } else {
          // 默认选择第一个设备
          if (deviceList.value.length > 0) {
            selectedDeviceId.value = deviceList.value[0].id
            onDeviceChange(selectedDeviceId.value)
          }
        }
        
        initAgingChart()
        initFaultChart()
      })
      window.addEventListener('resize', handleResize)
    })

    onUnmounted(() => {
      if (agingChartInstance) {
        agingChartInstance.dispose()
      }
      if (faultChartInstance) {
        faultChartInstance.dispose()
      }
      window.removeEventListener('resize', handleResize)
    })

    return {
      agingChart,
      faultChart,
      agingTimeRange,
      faultTimeRange,
      showAIPrediction,
      aiPredictionTip,
      selectedDeviceId,
      deviceList,
      deviceInfo,
      aiSuggestions,
      onDeviceChange,
      updateData,
      triggerAIPrediction,
      updateAgingChart,
      updateFaultChart,
      exportReport,
      getRiskLevelClass,
      getAgingLevelClass,
      getSuggestionType
    }
  }
}
</script>

<style scoped>
.device-aging-fault-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  color: #2c3e50;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.device-selector {
  min-width: 300px;
}

.device-select {
  width: 100%;
}

.device-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.device-info {
  font-size: 12px;
  color: #666;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: 600px;
}

.chart-section {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 600;
}

.chart-container {
  flex: 1;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

.chart-footer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e0e0e0;
}

.legend-info {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #666666;
}

.legend-color {
  width: 12px;
  height: 3px;
  border-radius: 2px;
}

.aging-line {
  background-color: #00ff88;
}

.threshold-line {
  background-color: #ff4444;
  border: 1px dashed #ff4444;
}

.warning-dot {
  background-color: #ff4444;
  border-radius: 50%;
  width: 8px;
  height: 8px;
}

.risk-line {
  background-color: #ffaa00;
}

.predicted-line {
  background-color: #00aaff;
}

.high-risk {
  background-color: #ff4444;
}

.ai-prediction {
  background-color: #9c27b0;
}

.ai-tip {
  margin-top: 10px;
}

.device-info-section {
  margin-top: 20px;
}

.device-info-card {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.device-basic-info,
.device-status-info {
  margin-bottom: 20px;
}

.device-basic-info h4,
.device-status-info h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.ai-suggestions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.ai-suggestions h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.suggestion-item {
  margin-bottom: 0;
}

.device-detail {
  padding: 10px 0;
}

/* 风险等级样式 */
.risk-high {
  color: #ff4444;
  font-weight: bold;
}

.risk-medium {
  color: #ffaa00;
  font-weight: bold;
}

.risk-low {
  color: #00ff88;
  font-weight: bold;
}

/* 老化等级样式 */
.aging-high {
  color: #ff4444;
  font-weight: bold;
}

.aging-medium {
  color: #ffaa00;
  font-weight: bold;
}

.aging-low {
  color: #00ff88;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
    height: auto;
  }
  
  .chart-section {
    min-height: 400px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .legend-info {
    flex-direction: column;
    gap: 10px;
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-dialog) {
  background: #ffffff;
  border: 1px solid #e0e0e0;
}

:deep(.el-dialog__header) {
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

:deep(.el-dialog__title) {
  color: #2c3e50;
}

:deep(.el-descriptions) {
  --el-descriptions-item-bordered-label-background: #f8f9fa;
  --el-descriptions-item-bordered-content-background: #ffffff;
  --el-descriptions-item-bordered-label-color: #2c3e50;
  --el-descriptions-item-bordered-content-color: #2c3e50;
}
</style>
