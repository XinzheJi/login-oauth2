<template>
  <div class="vibration-analysis-container">
    <div class="main-content">
      <!-- Left: Real-time Waveform Monitor -->
      <el-card class="waveform-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><Monitor /></el-icon> 实时光纤传感波形 (Real-time DAS)</span>
            <el-tag :type="statusType" effect="dark">{{ statusText }}</el-tag>
          </div>
        </template>
        <div ref="waveformChartRef" class="waveform-chart"></div>
      </el-card>

      <!-- Right: AI Inference Panel -->
      <div class="ai-panel">
        <!-- AI Result -->
        <el-card class="result-card" shadow="hover">
          <div class="ai-result-box" :class="statusClass">
            <div class="model-name">AI Model: CNN-LSTM-v2</div>
            <div class="result-main">{{ resultTitle }}</div>
            <div class="confidence">置信度: {{ confidence }}%</div>
          </div>
        </el-card>

        <!-- Feature Analysis Radar -->
        <el-card class="radar-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><DataAnalysis /></el-icon> 特征指纹分析</span>
            </div>
          </template>
          <div ref="radarChartRef" class="radar-chart"></div>
        </el-card>

        <!-- Hazard Assessment -->
        <el-card class="hazard-card" shadow="hover">
          <div class="hazard-grid">
            <div class="hazard-item">
              <div class="label">纵向深度</div>
              <div class="value">{{ depth }} <span class="unit">m</span></div>
            </div>
            <div class="hazard-item">
              <div class="label">横向距离</div>
              <div class="value">{{ distance }} <span class="unit">m</span></div>
            </div>
            <div class="hazard-item">
              <div class="label">危害等级</div>
              <div class="value" :style="{ color: hazardColor }">{{ hazardLevel }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- Bottom: Simulation Control -->
    <el-card class="control-card" shadow="always">
      <div class="control-row">
        <div class="control-label">样本库模拟输入:</div>
        <el-button-group>
          <el-button type="success" @click="simulateVehicle">输入：车辆经过数据</el-button>
          <el-button type="warning" @click="simulateManualDigging">输入：人工挖掘数据</el-button>
          <el-button type="danger" @click="simulateMachineDigging">输入：机械施工数据</el-button>
        </el-button-group>
        <el-button @click="resetSimulation" style="margin-left: 20px;">停止/重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { Monitor, DataAnalysis } from '@element-plus/icons-vue'

// --- Refs & State ---
const waveformChartRef = ref(null)
const radarChartRef = ref(null)
let waveformChart = null
let radarChart = null
let timer = null

const currentMode = ref('IDLE') // IDLE, VEHICLE, MANUAL, MACHINE
const confidence = ref(0)
const depth = ref(0)
const distance = ref(0)

// --- Computed UI State ---
const statusText = computed(() => {
  if (currentMode.value === 'IDLE') return '待机监测中'
  if (currentMode.value === 'VEHICLE') return '监测中: 车辆经过'
  if (currentMode.value === 'MANUAL') return '监测中: 疑似挖掘'
  if (currentMode.value === 'MACHINE') return '警告: 机械施工'
  return ''
})

const statusType = computed(() => {
  if (currentMode.value === 'IDLE') return 'info'
  if (currentMode.value === 'VEHICLE') return 'success'
  if (currentMode.value === 'MANUAL') return 'warning'
  if (currentMode.value === 'MACHINE') return 'danger'
  return 'info'
})

const statusClass = computed(() => {
  if (currentMode.value === 'IDLE') return 'bg-idle'
  if (currentMode.value === 'VEHICLE') return 'bg-safe'
  if (currentMode.value === 'MANUAL') return 'bg-warning'
  if (currentMode.value === 'MACHINE') return 'bg-danger'
  return ''
})

const resultTitle = computed(() => {
  if (currentMode.value === 'IDLE') return '无明显振动'
  if (currentMode.value === 'VEHICLE') return '普通交通振动'
  if (currentMode.value === 'MANUAL') return '人工挖掘'
  if (currentMode.value === 'MACHINE') return '大型机械施工'
  return 'Unknown'
})

const hazardLevel = computed(() => {
  if (currentMode.value === 'IDLE' || currentMode.value === 'VEHICLE') return '低 (安全)'
  if (currentMode.value === 'MANUAL') return '中 (需关注)'
  if (currentMode.value === 'MACHINE') return '高 (立即处置)'
  return '-'
})

const hazardColor = computed(() => {
  if (currentMode.value === 'MANUAL') return '#E6A23C'
  if (currentMode.value === 'MACHINE') return '#F56C6C'
  return '#67C23A'
})

// --- Mock Data Generators ---
const waveData = ref(new Array(100).fill(0))
const xAxisData = new Array(100).fill('').map((_, i) => i)

const generateWavePoint = () => {
  let base = 0
  const random = (Math.random() - 0.5)
  
  if (currentMode.value === 'IDLE') {
    base = random * 2
  } else if (currentMode.value === 'VEHICLE') {
    // Periodic burssts
    const t = Date.now() / 200
    base = Math.sin(t) * 10 + random * 5
  } else if (currentMode.value === 'MANUAL') {
    // Sharp spikes
    base = (Math.random() > 0.9 ? 30 : 0) + random * 5
  } else if (currentMode.value === 'MACHINE') {
    // Continuous high amplitude chaos
    base = random * 80
  }
  return base
}

const updateWaveform = () => {
  const newVal = generateWavePoint()
  waveData.value.shift()
  waveData.value.push(newVal)

  waveformChart.setOption({
    series: [{
      data: waveData.value,
      itemStyle: {
        color: currentMode.value === 'MACHINE' ? '#F56C6C' : '#409EFF'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: currentMode.value === 'MACHINE' ? 'rgba(245, 108, 108, 0.5)' : 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(255, 255, 255, 0)' }
        ])
      }
    }]
  })
}

// --- Radar Chart Updater ---
const updateRadar = () => {
  // Config per mode: [Frequency, Amplitude, Duration, Periodicity, Energy]
  let data = [0, 0, 0, 0, 0]
  if (currentMode.value === 'IDLE') data = [10, 5, 5, 10, 5]
  if (currentMode.value === 'VEHICLE') data = [60, 30, 20, 90, 30] // High periodicity
  if (currentMode.value === 'MANUAL') data = [30, 50, 40, 30, 40] // Spiky
  if (currentMode.value === 'MACHINE') data = [80, 95, 90, 40, 95] // High Energy/Amp

  radarChart.setOption({
    series: [{
      data: [{ value: data, name: '当前特征' }]
    }]
  })
}

// --- Simulations ---
const simulateVehicle = () => {
  currentMode.value = 'VEHICLE'
  confidence.value = 92.5
  depth.value = 0.0
  distance.value = 12.5
  updateRadar()
}

const simulateManualDigging = () => {
  currentMode.value = 'MANUAL'
  confidence.value = 85.0
  depth.value = 0.5
  distance.value = 2.0
  updateRadar()
}

const simulateMachineDigging = () => {
  currentMode.value = 'MACHINE'
  confidence.value = 99.8
  depth.value = 1.8
  distance.value = 0.5
  updateRadar()
}

const resetSimulation = () => {
  currentMode.value = 'IDLE'
  confidence.value = 0
  depth.value = 0
  distance.value = 0
  updateRadar()
}

// --- Lifecycle ---
onMounted(() => {
  // Init Waveform Chart
  if (waveformChartRef.value) {
    waveformChart = echarts.init(waveformChartRef.value)
    waveformChart.setOption({
      grid: { top: 20, right: 20, bottom: 20, left: 40 },
      xAxis: { type: 'category', data: xAxisData, show: false },
      yAxis: { type: 'value', min: -100, max: 100 },
      series: [{
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 2 }
      }]
    })
  }

  // Init Radar Chart
  if (radarChartRef.value) {
    radarChart = echarts.init(radarChartRef.value)
    radarChart.setOption({
      radar: {
        indicator: [
          { name: '频率', max: 100 },
          { name: '幅度', max: 100 },
          { name: '持续', max: 100 },
          { name: '周期', max: 100 },
          { name: '能量', max: 100 }
        ]
      },
      series: [{
        type: 'radar',
        areaStyle: { opacity: 0.2 },
        itemStyle: { color: '#67C23A' }
      }]
    })
  }

  // Start Loop
  timer = setInterval(updateWaveform, 50)
  
  // Responsive
  window.addEventListener('resize', () => {
    waveformChart && waveformChart.resize()
    radarChart && radarChart.resize()
  })
})

onBeforeUnmount(() => {
  clearInterval(timer)
  waveformChart && waveformChart.dispose()
  radarChart && radarChart.dispose()
})
</script>

<style scoped>
.vibration-analysis-container {
  height: calc(100vh - 80px);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: #f5f7fa;
}

.main-content {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0; /* Enable scrolling if needed */
}

/* Left Column */
.waveform-card {
  flex: 3;
  display: flex;
  flex-direction: column;
}
.waveform-chart {
  height: 100%;
  min-height: 400px;
}

/* Right Column */
.ai-panel {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-card .ai-result-box {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  color: #fff;
  transition: all 0.3s;
}
.bg-idle { background: #909399; }
.bg-safe { background: #67C23A; }
.bg-warning { background: #E6A23C; }
.bg-danger { background: #F56C6C; animation: pulse 1s infinite alternate; }

@keyframes pulse {
  from { transform: scale(1); box-shadow: 0 0 10px rgba(245, 108, 108, 0.5); }
  to { transform: scale(1.02); box-shadow: 0 0 20px rgba(245, 108, 108, 0.8); }
}

.model-name { font-size: 12px; opacity: 0.8; margin-bottom: 5px; }
.result-main { font-size: 28px; font-weight: bold; margin-bottom: 5px; }
.confidence { font-size: 14px; opacity: 0.9; }

.radar-chart {
  height: 250px;
}

.hazard-grid {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: 10px 0;
}
.hazard-item .label { color: #909399; font-size: 12px; margin-bottom: 5px; }
.hazard-item .value { font-size: 20px; font-weight: bold; color: #303133; }
.hazard-item .unit { font-size: 12px; color: #909399; }

/* Bottom Control */
.control-card {
  height: 80px;
  display: flex;
  align-items: center;
}
.control-row {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0 20px;
}
.control-label {
  font-weight: bold;
  margin-right: 20px;
  color: #606266;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
