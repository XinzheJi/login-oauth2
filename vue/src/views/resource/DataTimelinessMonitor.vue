<template>
  <div class="data-timeliness-monitor">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="item in statsCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon-wrapper" :class="item.type">
              <el-icon class="stat-icon"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-compare">
                同比上周 
                <span :class="item.trend > 0 ? 'up' : 'down'">
                  {{ item.trend > 0 ? '+' : '' }}{{ item.trend }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中部趋势图表 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>通信链路数据传输时延趋势</span>
        </div>
      </template>
      <div ref="trendChart" class="chart-container"></div>
    </el-card>

    <!-- 底部查询与明细表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>数据传输明细查询</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="设备名称">
          <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="queryParams.dataType" placeholder="请选择" clearable>
            <el-option label="遥测数据" value="telemetry" />
            <el-option label="遥信数据" value="telesignal" />
            <el-option label="告警数据" value="alarm" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="正常" value="normal" />
            <el-option label="延迟" value="delay" />
            <el-option label="丢包" value="loss" />
            <el-option label="严重超时" value="timeout" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="流水号" width="180" />
        <el-table-column prop="deviceName" label="设备名称" width="180" />
        <el-table-column prop="dataType" label="数据类型" width="120" />
        <el-table-column prop="collectTime" label="采集时间" width="180" />
        <el-table-column prop="reportTime" label="上报时间" width="180" />
        <el-table-column prop="latency" label="传输时延(ms)" width="120">
          <template #default="scope">
            <span :class="{ 'red-text': scope.row.latency > 1000 }">{{ scope.row.latency }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="及时性评估">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.statusText }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 (模拟) -->
      <div class="pagination-container">
        <el-pagination layout="total, prev, pager, next" :total="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Timer, Odometer, Warning, DataLine } from '@element-plus/icons-vue'

// 统计卡片数据
const statsCards = ref([
  { title: '实时数据到达率', value: '99.8%', trend: 0.2, icon: 'Timer', type: 'success' },
  { title: '平均传输时延', value: '45ms', trend: -1.5, icon: 'Odometer', type: 'primary' },
  { title: '今日超时告警数', value: '12', trend: 5.0, icon: 'Warning', type: 'warning' },
  { title: '数据吞吐量', value: '8.5 MB/s', trend: 12.3, icon: 'DataLine', type: 'info' }
])

// 趋势图表
const trendChart = ref(null)

const initChart = () => {
  if (!trendChart.value) return
  const myChart = echarts.init(trendChart.value)

  // 模拟数据 (12:00 波峰)
  const times = []
  const avgLatency = []
  const maxLatency = []
  
  for (let i = 0; i < 24; i++) {
    const time = `${String(i).padStart(2, '0')}:00`
    times.push(time)
    
    // 基础时延
    let baseAvg = 40 + Math.random() * 10
    let baseMax = 100 + Math.random() * 50
    
    // 12:00 模拟波峰
    if (i === 12) {
      baseAvg += 150
      baseMax += 800
    }
    
    avgLatency.push(baseAvg.toFixed(0))
    maxLatency.push(baseMax.toFixed(0))
  }

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均时延', '最大时延'] },
    xAxis: { type: 'category', data: times },
    yAxis: { type: 'value', name: '时延 (ms)' },
    series: [
      {
        name: '平均时延',
        type: 'line',
        data: avgLatency,
        itemStyle: { color: '#409EFF' },
        lineStyle: { width: 2 }
      },
      {
        name: '最大时延',
        type: 'line',
        data: maxLatency,
        itemStyle: { color: '#F56C6C' },
        lineStyle: { type: 'dashed', width: 2 }
      }
    ]
  }
  
  myChart.setOption(option)
  
  window.addEventListener('resize', () => {
    myChart.resize()
  })
}

// 查询与表格
const queryParams = ref({
  deviceName: '',
  dataType: '',
  status: ''
})

const sourceTableData = [
  { id: '202312180001', deviceName: '1#配电变压器', dataType: '遥测数据', collectTime: '2023-12-18 12:00:00', reportTime: '2023-12-18 12:00:00', latency: 45, status: 'normal', statusText: '及时' },
  { id: '202312180002', deviceName: '2#智能断路器', dataType: '告警数据', collectTime: '2023-12-18 12:00:05', reportTime: '2023-12-18 12:00:06', latency: 1200, status: 'timeout', statusText: '严重超时' },
  { id: '202312180003', deviceName: '3#环境监测仪', dataType: '遥信数据', collectTime: '2023-12-18 12:00:10', reportTime: '2023-12-18 12:00:10', latency: 38, status: 'normal', statusText: '及时' },
  { id: '202312180004', deviceName: '1#配电变压器', dataType: '遥测数据', collectTime: '2023-12-18 12:01:00', reportTime: '2023-12-18 12:01:00', latency: 50, status: 'normal', statusText: '及时' },
  { id: '202312180005', deviceName: '4#视频监控', dataType: '流媒体', collectTime: '2023-12-18 12:05:00', reportTime: '2023-12-18 12:05:02', latency: 2000, status: 'loss', statusText: '丢包风险' }
]

const tableData = ref([...sourceTableData])

const getStatusTagType = (status) => {
  const map = {
    normal: 'success',
    delay: 'warning',
    loss: 'warning',
    timeout: 'danger'
  }
  return map[status] || 'info'
}

const handleSearch = () => {
    const { deviceName, dataType, status } = queryParams.value
    
    // Map dropdown values to data values if necessary, or ensure they match.
    // In template:
    // dataType options: 'telemetry', 'telesignal', 'alarm'
    // status options: 'normal', 'delay', 'loss', 'timeout'
    // Data values:
    // dataType: '遥测数据', '遥信数据', '告警数据', '流媒体'
    // status: 'normal', 'timeout', 'loss' (matches)
    
    // We need a mapping for dataType since the select values are english/codes but data is Chinese
    const dataTypeMap = {
        'telemetry': '遥测数据',
        'telesignal': '遥信数据',
        'alarm': '告警数据'
    }

    const filterDataType = dataType ? dataTypeMap[dataType] : null

    tableData.value = sourceTableData.filter(item => {
        const matchName = !deviceName || item.deviceName.includes(deviceName)
        const matchType = !filterDataType || item.dataType === filterDataType
        const matchStatus = !status || item.status === status
        
        return matchName && matchType && matchStatus
    })
}

const resetSearch = () => {
    queryParams.value = {
        deviceName: '',
        dataType: '',
        status: ''
    }
    tableData.value = [...sourceTableData]
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.data-timeliness-monitor {
  padding: 20px;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  height: 120px;
  display: flex;
  align-items: center;
}
.stat-content {
  display: flex;
  align-items: center;
  width: 100%;
}
.stat-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}
.stat-icon {
  font-size: 30px;
  color: #fff;
}
.stat-icon-wrapper.success { background-color: #67C23A; }
.stat-icon-wrapper.primary { background-color: #409EFF; }
.stat-icon-wrapper.warning { background-color: #E6A23C; }
.stat-icon-wrapper.info { background-color: #909399; }

.stat-info {
  flex: 1;
}
.stat-title {
  font-size: 14px;
  color: #909399;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin: 5px 0;
}
.stat-compare {
  font-size: 12px;
  color: #909399;
}
.stat-compare .up { color: #F56C6C; }
.stat-compare .down { color: #67C23A; }

.chart-card {
  margin-bottom: 20px;
}
.chart-container {
  height: 400px;
}
.search-form {
  margin-bottom: 10px;
}
.red-text {
  color: #F56C6C;
  font-weight: bold;
}
.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
