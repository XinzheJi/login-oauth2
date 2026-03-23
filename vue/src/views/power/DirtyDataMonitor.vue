<template>
  <div class="dirty-data-monitor">
    <el-page-header @back="goBack" :title="deviceName || '设备详情'">
      <template #content>
        <div class="header-content">
          <span class="status-tag">
            当前供电状态: 
            <el-tag :type="isMainsActive ? 'success' : 'danger'">
              {{ isMainsActive ? '市电供电' : '备用电源供电' }}
            </el-tag>
          </span>
          <el-button 
            :type="isMainsActive ? 'danger' : 'success'" 
            size="small" 
            @click="toggleMainsStatus"
            class="simulate-btn"
          >
            {{ isMainsActive ? '模拟市电中断' : '恢复市电' }}
          </el-button>
        </div>
      </template>
    </el-page-header>

    <el-row :gutter="20" class="main-content">
      <!-- 左侧：电源故障自愈拓扑 -->
      <el-col :span="12">
        <el-card class="topology-card">
          <template #header>
            <div class="card-header">
              <span>电源故障自愈拓扑</span>
            </div>
          </template>
          <div ref="topologyChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 右侧：脏数据指标管理 -->
      <el-col :span="12">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>脏数据指标管理</span>
            </div>
          </template>
          
          <div class="battery-section">
            <div class="label">备用电源电量 (SoC)</div>
            <el-progress 
              :text-inside="true" 
              :stroke-width="24" 
              :percentage="batterySoc" 
              :status="batteryStatus"
            />
          </div>

          <el-table :data="metricsData" border style="width: 100%; margin-top: 20px">
            <el-table-column prop="name" label="指标名称"></el-table-column>
            <el-table-column prop="value" label="监测值">
              <template #default="scope">
                <span :class="{ 'abnormal-value': scope.row.isAbnormal }">
                  {{ scope.row.value }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80"></el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <span :style="{color: scope.row.isAbnormal ? '#F56C6C' : '#67C23A'}">
                  {{ scope.row.isAbnormal ? '异常' : '正常' }}
                </span>
              </template>
            </el-table-column>
          </el-table>

          <div class="actions-section">
            <el-button type="primary"  @click="queryHistory" style="margin: 0;">查询历史记录</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()

const deviceId = ref(route.query.deviceId || '')
const deviceName = ref(route.query.deviceName || '未知设备')
const isMainsActive = ref(true)
const batterySoc = ref(100)
const topologyChart = ref(null)
let chartInstance = null

// 电池状态计算属性
const batteryStatus = ref('success')

// 指标数据
const metricsData = ref([
  { name: '市电电压', value: '220.0', unit: 'V', isAbnormal: false },
  { name: '市电频率', value: '50.00', unit: 'Hz', isAbnormal: false },
  { name: '电压偏差', value: '0.0', unit: '%', isAbnormal: false },
  { name: '频率偏差', value: '0.00', unit: 'Hz', isAbnormal: false },
  { name: '谐波畸变率', value: '1.2', unit: '%', isAbnormal: false }
])

// 初始化图表
const initChart = () => {
  if (!topologyChart.value) return
  
  chartInstance = echarts.init(topologyChart.value)
  updateChart()
  
  window.addEventListener('resize', () => {
    chartInstance && chartInstance.resize()
  })
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return

  // SVG 路径定义
  const icons = {
    // 市电电网 (电塔/闪电)
    grid: 'path://M12,2L2,22h20L12,2z M12,6l6,14H6L12,6z M10,10h4v2h-4V10z M10,14h4v2h-4V14z', // 简化示例：三角形带横线
    // 配电通信电源 (蓄电池)
    battery: 'path://M4,6V22H20V6M6,8H18V20H6M9,2V4H15V2', // 电池形状
    // 智能切换装置 (双向箭头/开关)
    switch: 'path://M12,2C6.48,2,2,6.48,2,12s4.48,10,10,10s10-4.48,10-10S17.52,2,12,2z M12,20c-4.41,0-8-3.59-8-8s3.59-8,8-8s8,3.59,8,8S16.41,20,12,20z M6.5,11L10,7.5v7L6.5,11z M17.5,13L14,16.5v-7L17.5,13z', // 刷新/循环箭头
    // 通信业务终端 (服务器/电脑)
    terminal: 'path://M20,18c1.1,0,2-0.9,2-2V6c0-1.1-0.9-2-2-2H4C2.9,4,2,4.9,2,6v10c0,1.1,0.9,2,2,2H0v2h24v-2H20z M4,6h16v10H4V6z' // 屏幕形状
  }

  // 使用更具体的SVG Path (这里使用 Material Design 风格的 path 数据)
  const svgs = {
    grid: 'path://M11.99 2L2 22h20L11.99 2zM12 5.84l5.65 11.3H6.35L12 5.84z M10.42 16.53h3.16v2.11h-3.16z M10.42 13.37h3.16v2.11h-3.16z M10.42 10.21h3.16v2.11h-3.16z', // Power Tower
    battery: 'path://M16.67,4H15V2H9v2H7.33C6.6,4,6,4.6,6,5.33v3h12v-3C18,4.6,17.4,4,16.67,4z M6,20.67C6,21.4,6.6,22,7.33,22h9.33c0.74,0,1.34-0.6,1.34-1.33V10H6V20.67z', // Battery Full
    switch: 'path://M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z', // Switch (Actually using X/Cross for switch logic is confusing, let's use a "Swap" or "Toggle" icon) -> replacing with Swap Horizontal
    switch_real: 'path://M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z', // Swap horizontal
    terminal: 'path://M20 18c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z' // Computer
  }

  const nodes = [
    { 
      name: '市电电网', 
      x: 300, 
      y: 50, 
      symbolSize: 50, 
      symbol: svgs.grid,
      itemStyle: { color: isMainsActive.value ? '#409EFF' : '#909399' } // 激活时亮色，否则灰色
    },
    { 
      name: '智能切换装置', 
      x: 300, 
      y: 200, 
      symbolSize: 60, 
      symbol: svgs.switch_real,
      itemStyle: { color: '#E6A23C' } 
    },
    { 
      name: '配电通信电源', 
      x: 100, 
      y: 200, 
      symbolSize: 50, 
      symbol: svgs.battery,
      itemStyle: { color: isMainsActive.value ? '#67C23A' : '#E6A23C' } // 充电时绿，放电时橙
    }, 
    { 
      name: '通信业务终端', 
      x: 300, 
      y: 350, 
      symbolSize: 50, 
      symbol: svgs.terminal,
      itemStyle: { color: '#409EFF' } // 始终工作
    }
  ]

  const links = []

  if (isMainsActive.value) {
    // 正常状态
    links.push({
      source: '市电电网',
      target: '智能切换装置',
      lineStyle: { color: '#67C23A', width: 4, type: 'solid' }, // 绿色实线
      label: { show: true, formatter: '供电中' }
    })
    links.push({
      source: '配电通信电源',
      target: '智能切换装置',
      lineStyle: { color: '#909399', width: 2, type: 'dashed' }, // 灰色虚线
      label: { show: true, formatter: '备用' }
    })
  } else {
    // 故障状态
    links.push({
      source: '市电电网',
      target: '智能切换装置',
      lineStyle: { color: '#F56C6C', width: 2, type: 'dotted' }, // 红色断开
      label: { show: true, formatter: '断开' }
    })
    links.push({
      source: '配电通信电源',
      target: '智能切换装置',
      lineStyle: { color: '#E6A23C', width: 4, type: 'solid' }, // 橙色实线
      label: { show: true, formatter: '放电中' }
    })
  }

  // 始终连接负载
  links.push({
    source: '智能切换装置',
    target: '通信业务终端',
    lineStyle: { color: isMainsActive.value ? '#67C23A' : '#E6A23C', width: 4 },
    label: { show: true, formatter: '供电' }
  })

  const option = {
    title: {
      text: isMainsActive.value ? '供电拓扑：正常' : '供电拓扑：市电中断',
      left: 'center',
      textStyle: {
        fontWeight: 'normal'
      }
    },
    tooltip: {},
    series: [
      {
        type: 'graph',
        layout: 'none',
        symbolSize: 50,
        roam: true,
        label: { show: true, position: 'bottom' },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        data: nodes,
        links: links,
        lineStyle: { opacity: 0.9, width: 2, curveness: 0 }
      }
    ]
  }

  chartInstance.setOption(option)
}

// 切换市电状态
const toggleMainsStatus = () => {
  isMainsActive.value = !isMainsActive.value
  
  if (!isMainsActive.value) {
    // 模拟中断
    ElMessage.warning('模拟市电中断！切换至备用电源。')
    // 脏数据模拟
    metricsData.value[0].value = '0.0' // 电压
    metricsData.value[0].isAbnormal = true
    metricsData.value[1].value = '0.00' // 频率
    metricsData.value[1].isAbnormal = true
    metricsData.value[2].value = '-100.0' // 电压偏差
    metricsData.value[2].isAbnormal = true
    
    // 电池电量开始下降模拟
    batteryStatus.value = 'warning'
    let timer = setInterval(() => {
      if (isMainsActive.value || batterySoc.value <= 0) {
        clearInterval(timer)
        return
      }
      batterySoc.value = Math.max(0, batterySoc.value - 1)
      if (batterySoc.value < 20) batteryStatus.value = 'exception'
    }, 1000)
    
  } else {
    // 恢复
    ElMessage.success('市电已恢复正常。')
    // 数据恢复
    metricsData.value[0].value = '220.0'
    metricsData.value[0].isAbnormal = false
    metricsData.value[1].value = '50.00'
    metricsData.value[1].isAbnormal = false
    metricsData.value[2].value = '0.0'
    metricsData.value[2].isAbnormal = false
    
    batteryStatus.value = 'success'
    batterySoc.value = 100
  }
  
  updateChart()
}

const goBack = () => {
  router.back()
}

const queryHistory = () => {
  ElMessage.info('正在查询历史记录...')
}

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<style scoped>
.dirty-data-monitor {
  padding: 20px;
}
.header-content {
  display: flex;
  align-items: center;
  gap: 15px;
}
.main-content {
  margin-top: 20px;
}
.chart-container {
  height: 500px;
}
.measure-value {
  font-weight: bold;
}
.abnormal-value {
  color: #F56C6C;
  font-weight: bold;
}
.battery-section {
  margin-bottom: 30px;
}
.battery-section .label {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}
.actions-section {
  margin-top: 20px;
  text-align: center;
}
</style>