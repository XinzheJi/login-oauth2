<template>
  <div class="optical-health-container">
    <el-card class="box-card">
      <!-- Top: Search Filter -->
      <div class="filter-header">
        <span class="filter-label">时间范围:</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
        />
        
        <span class="filter-label" style="margin-left: 20px;">光缆编号:</span>
        <el-input v-model="cableId" placeholder="请输入光缆编号" style="width: 200px" />
        
        <span class="filter-label" style="margin-left: 20px;">指标类型:</span>
        <el-select v-model="indicatorType" placeholder="请选择">
          <el-option label="光衰值 (dB/km)" value="attenuation" />
          <el-option label="偏振模色散 (PMD)" value="pmd" />
        </el-select>
        
        <el-button type="primary" style="margin-left: 20px;" @click="refreshData">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>
      
      <!-- Middle: Charts -->
      <div class="charts-row">
        <!-- Left: Aging Trend -->
        <div class="chart-wrapper">
          <div class="chart-title">光缆老化趋势分析 (Aging Trend)</div>
          <div ref="lineChartRef" class="chart-box"></div>
        </div>
        
        <!-- Right: Health Distribution -->
        <div class="chart-wrapper">
          <div class="chart-title">光缆健康状态分布</div>
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </div>
      
      <!-- Bottom: Data Table -->
      <div class="table-section">
        <div class="chart-title">详细测试记录</div>
        <el-table :data="tableData" border style="width: 100%" height="250">
          <el-table-column prop="date" label="测试时间" width="180" />
          <el-table-column prop="cableId" label="光缆编号" width="180" />
          <el-table-column prop="value" label="光衰值 (dB/km)" />
          <el-table-column prop="threshold" label="标准阈值" />
          <el-table-column label="评估结论">
             <template #default="scope">
               <el-tag :type="getConclusionType(scope.row.conclusion)">{{ scope.row.conclusion }}</el-tag>
             </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'

const dateRange = ref([])
const cableId = ref('CABLE-2023-A01')
const indicatorType = ref('attenuation')

const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null

// Mock Data
const sourceTableData = [
  { date: '2023-08-15', cableId: 'CABLE-2023-A01', value: 0.21, threshold: 0.25, conclusion: '健康' },
  { date: '2023-09-15', cableId: 'CABLE-2023-A01', value: 0.22, threshold: 0.25, conclusion: '健康' },
  { date: '2023-10-15', cableId: 'CABLE-2023-A01', value: 0.23, threshold: 0.25, conclusion: '亚健康' },
  { date: '2023-11-15', cableId: 'CABLE-2023-A01', value: 0.26, threshold: 0.25, conclusion: '严重老化' },
  { date: '2023-12-15', cableId: 'CABLE-2023-A01', value: 0.28, threshold: 0.25, conclusion: '严重老化' }
]

const tableData = ref([...sourceTableData])

const initCharts = () => {
  // Line Chart
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['8月', '9月', '10月', '11月', '12月']
    },
    yAxis: {
      type: 'value',
      name: '光衰值 (dB/km)',
      min: 0.15,
      max: 0.35
    },
    series: [
      {
        name: '衰耗值',
        type: 'line',
        data: [0.21, 0.22, 0.23, 0.26, 0.28],
        itemStyle: { color: '#409EFF' },
        markLine: {
          data: [{ yAxis: 0.25, name: '告警阈值' }],
          lineStyle: { color: '#F56C6C', type: 'dashed' },
          label: { formatter: '{b}: {c}' }
        },
      }
    ]
  })

  // Pie Chart
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
     tooltip: { trigger: 'item' },
     legend: { bottom: '5%', left: 'center' },
     series: [
       {
         name: '健康分布',
         type: 'pie',
         radius: ['40%', '70%'],
         avoidLabelOverlap: false,
         itemStyle: {
           borderRadius: 10,
           borderColor: '#fff',
           borderWidth: 2
         },
         label: { show: false },
         data: [
           { value: 75, name: '健康光缆', itemStyle: { color: '#67C23A' } },
           { value: 20, name: '亚健康光缆', itemStyle: { color: '#E6A23C' } },
           { value: 5, name: '严重老化', itemStyle: { color: '#F56C6C' } }
         ]
       }
     ]
  })
}

const getConclusionType = (val) => {
  if (val === '健康') return 'success'
  if (val === '亚健康') return 'warning'
  return 'danger'
}

const refreshData = () => {
  tableData.value = sourceTableData.filter(item => {
    let matchId = true
    let matchDate = true
    
    if (cableId.value) {
      matchId = item.cableId.includes(cableId.value)
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      const itemDate = new Date(item.date)
      matchDate = itemDate >= dateRange.value[0] && itemDate <= dateRange.value[1]
    }
    
    return matchId && matchDate
  })
}

const resetFilter = () => {
  dateRange.value = []
  cableId.value = 'CABLE-2023-A01'
  indicatorType.value = 'attenuation'
  tableData.value = [...sourceTableData]
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (lineChart) lineChart.dispose()
  if (pieChart) pieChart.dispose()
})

const handleResize = () => {
  lineChart && lineChart.resize()
  pieChart && pieChart.resize()
}
</script>

<style scoped>
.optical-health-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 80px);
}
.filter-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.filter-label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
  white-space: nowrap;
}

.charts-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-wrapper {
  flex: 1;
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.chart-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}
.chart-box {
  height: 300px;
}

.table-section {
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
</style>
