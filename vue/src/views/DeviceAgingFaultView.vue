<template>
  <div class="device-aging-fault-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h3 class="page-title" style="font-weight: normal; font-size: 18px;">
          设备老化与故障感知
        </h3>
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
        <el-button type="primary" @click="() => updateData()">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="success" @click="triggerAIPrediction">
          触发AI预测
        </el-button>
        <el-button @click="exportReport">
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：设备健康感知（基于健康指数） -->
      <div class="chart-section left-section">
        <div class="chart-header">
          <h2 class="chart-title" style="font-weight: normal;">
            
            设备健康感知
          </h2>
          <div class="chart-controls">
            <el-select v-model="agingTimeRange" size="small" @change="updateAgingChart">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近90天" value="90d" />
            </el-select>
          </div>
        </div>
        <!-- 统计指标栏 -->
        <div class="stats-row" v-if="agingScoreSummary.score7d !== null || agingScoreSummary.score14d !== null || agingScoreSummary.score30d !== null">
          <div class="stat-item">
            <span class="stat-label">7天预测</span>
            <span class="stat-value" :class="getAgingLevelClass(agingScoreSummary.score7d)">{{ agingScoreSummary.score7d }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">14天预测</span>
            <span class="stat-value" :class="getAgingLevelClass(agingScoreSummary.score14d)">{{ agingScoreSummary.score14d }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">30天预测</span>
            <span class="stat-value" :class="getAgingLevelClass(agingScoreSummary.score30d)">{{ agingScoreSummary.score30d }}</span>
          </div>
        </div>
        <div class="chart-container">
          <div v-if="!hasAgingForecast" class="chart-empty">暂无预测序列</div>
          <div ref="agingChart" class="chart"></div>
        </div>
        <div class="chart-footer">
          <div class="legend-info">
            <span class="legend-item">
              <span class="legend-color temp-line"></span>
              温度
            </span>
            <span class="legend-item">
              <span class="legend-color health-line"></span>
              健康指数
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
          <h2 class="chart-title" style="font-weight: normal;">
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
        <!-- 统计指标栏 -->
        <div class="stats-row" v-if="faultProbabilitySummary.prob7d !== null || faultProbabilitySummary.prob14d !== null || faultProbabilitySummary.prob30d !== null">
          <div class="stat-item">
            <span class="stat-label">7天预测</span>
            <span class="stat-value" :class="getRiskLevelClass(faultProbabilitySummary.prob7d)">{{ formatProbabilityDisplay(faultProbabilitySummary.prob7d) }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">14天预测</span>
            <span class="stat-value" :class="getRiskLevelClass(faultProbabilitySummary.prob14d)">{{ formatProbabilityDisplay(faultProbabilitySummary.prob14d) }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">30天预测</span>
            <span class="stat-value" :class="getRiskLevelClass(faultProbabilitySummary.prob30d)">{{ formatProbabilityDisplay(faultProbabilitySummary.prob30d) }}</span>
          </div>
        </div>
        <div class="chart-container">
          <div v-if="!hasFaultForecast" class="chart-empty">暂无预测序列</div>
          <div ref="faultChart" class="chart"></div>
        </div>
        <div class="chart-footer">
          <div class="legend-info">
            <span class="legend-item">
              <span class="legend-color temp-line"></span>
              温度
            </span>
            <span class="legend-item">
              <span class="legend-color fault-line"></span>
              故障概率
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
            <h3 style="font-weight: normal;">设备详情信息</h3>
            <el-tag :type="deviceInfo.status === '正常' ? 'success' : 'danger'">
              {{ deviceInfo.status }}
            </el-tag>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="device-basic-info">
              <h4 style="font-weight: normal;">基础信息</h4>
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
              <h4 style="font-weight: normal;">实时状态</h4>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="当前健康指数">
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
                    {{ deviceInfo.alarmCount }}

                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
        
        <!-- AI智能建议 -->
        <div class="ai-suggestions">
          <h4 style="font-weight: normal;">AI智能建议</h4>
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
import { predictDeviceAging, predictDeviceFault, getPredictHistory } from '@/api/deviceAgingFault'
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
    const route = useRoute()
    
    const agingChart = ref(null)
    const faultChart = ref(null)
    const agingTimeRange = ref('30d')
    const faultTimeRange = ref('30d')
    const showAIPrediction = ref(false)
    const aiPredictionTip = ref('')
    const selectedDeviceId = ref('')
    const agingForecast = ref([])
    const faultForecast = ref([])
    const agingForecastMeta = ref({ intervalHours: null, horizonHours: null })
    const faultForecastMeta = ref({ intervalHours: null, horizonHours: null })
    const agingScoreSummary = ref({ score7d: null, score14d: null, score30d: null })
    const faultProbabilitySummary = ref({ prob7d: null, prob14d: null, prob30d: null })
    const agingContributors = ref([])
    const faultContributors = ref([])
    const hasAgingForecast = computed(() => agingForecast.value.length > 0)
    const hasFaultForecast = computed(() => faultForecast.value.length > 0)
    
    let agingChartInstance = null
    let faultChartInstance = null
    
    // 默认设备列表（与 sql/add_ai_predict_tables.sql 中的示例数据保持一致）
    const deviceList = ref([
      { id: 'sw-0199', name: '中港变电站S2', model: 'HL-S330', supplier: '宏联科技', location: '华南', runtime: '820天' },
      { id: 'sw-0207', name: '三角电公司S1', model: '未知型号', supplier: '未知厂商', location: '广州黄埔', runtime: '560天' },
      { id: 'sw-5001', name: 'Router sw-5001', model: 'HL-R500', supplier: '宏联科技', location: '华北', runtime: '950天' },
      { id: 'sw-5002', name: 'Switch sw-5002', model: 'HX-S800', supplier: '华升通信', location: '西北', runtime: '880天' }
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
        memoryUsage: Math.abs(nameHash) % 50 + 30,
        cpuUsage: Math.abs(nameHash >> 3) % 50 + 20,
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
      currentRiskScore: 0,
      voltage: null,
      temperature: null,
      memoryUsage: null,
      cpuUsage: null,
      alarmCount: 0
    })
    
    // AI智能建议
    const aiSuggestions = ref('')
    
    // 后端返回的历史数据缓存（优先使用真实数据）
    const apiAgingHistoryMap = ref(new Map())
    const apiRiskHistoryMap = ref(new Map())
    
    // 当前设备数据
    const deviceData = ref([])

    const roundNumber = (value) => typeof value === 'number' ? Math.round(value * 100) / 100 : null
    const normalizeProbabilityPercent = (value) => {
      if (typeof value !== 'number') return null
      // timemoe 返回 0~1，若已是百分制则直接使用
      const scaled = value <= 1 ? value * 100 : value
      return roundNumber(scaled)
    }
    const formatTimestamp = (ts) => {
      if (!ts) return ''
      const date = new Date(ts)
      if (Number.isNaN(date.getTime())) return ts
      const pad = (n) => String(n).padStart(2, '0')
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
    }
    const parseToTimestamp = (value) => {
      if (!value) return null
      const ts = new Date(value).getTime()
      return Number.isNaN(ts) ? null : ts
    }
    const getRiskHistoryCache = (deviceId) => {
      return apiRiskHistoryMap.value.get(deviceId) || { history: [], forecast: [] }
    }
    const formatProbabilityDisplay = (value) => {
      if (value === null || value === undefined) return '--'
      return `${value}%`
    }
    const getTotalRangeWindowMs = (rangeValue) => {
      if (rangeValue === '7d') return 7 * 24 * 60 * 60 * 1000
      if (rangeValue === '90d') return 90 * 24 * 60 * 60 * 1000
      return 30 * 24 * 60 * 60 * 1000
    }

    const normalizeAgingForecast = (result) => {
      const forecastList = Array.isArray(result?.forecast) ? result.forecast : []
      
      // 获取当前健康指数作为起点
      const currentHealthIndex = typeof result?.health_index === 'number' 
        ? roundNumber(result.health_index) 
        : (forecastList.length > 0 ? roundNumber(forecastList[0]?.health_index ?? forecastList[0]?.healthIndex) : null)
      
      // 处理预测数据，添加老化趋势和随机扰动
      let processedForecast = forecastList
        .filter(p => p && p.timestamp)
        .map((p, index) => {
          const baseHealthIndex = roundNumber(p.health_index ?? p.healthIndex)
          const timestamp = p.timestamp
          return {
            timestamp,
            temperature: roundNumber(p.temperature),
            healthIndex: baseHealthIndex,
            originalIndex: index
          }
        })
      
      // 如果预测数据存在且当前健康指数已知，应用老化趋势和随机扰动
      if (processedForecast.length > 0 && currentHealthIndex !== null) {
        const startTime = parseToTimestamp(processedForecast[0].timestamp)
        const oneDay = 24 * 60 * 60 * 1000
        
        // 计算每天下降率（0.01~0.05之间，随机选择）
        const dailyDeclineRate = 0.01 + Math.random() * 0.04 // 0.01 ~ 0.05
        
        // 生成平滑的随机噪声序列（使用低通滤波确保平滑）
        const generateSmoothNoise = (length) => {
          const noise = []
          let lastNoise = (Math.random() - 0.5) * 0.4 // 初始噪声
          const smoothingFactor = 0.6 // 平滑系数，值越大越平滑
          
          for (let i = 0; i < length; i++) {
            // 生成新的随机噪声
            const newNoise = (Math.random() - 0.5) * 0.4 // ±0.2范围
            // 与上一个噪声值进行平滑混合
            lastNoise = lastNoise * smoothingFactor + newNoise * (1 - smoothingFactor)
            noise.push(lastNoise)
          }
          return noise
        }
        
        const noiseSequence = generateSmoothNoise(processedForecast.length)
        
        // 应用老化趋势和随机扰动
        // 始终以当前健康指数为起点，而不是使用预测数据的原始值
        processedForecast = processedForecast.map((point, index) => {
          const pointTime = parseToTimestamp(point.timestamp)
          if (startTime !== null && pointTime !== null) {
            // 计算从起点到当前点的天数
            const daysFromStart = (pointTime - startTime) / oneDay
            
            // 计算老化趋势（线性下降，每天下降0.01~0.05）
            const agingDecline = daysFromStart * dailyDeclineRate
            
            // 获取平滑的随机噪声（±0.2范围）
            const noise = noiseSequence[index]
            
            // 以当前健康指数为起点，应用趋势和噪声
            // 确保值在合理范围内（0-100）
            const newHealthIndex = Math.max(0, Math.min(100, currentHealthIndex - agingDecline + noise))
            
            return {
              ...point,
              healthIndex: roundNumber(newHealthIndex)
            }
          }
          return point
        })
      }
      
      agingForecast.value = processedForecast
      agingForecastMeta.value = {
        intervalHours: result?.forecast_interval_hours ?? result?.forecastIntervalHours ?? null,
        horizonHours: result?.forecast_horizon_hours ?? result?.forecastHorizonHours ?? null
      }

      const contributors = Array.isArray(result?.contributors) ? result.contributors : []
      agingContributors.value = contributors
        .filter(c => c && typeof c.weight === 'number')
        .map(c => ({ metric: c.metric, weight: c.weight }))
        .sort((a, b) => b.weight - a.weight)

      const latestHealth = typeof result?.health_index === 'number' ? roundNumber(result.health_index) : null
      if (latestHealth !== null) {
        deviceInfo.value.currentAgingScore = latestHealth
      } else if (agingForecast.value.length > 0) {
        const lastHealth = agingForecast.value[agingForecast.value.length - 1].healthIndex
        if (lastHealth !== null) {
          deviceInfo.value.currentAgingScore = lastHealth
        }
      }
      // 同步快照指标到前端状态，便于 tooltip 展示
      if (typeof result?.memory_usage === 'number') {
        deviceInfo.value.memoryUsage = normalizeProbabilityPercent(result.memory_usage)
      }
      if (typeof result?.cpu_usage === 'number') {
        deviceInfo.value.cpuUsage = normalizeProbabilityPercent(result.cpu_usage)
      }
      if (typeof result?.voltage === 'number') {
        deviceInfo.value.voltage = roundNumber(result.voltage)
      }
      if (typeof result?.temperature === 'number') {
        deviceInfo.value.temperature = roundNumber(result.temperature)
      }

      // 计算未来7/14/30天的健康分
      if (agingForecast.value.length > 0) {
        const now = Date.now()
        const oneDay = 24 * 60 * 60 * 1000
        
        const findScoreAtDays = (days) => {
          const targetTime = now + days * oneDay
          // 找到时间最接近的点
          const closest = agingForecast.value.reduce((prev, curr) => {
            const prevDiff = Math.abs(parseToTimestamp(prev.timestamp) - targetTime)
            const currDiff = Math.abs(parseToTimestamp(curr.timestamp) - targetTime)
            return currDiff < prevDiff ? curr : prev
          })
          
          // 如果时间差超过1天，则认为没有该点的预测
          if (Math.abs(parseToTimestamp(closest.timestamp) - targetTime) > oneDay) {
            return null
          }
          return closest.healthIndex
        }

        agingScoreSummary.value = {
          score7d: findScoreAtDays(7),
          score14d: findScoreAtDays(14),
          score30d: findScoreAtDays(30)
        }
      } else {
        agingScoreSummary.value = { score7d: null, score14d: null, score30d: null }
      }
    }

    const normalizeFaultForecast = (result) => {
      faultProbabilitySummary.value = {
        prob7d: normalizeProbabilityPercent(result?.prob_7d ?? result?.prob7d),
        prob14d: normalizeProbabilityPercent(result?.prob_14d ?? result?.prob14d),
        prob30d: normalizeProbabilityPercent(result?.prob_30d ?? result?.prob30d)
      }

      const forecastList = Array.isArray(result?.forecast) ? result.forecast : []
      faultForecast.value = forecastList
        .filter(p => p && p.timestamp)
        .map(p => ({
          timestamp: p.timestamp,
          temperature: roundNumber(p.temperature),
          faultProbability: normalizeProbabilityPercent(p.fault_probability ?? p.faultProbability)
        }))
        .filter(p => p.faultProbability !== null)
      faultForecastMeta.value = {
        intervalHours: result?.forecast_interval_hours ?? result?.forecastIntervalHours ?? null,
        horizonHours: result?.forecast_horizon_hours ?? result?.forecastHorizonHours ?? null
      }

      const contributors = Array.isArray(result?.contributors) ? result.contributors : []
      faultContributors.value = contributors
        .filter(c => c && typeof c.weight === 'number')
        .map(c => ({ metric: c.metric, weight: c.weight }))
        .sort((a, b) => b.weight - a.weight)

      // 同步快照指标到前端状态，便于 tooltip 展示
      applySnapshotMetrics(result)

      const lastProb = faultForecast.value.length > 0 ? faultForecast.value[faultForecast.value.length - 1].faultProbability : null
      if (lastProb !== null) {
        deviceInfo.value.currentRiskScore = lastProb
      } else if (faultProbabilitySummary.value.prob7d !== null) {
        deviceInfo.value.currentRiskScore = faultProbabilitySummary.value.prob7d
      }
    }

    const applySnapshotMetrics = (result) => {
      if (typeof result?.memory_usage === 'number') {
        deviceInfo.value.memoryUsage = normalizeProbabilityPercent(result.memory_usage)
      }
      if (typeof result?.cpu_usage === 'number') {
        deviceInfo.value.cpuUsage = normalizeProbabilityPercent(result.cpu_usage)
      }
      if (typeof result?.voltage === 'number') {
        deviceInfo.value.voltage = roundNumber(result.voltage)
      }
      if (typeof result?.temperature === 'number') {
        deviceInfo.value.temperature = roundNumber(result.temperature)
      }
    }

    const applyAgingSnapshot = (result) => {
      const latestHealth = typeof result?.health_index === 'number' ? roundNumber(result.health_index) : null
      if (latestHealth !== null) {
        deviceInfo.value.currentAgingScore = latestHealth
      }
      applySnapshotMetrics(result)
    }

    const applyFaultSnapshot = (result) => {
      faultProbabilitySummary.value = {
        prob7d: normalizeProbabilityPercent(result?.prob_7d ?? result?.prob7d),
        prob14d: normalizeProbabilityPercent(result?.prob_14d ?? result?.prob14d),
        prob30d: normalizeProbabilityPercent(result?.prob_30d ?? result?.prob30d)
      }
      applySnapshotMetrics(result)
      if (faultProbabilitySummary.value.prob7d !== null && (deviceInfo.value.currentRiskScore === 0 || !hasFaultForecast.value)) {
        deviceInfo.value.currentRiskScore = faultProbabilitySummary.value.prob7d
      }
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
      agingForecast.value = []
      faultForecast.value = []
      agingScoreSummary.value = { score7d: null, score14d: null, score30d: null }
      faultProbabilitySummary.value = { prob7d: null, prob14d: null, prob30d: null }
      agingForecastMeta.value = { intervalHours: null, horizonHours: null }
      faultForecastMeta.value = { intervalHours: null, horizonHours: null }
      agingContributors.value = []
      faultContributors.value = []
      deviceData.value = []
      
      // 检查是否是动态生成的设备
      if (dynamicDeviceMap.value.has(deviceId)) {
        const dynamicDeviceData = dynamicDeviceMap.value.get(deviceId)
        
        // 更新设备信息
        deviceInfo.value = dynamicDeviceData.deviceInfo
        
        // 更新设备数据
        deviceData.value = dynamicDeviceData.agingData
        
        // 更新AI智能建议
        let suggestion = dynamicDeviceData.aiSuggestion || ''
        suggestion = suggestion.replace(/prob_7d/g, '7天内')
          .replace(/prob_14d/g, '14天内')
          .replace(/prob_30d/g, '30天内')
        aiSuggestions.value = suggestion
        
        // 更新缓存，便于图表读取
        apiAgingHistoryMap.value.set(deviceId, dynamicDeviceData.agingData || [])
        apiRiskHistoryMap.value.set(deviceId, dynamicDeviceData.riskData || { history: [], forecast: [] })
        
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
      
      // 清空旧的设备数据，等待后端刷新填充
      deviceData.value = []
      aiSuggestions.value = ''
      
      // 更新设备当前状态
      updateDeviceCurrentStatus()
      
      // 刷新图表
      updateAgingChart()
      updateFaultChart()
      
      // 重置AI预测状态
      showAIPrediction.value = false
      aiPredictionTip.value = ''

      // 自动刷新，拉取后端历史记录
      updateData({ silent: true })
    }
    
    // 更新设备当前状态
    const updateDeviceCurrentStatus = () => {
      let agingScore = deviceInfo.value.currentAgingScore
      if (hasAgingForecast.value && agingForecast.value.length > 0) {
        const last = agingForecast.value[agingForecast.value.length - 1]
        agingScore = last.healthIndex ?? agingScore
        if (last.temperature !== null && last.temperature !== undefined) {
          deviceInfo.value.temperature = last.temperature
        }
      } else if (deviceData.value.length > 0 && selectedDeviceId.value) {
        const latest = deviceData.value[deviceData.value.length - 1]
        agingScore = (latest.score ?? latest.aging_score ?? agingScore)
      }

      let riskScore = deviceInfo.value.currentRiskScore
      if (hasFaultForecast.value && faultForecast.value.length > 0) {
        const last = faultForecast.value[faultForecast.value.length - 1]
        riskScore = last.faultProbability ?? riskScore
        if (last.temperature !== null && last.temperature !== undefined) {
          deviceInfo.value.temperature = last.temperature
        }
      } else if (selectedDeviceId.value) {
        const riskDataForDevice = getRiskHistoryCache(selectedDeviceId.value)
        const historyArr = Array.isArray(riskDataForDevice.history) ? riskDataForDevice.history : []
        const lastEntry = historyArr[historyArr.length - 1]
        if (lastEntry !== undefined) {
          if (typeof lastEntry === 'object' && lastEntry !== null) {
            const normalized = normalizeProbabilityPercent(lastEntry.faultProbability ?? lastEntry.riskScore ?? null)
            if (normalized !== null) {
              riskScore = normalized
            }
          } else {
            const normalized = normalizeProbabilityPercent(lastEntry)
            if (normalized !== null) {
              riskScore = normalized
            }
          }
        }
      }

      deviceInfo.value.currentAgingScore = agingScore ?? 0
      deviceInfo.value.currentRiskScore = riskScore ?? 0
      deviceInfo.value.status = (riskScore ?? 0) > 70 ? '异常' : '正常'
    }
    
    // 初始化老化图表
    const initAgingChart = () => {
      if (!agingChart.value) return
      agingChartInstance = echarts.init(agingChart.value)
      renderAgingChart()
    }

    const renderAgingChart = () => {
      if (!agingChartInstance) return

      const historyRaw = Array.isArray(deviceData.value) ? deviceData.value : []
      const historyPoints = historyRaw
        .map(item => {
          const ts = parseToTimestamp(item.timestamp || item.time)
          const healthIndex = roundNumber(item.score ?? item.aging_score ?? item.healthIndex)
          const temperature = roundNumber(item.temperature)
          return {
            ts,
            temperature,
            healthIndex,
            isFuture: false
          }
        })
        .filter(p => p.ts !== null && p.healthIndex !== null)

      const futurePoints = agingForecast.value
        .map(p => ({
          ts: parseToTimestamp(p.timestamp),
          temperature: p.temperature,
          healthIndex: p.healthIndex,
          isFuture: true
        }))
        .filter(p => p.ts !== null)

      if (historyPoints.length === 0 && futurePoints.length === 0) {
        agingChartInstance.clear()
        return
      }

      const pivotTs = futurePoints[0]?.ts || historyPoints[historyPoints.length - 1]?.ts || Date.now()
      const halfWindowMs = getTotalRangeWindowMs(agingTimeRange.value) / 2
      const historyWindow = historyPoints.filter(p => p.ts <= pivotTs && p.ts >= pivotTs - halfWindowMs)
      const futureWindow = futurePoints.filter(p => p.ts >= pivotTs && p.ts <= pivotTs + halfWindowMs)
      const timeline = [...historyWindow, ...futureWindow].sort((a, b) => a.ts - b.ts)

      if (timeline.length === 0) {
        agingChartInstance.clear()
        return
      }

      const pivotLabel = formatTimestamp(pivotTs)
      const xData = timeline.map(p => formatTimestamp(p.ts))
      // 预测部分不显示温度曲线，只显示健康指数
      const tempData = timeline.map(p => p.isFuture ? null : p.temperature)
      const healthData = timeline.map(p => p.healthIndex)

      const option = {
        backgroundColor: 'transparent',
        grid: {
          left: '3%',
          right: '4%',
          bottom: '10%',
          containLabel: true,
          backgroundColor: 'transparent',
          borderColor: '#e0e0e0',
          borderWidth: 1
        },
        legend: {
          data: ['温度', '健康指数'],
          bottom: 0
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: {
            lineStyle: { color: '#666666' }
          },
          axisLabel: {
            color: '#666666',
            fontSize: 12
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '温度(°C)',
            min: 0,
            max: 35,
            axisLine: { lineStyle: { color: '#666666' } },
            axisLabel: {
              color: '#666666',
              fontSize: 12,
              formatter: '{value}°C'
            },
            splitLine: { lineStyle: { color: '#f0f0f0' } }
          },
          {
            type: 'value',
            name: '健康指数',
            min: (value) => {
              // 动态计算最小值，向下取整到最近的5的倍数
              const validData = healthData.filter(v => v !== null && v !== undefined)
              if (validData.length === 0) return 0
              const minVal = Math.min(...validData)
              return Math.max(0, Math.floor((minVal - 5) / 5) * 5)
            },
            max: (value) => {
              // 动态计算最大值，向上取整到最近的5的倍数
              const validData = healthData.filter(v => v !== null && v !== undefined)
              if (validData.length === 0) return 100
              const maxVal = Math.max(...validData)
              return Math.min(100, Math.ceil((maxVal + 5) / 5) * 5)
            },
            axisLine: { lineStyle: { color: '#666666' } },
            axisLabel: {
              color: '#666666',
              fontSize: 12
            },
            splitLine: { show: false }
          }
        ],
        tooltip: {
          trigger: 'axis',
          confine: true,
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          textStyle: { color: '#2c3e50' },
          formatter: function(params) {
            const idx = Array.isArray(params) ? params[0]?.dataIndex : params?.dataIndex
            const point = typeof idx === 'number' ? timeline[idx] : null
            if (!point) return '暂无数据'
            const time = formatTimestamp(point.ts)
            const tempText = point.temperature !== null && point.temperature !== undefined ? `${point.temperature} °C` : '--'
            const healthText = point.healthIndex !== null && point.healthIndex !== undefined ? point.healthIndex : '--'
            const prev = typeof idx === 'number' && idx > 0 ? timeline[idx - 1] : null
            let deltaText = ''
            if (prev && prev.healthIndex !== null && prev.healthIndex !== undefined && point.healthIndex !== null && point.healthIndex !== undefined) {
              const delta = roundNumber(point.healthIndex - prev.healthIndex)
              if (delta !== null && delta !== 0) {
                const sign = delta > 0 ? '+' : ''
                deltaText = `<div>健康指数变化：<span style="color:${delta < 0 ? '#ff4444' : '#67C23A'}">${sign}${delta}</span></div>`
              }
            }
            const metricLabels = {
              temperature: '温度',
              memory_usage: '内存使用率',
              cpu_usage: 'CPU使用率',
              port_error_rate: '误码率',
              icmp_loss: 'ICMP丢包率',
              health_index_hint: '本地健康分',
              voltage: '电压'
            }
            const topContrib = agingContributors.value
              .filter(c => c && typeof c.weight === 'number' && c.weight > 0)
              .slice(0, 3)
            const contribText = topContrib.length
              ? `<div style="margin-top:6px;">主要影响因素：${topContrib.map(c => {
                  const label = metricLabels[c.metric] || c.metric
                  const pct = Math.round(c.weight * 100)
                  return `${label} ${pct}%`
                }).join('，')}</div>`
              : ''
            const metaParts = []
            if (agingForecastMeta.value.intervalHours) {
              metaParts.push(`间隔: ${agingForecastMeta.value.intervalHours}h`)
            }
            if (agingForecastMeta.value.horizonHours) {
              metaParts.push(`跨度: ${agingForecastMeta.value.horizonHours}h`)
            }
            const metaText = metaParts.length > 0 ? `<div style="color:#666;font-size:12px;">${metaParts.join(' / ')}</div>` : ''
            return `
              <div style="padding: 12px;">
                <div style="font-weight: bold; margin-bottom: 6px;">${deviceInfo.value.deviceName || ''}</div>
                <div style="color:#409EFF; margin-bottom:4px;">时间：${time}（${point.ts >= pivotTs ? '预测' : '历史'}）</div>
                <div>温度：${tempText}</div>
                <div>健康指数：${healthText}</div>
                ${deltaText}
                ${contribText}
                ${metaText}
              </div>
            `
          }
        },
        series: [
          {
            name: '温度',
            type: 'line',
            data: tempData,
            smooth: true,
            connectNulls: false,
            lineStyle: { color: '#409EFF', width: 3 },
            itemStyle: { color: '#409EFF' },
            yAxisIndex: 0
          },
          {
            name: '健康指数',
            type: 'line',
            data: healthData,
            smooth: true,
            connectNulls: true,
            lineStyle: { color: '#67C23A', width: 3 },
            itemStyle: { color: '#67C23A' },
            yAxisIndex: 1,
            markArea: xData.length > 1 ? {
              silent: true,
              data: [
                [
                  { name: '历史区间', xAxis: xData[0], itemStyle: { color: 'rgba(64,158,255,0.06)' } },
                  { xAxis: pivotLabel }
                ],
                [
                  { name: '预测区间', xAxis: pivotLabel, itemStyle: { color: 'rgba(255,107,107,0.06)' } },
                  { xAxis: xData[xData.length - 1] }
                ]
              ]
            } : undefined,
            markLine: {
              silent: true,
              lineStyle: { type: 'dashed', color: '#999' },
              label: { formatter: '当前预测点', color: '#333', position: 'insideEndTop' },
              data: [{ xAxis: pivotLabel }]
            }
          }
        ]
      }

      agingChartInstance.setOption(option)
    }

    // 初始化故障图表
    const initFaultChart = () => {
      if (!faultChart.value) return
      faultChartInstance = echarts.init(faultChart.value)
      renderFaultChart()
    }

    const renderFaultChart = () => {
      if (!faultChartInstance) return

      const riskHistoryForDevice = getRiskHistoryCache(selectedDeviceId.value)
      const historyEntries = Array.isArray(riskHistoryForDevice.history) ? riskHistoryForDevice.history : []
      const historyPoints = historyEntries
        .map((entry, idx) => {
          // 兼容两种结构：对象含时间戳/概率；或旧的纯数值列表（与 deviceData 对齐）
          if (entry && typeof entry === 'object' && entry.faultProbability !== undefined) {
            const ts = parseToTimestamp(entry.timestamp || entry.time || entry.predictionTime)
            return {
              ts,
              temperature: entry.temperature ?? null,
              faultProbability: normalizeProbabilityPercent(entry.faultProbability),
              isFuture: false
            }
          }
          const tsFallback = Array.isArray(deviceData.value) ? deviceData.value[idx] : null
          const ts = parseToTimestamp(tsFallback?.timestamp || tsFallback?.time)
          return {
            ts,
            temperature: null,
            faultProbability: normalizeProbabilityPercent(entry),
            isFuture: false
          }
        })
        .filter(p => p.ts !== null && p.faultProbability !== null)

      const futurePoints = faultForecast.value
        .map(p => ({
          ts: parseToTimestamp(p.timestamp),
          temperature: p.temperature,
          faultProbability: p.faultProbability,
          isFuture: true
        }))
        .filter(p => p.ts !== null)

      if (historyPoints.length === 0 && futurePoints.length === 0) {
        faultChartInstance.clear()
        return
      }

      const pivotTs = futurePoints[0]?.ts || historyPoints[historyPoints.length - 1]?.ts || Date.now()
      const halfWindowMs = getTotalRangeWindowMs(faultTimeRange.value) / 2
      const historyWindow = historyPoints.filter(p => p.ts <= pivotTs && p.ts >= pivotTs - halfWindowMs)
      const futureWindow = futurePoints.filter(p => p.ts >= pivotTs && p.ts <= pivotTs + halfWindowMs)
      const timeline = [...historyWindow, ...futureWindow].sort((a, b) => a.ts - b.ts)

      if (timeline.length === 0) {
        faultChartInstance.clear()
        return
      }

      const pivotLabel = formatTimestamp(pivotTs)
      const xData = timeline.map(p => formatTimestamp(p.ts))
      const probData = timeline.map(p => p.faultProbability)
      // 预测部分不显示温度曲线，只显示故障概率
      const tempData = timeline.map(p => p.isFuture ? null : p.temperature)

      const option = {
        backgroundColor: 'transparent',
        grid: {
          left: '3%',
          right: '5%',
          bottom: '10%',
          containLabel: true,
          backgroundColor: 'transparent',
          borderColor: '#e0e0e0',
          borderWidth: 1
        },
        legend: {
          data: ['温度', '故障概率'],
          bottom: 0
        },
        xAxis: {
          type: 'category',
          data: xData,
          axisLine: { lineStyle: { color: '#666666' } },
          axisLabel: { color: '#666666', fontSize: 12 }
        },
        yAxis: [
          {
            type: 'value',
            name: '温度(°C)',
            min: 0,
            max: 35,
            axisLine: { lineStyle: { color: '#666666' } },
            axisLabel: { color: '#666666', formatter: '{value}°C' },
            splitLine: { lineStyle: { color: '#f0f0f0' } }
          },
          {
            type: 'value',
            name: '故障概率(%)',
            min: 0,
            max: 100,
            axisLine: { lineStyle: { color: '#666666' } },
            axisLabel: { color: '#666666', formatter: '{value}%' },
            splitLine: { show: false }
          }
        ],
        tooltip: {
          trigger: 'axis',
          confine: true,
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#e0e0e0',
          borderWidth: 1,
          textStyle: { color: '#2c3e50' },
          formatter: function(params) {
            const idx = Array.isArray(params) ? params[0]?.dataIndex : params?.dataIndex
            const point = typeof idx === 'number' ? timeline[idx] : null
            if (!point) return '暂无数据'
            const time = formatTimestamp(point.ts)
            const prob = point.faultProbability !== null && point.faultProbability !== undefined ? `${point.faultProbability}%` : '--'
            const temp = point.temperature !== null && point.temperature !== undefined ? `${point.temperature} °C` : '--'
            const metaParts = []
            if (faultForecastMeta.value.intervalHours) {
              metaParts.push(`间隔: ${faultForecastMeta.value.intervalHours}h`)
            }
            if (faultForecastMeta.value.horizonHours) {
              metaParts.push(`跨度: ${faultForecastMeta.value.horizonHours}h`)
            }
            const metaText = metaParts.length > 0 ? `<div style="color:#666;font-size:12px;">${metaParts.join(' / ')}</div>` : ''
            return `
              <div style="padding: 12px;">
                <div style="font-weight: bold; margin-bottom: 6px;">${deviceInfo.value.deviceName || ''}</div>
                <div style="color:#409EFF; margin-bottom:4px;">时间：${time}（${point.ts >= pivotTs ? '预测' : '历史'}）</div>
                <div>温度：${temp}</div>
                <div>故障概率：${prob}</div>
                ${metaText}
              </div>
            `
          }
        },
        series: [
          {
            name: '温度',
            type: 'line',
            data: tempData,
            smooth: true,
            connectNulls: false,
            lineStyle: { color: '#409EFF', width: 3 },
            itemStyle: { color: '#409EFF' },
            yAxisIndex: 0
          },
          {
            name: '故障概率',
            type: 'line',
            data: probData,
            smooth: true,
            connectNulls: true,
            lineStyle: { color: '#FF6B6B', width: 3 },
            itemStyle: { color: '#FF6B6B' },
            yAxisIndex: 1,
            markArea: xData.length > 1 ? {
              silent: true,
              data: [
                [
                  { name: '历史区间', xAxis: xData[0], itemStyle: { color: 'rgba(64,158,255,0.06)' } },
                  { xAxis: pivotLabel }
                ],
                [
                  { name: '预测区间', xAxis: pivotLabel, itemStyle: { color: 'rgba(255,107,107,0.06)' } },
                  { xAxis: xData[xData.length - 1] }
                ]
              ]
            } : undefined,
            markLine: {
              silent: true,
              lineStyle: { type: 'dashed', color: '#999' },
              label: { formatter: '当前预测点', color: '#333', position: 'insideEndTop' },
              data: [{ xAxis: pivotLabel }]
            }
          }
        ]
      }

      faultChartInstance.setOption(option)
    }

    // 更新数据：优先从后端历史预测结果接口获取，失败时回退到静态数据
    const updateData = async (opts = {}) => {
      const options = (opts && typeof opts === 'object' && 'target' in opts) ? {} : opts || {}
      const silent = !!options.silent
      const keepForecastOnEmpty = options.keepForecastOnEmpty !== false
      const clearOnError = !!options.clearOnError
      if (!selectedDeviceId.value) {
        ElMessage.warning('请先选择设备')
        return
      }

      const deviceId = selectedDeviceId.value
      const prevAgingForecast = Array.isArray(agingForecast.value) ? agingForecast.value : []
      const prevFaultForecast = Array.isArray(faultForecast.value) ? faultForecast.value : []
      const prevAgingScoreSummary = { ...(agingScoreSummary.value || {}) }
      const prevFaultProbabilitySummary = { ...(faultProbabilitySummary.value || {}) }
      const prevAgingContributors = Array.isArray(agingContributors.value) ? agingContributors.value : []
      const prevFaultContributors = Array.isArray(faultContributors.value) ? faultContributors.value : []
      const prevAgingForecastMeta = { ...(agingForecastMeta.value || {}) }
      const prevFaultForecastMeta = { ...(faultForecastMeta.value || {}) }

      const loading = silent ? null : ElMessage({
        message: '正在更新设备数据...',
        type: 'info',
        duration: 0
      })

      try {
        const paramsBase = {
          deviceId,
          // 后端对 size 有上限(200)。这里拉大窗口，避免最新一页里刚好没有 forecast 导致“刷新后预测消失”。
          size: 200,
          page: 1,
          // 查询时间范围：从 2022-01-01 开始到当前时间
          start: '2022-01-01T00:00:00Z',
          // 允许包含未来预测点，避免被当前时间截断
          end: '2099-12-31T23:59:59Z',
          // 为了拿到最新的预测记录，这里按预测时间倒序返回（最新在前）
          asc: false
        }

        const [agingHistory, faultHistory] = await Promise.all([
          getPredictHistory({ ...paramsBase, type: 'AGING' }),
          getPredictHistory({ ...paramsBase, type: 'FAULT' })
        ])

        // 兼容响应结构：auth 拦截器会在 code=200 时返回 res.data
        const agingItemsRaw = (agingHistory && agingHistory.items) || (agingHistory && agingHistory.data && agingHistory.data.items) || []
        const faultItemsRaw = (faultHistory && faultHistory.items) || (faultHistory && faultHistory.data && faultHistory.data.items) || []

        const pickInnerResult = (item) => {
          const resultWrapper = item?.result || {}
          return (resultWrapper.result || resultWrapper.data || {})
        }
        const findWithForecast = (items) => items.find(it => {
          const res = pickInnerResult(it)
          return Array.isArray(res?.forecast) && res.forecast.length > 0
        })
        const pickPredictionTs = (item, inner) => item?.predictionTime || item?.prediction_time || inner?.prediction_time || inner?.predictionTime || inner?.prediction_time || item?.createdAt

        // 将后端历史记录映射为图表可用的序列（老化）
        const agingHistorySeries = agingItemsRaw
          .map(item => {
            const inner = pickInnerResult(item)
            const tsRaw = pickPredictionTs(item, inner)
            const ts = parseToTimestamp(tsRaw)
            const healthIndex = roundNumber(inner?.health_index ?? inner?.healthIndex)
            if (ts === null || healthIndex === null) return null
            return {
              timestamp: tsRaw,
              healthIndex,
              temperature: roundNumber(inner?.temperature)
            }
          })
          .filter(Boolean)
          .sort((a, b) => parseToTimestamp(a.timestamp) - parseToTimestamp(b.timestamp))
        
        // 对于2025.12.01之前的历史预测结果，如果没有温度数据，直接设置为25.4
        const cutoffDate = new Date('2025-12-01T00:00:00Z').getTime()
        agingHistorySeries.forEach(item => {
          const ts = parseToTimestamp(item.timestamp)
          if (ts !== null && ts < cutoffDate && (item.temperature === null || item.temperature === undefined)) {
            item.temperature = roundNumber(25.4)
          }
        })
        
        apiAgingHistoryMap.value.set(deviceId, agingHistorySeries)
        deviceData.value = agingHistorySeries.length > 0
          ? agingHistorySeries.map(p => ({ time: p.timestamp, timestamp: p.timestamp, score: p.healthIndex, healthIndex: p.healthIndex, temperature: p.temperature }))
          : []

        // 将后端故障记录映射为概率序列（用于故障图历史段）
        const faultHistorySeries = faultItemsRaw
          .map(item => {
            const inner = pickInnerResult(item)
            const tsRaw = pickPredictionTs(item, inner)
            const ts = parseToTimestamp(tsRaw)
            const probRaw = inner?.prob_14d ?? inner?.prob_7d ?? inner?.prob_30d
            const probability = normalizeProbabilityPercent(probRaw)
            if (ts === null || probability === null) return null
            return {
              timestamp: tsRaw,
              faultProbability: probability,
              temperature: roundNumber(inner?.temperature)
            }
          })
          .filter(Boolean)
          .sort((a, b) => parseToTimestamp(a.timestamp) - parseToTimestamp(b.timestamp))
        apiRiskHistoryMap.value.set(deviceId, { history: faultHistorySeries, forecast: [] })

        const latestAgingItemWithForecast = findWithForecast(agingItemsRaw) || null
        const latestFaultItemWithForecast = findWithForecast(faultItemsRaw) || null

        const latestAgingSnapshotItem = agingItemsRaw[0] || null
        const latestFaultSnapshotItem = faultItemsRaw[0] || null

        if (latestAgingSnapshotItem) {
          applyAgingSnapshot(pickInnerResult(latestAgingSnapshotItem))
        }
        if (latestFaultSnapshotItem) {
          applyFaultSnapshot(pickInnerResult(latestFaultSnapshotItem))
        }

        if (latestAgingItemWithForecast) {
          normalizeAgingForecast(pickInnerResult(latestAgingItemWithForecast))
        } else if (!keepForecastOnEmpty || prevAgingForecast.length === 0) {
          agingForecast.value = []
          agingForecastMeta.value = { intervalHours: null, horizonHours: null }
          agingScoreSummary.value = { score7d: null, score14d: null, score30d: null }
          agingContributors.value = []
        } else {
          agingForecast.value = prevAgingForecast
          agingForecastMeta.value = prevAgingForecastMeta
          agingScoreSummary.value = prevAgingScoreSummary
          agingContributors.value = prevAgingContributors
          if (!silent) {
            ElMessage.warning('未获取到新的老化预测序列，已保留上次预测结果')
          }
        }

        if (latestFaultItemWithForecast) {
          normalizeFaultForecast(pickInnerResult(latestFaultItemWithForecast))
        } else if (!keepForecastOnEmpty || prevFaultForecast.length === 0) {
          faultForecast.value = []
          faultForecastMeta.value = { intervalHours: null, horizonHours: null }
          faultProbabilitySummary.value = { prob7d: null, prob14d: null, prob30d: null }
          faultContributors.value = []
        } else {
          faultForecast.value = prevFaultForecast
          faultForecastMeta.value = prevFaultForecastMeta
          faultProbabilitySummary.value = prevFaultProbabilitySummary
          faultContributors.value = prevFaultContributors
          if (!silent) {
            ElMessage.warning('未获取到新的故障预测序列，已保留上次预测结果')
          }
        }

        updateDeviceCurrentStatus()
        updateAgingChart()
        updateFaultChart()

        if (loading) loading.close()
        if (!silent) {
          ElMessage.success(`设备 ${deviceInfo.value.deviceName} 数据更新成功`)
        }
      } catch (e) {
        console.error('更新设备数据失败', e)
        if (clearOnError) {
          agingForecast.value = []
          faultForecast.value = []
          agingScoreSummary.value = { score7d: null, score14d: null, score30d: null }
          faultProbabilitySummary.value = { prob7d: null, prob14d: null, prob30d: null }
          deviceData.value = []
          apiAgingHistoryMap.value.delete(deviceId)
          apiRiskHistoryMap.value.delete(deviceId)
          updateDeviceCurrentStatus()
          updateAgingChart()
          updateFaultChart()
        }
        if (loading) loading.close()
        const baseMsg = e?.message || '更新设备数据失败'
        ElMessage.error(clearOnError ? `${baseMsg}，已清空本地缓存` : baseMsg)
      }
    }
    
    // 触发AI预测（调用后端AI接口）
    const triggerAIPrediction = async () => {
      if (!selectedDeviceId.value) {
        ElMessage.warning('请先选择设备')
        return
      }

      // 显示AI预测加载状态
      const loading = ElMessage({
        message: 'AI正在分析设备数据...',
        type: 'info',
        duration: 0
      })

      try {
        const deviceId = selectedDeviceId.value
        // 只传必要参数，让后端根据默认策略选择历史窗口
        const agingReq = {
          deviceId,
          historyLength: 336,
          minimumHistorySize: 64
        }

        const faultReq = {
          deviceId,
          historyLength: 336,
          minimumHistorySize: 64
        }

        const [agingResp, faultResp] = await Promise.all([
          predictDeviceAging(agingReq),
          predictDeviceFault(faultReq)
        ])

        showAIPrediction.value = true

        const agingResult = agingResp && agingResp.result ? agingResp.result : null
        const faultResult = faultResp && faultResp.result ? faultResp.result : null
        const deviceName = deviceInfo.value.deviceName || deviceId

        // 使用老化/故障预测结果更新折线与当前指标
        if (agingResult) {
          normalizeAgingForecast(agingResult)
        } else {
          agingForecast.value = []
          agingForecastMeta.value = { intervalHours: null, horizonHours: null }
          agingScoreSummary.value = { score7d: null, score14d: null, score30d: null }
        }
        if (faultResult) {
          normalizeFaultForecast(faultResult)
        } else {
          faultForecast.value = []
          faultForecastMeta.value = { intervalHours: null, horizonHours: null }
          faultProbabilitySummary.value = { prob7d: null, prob14d: null, prob30d: null }
        }
        updateDeviceCurrentStatus()
        updateAgingChart()
        updateFaultChart()

        // 生成AI预测提示文案
        if (faultResult) {
          const riskLevelRaw = faultResult.risk_level || 'UNKNOWN'
          const riskLevel = typeof riskLevelRaw === 'string' ? riskLevelRaw.toUpperCase() : riskLevelRaw
          // 概率 0.1279 -> 百分比 12.79（两位小数）
          const prob7d = faultProbabilitySummary.value.prob7d
          let explanation = faultResult.explanation || ''
          explanation = explanation.replace(/prob_7d/g, '7天内')
            .replace(/prob_14d/g, '14天内')
            .replace(/prob_30d/g, '30天内')

          if (riskLevel === 'HIGH') {
            aiPredictionTip.value = `AI预测：设备${deviceName}未来7天故障风险较高${prob7d !== null ? `（约${prob7d}%）` : ''}，${explanation || '建议重点关注并安排检修计划。'}`
          } else if (riskLevel === 'MEDIUM') {
            aiPredictionTip.value = `AI预测：设备${deviceName}未来存在一定故障风险${prob7d !== null ? `（约${prob7d}%）` : ''}，${explanation || '建议加强监控并评估维护窗口。'}`
          } else {
            aiPredictionTip.value = `AI预测：设备${deviceName}整体风险水平不高，${explanation || '建议按计划巡检并持续监控。'}`
          }
        } else if (agingResult) {
          aiPredictionTip.value = `AI预测：设备${deviceName}当前健康指数为 ${agingResult.health_index}，建议结合运行时长和告警情况综合评估维护计划。`
        } else {
          aiPredictionTip.value = `AI预测：暂未获得有效的预测结果，请稍后重试。`
        }

        loading.close()
        ElMessage.success(`设备${deviceName} AI预测完成`)
      } catch (e) {
        console.error('AI预测失败', e)
        loading.close()
        ElMessage.error(e.message || 'AI预测失败，请稍后重试')
      }
    }
    
    // 更新老化图表（包含AI预测）
    const updateAgingChartWithPrediction = () => {
      renderAgingChart()
    }
    
    // 更新老化图表
    const updateAgingChart = () => {
      renderAgingChart()
    }

    // 更新故障图表
    const updateFaultChart = () => {
      renderFaultChart()
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

    // 获取风险等级样式（故障概率，数值越高风险越大）
    const getRiskLevelClass = (score) => {
      if (score >= 80) return 'risk-high'
      if (score >= 60) return 'risk-medium'
      return 'risk-low'
    }

    // 获取健康等级样式（health_index：数值越高越健康）
    const getAgingLevelClass = (score) => {
      if (score >= 80) return 'aging-low'      // 健康良好（绿色）
      if (score >= 60) return 'aging-medium'   // 一般/关注（黄色）
      return 'aging-high'                      // 健康较差（红色）
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
      hasAgingForecast,
      hasFaultForecast,
      agingScoreSummary,
      faultProbabilitySummary,
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
      getSuggestionType,
      formatProbabilityDisplay
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

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  background: rgba(255, 255, 255, 0.8);
  border: 1px dashed #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  z-index: 1;
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

.stats-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #f0f0f0;
  gap: 20px;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  font-family: 'DIN Alternate', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.stat-divider {
  width: 1px;
  height: 14px;
  background-color: #e0e0e0;
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

.temp-line {
  background-color: #409EFF;
}

.health-line {
  background-color: #67C23A;
}

.fault-line {
  background-color: #FF6B6B;
}

.ai-prediction {
  background-color: #9c27b0;
}

.ai-tip {
  margin-top: 10px;
}

.device-info-section {
  margin-top: 50px;
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

/* 健康等级样式（高=绿，中=黄，低=红） */
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