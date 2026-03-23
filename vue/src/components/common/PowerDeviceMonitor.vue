<template>
  <el-card class="device-monitor-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <h3>设备实时监控</h3>
      </div>
    </template>
    
    <!-- 1. 电源基本实时显示模块 -->
    <div class="realtime-status-section">
      <div class="status-item" v-for="item in realtimeStatus" :key="item.label">
        <div class="status-label">{{ item.label }}</div>
        <div 
          class="status-value" 
          :style="{ color: item.color }"
        >
          {{ item.value }}
        </div>
        <div class="status-unit">{{ item.unit }}</div>
      </div>
    </div>
    
    <el-divider />
    
    <!-- 2. 循环次数仪表盘和电池容量 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>循环次数</span>
            </div>
          </template>
          <div  class="chart-container"></div>
          <div class="battery-capacity-info">
            <div class="capacity-item">
              <span class="capacity-label">电池容量1:</span>
              <span class="capacity-value"></span>
            </div>
            <div class="capacity-item">
              <span class="capacity-label">电池容量2:</span>
              <span class="capacity-value"></span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 3. 充电电流曲线图 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>充电电流曲线</span>
            </div>
          </template>
          <div class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 4. 24小时负载情况折线图 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>24小时负载情况</span>
            </div>
          </template>
          <div  class="chart-container"></div>
        </el-card>
      </el-col>
      
      <!-- 5. 充放电曲线 -->
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>充放电曲线</span>
            </div>
          </template>
          <div  class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 6. 设备运行状态模块 -->
    <el-row :gutter="20" class="status-icons-row">
      <el-col :span="6" v-for="status in deviceStatus" :key="status.type">
        <div class="status-icon-item">
          <div class="status-icon" :style="{ backgroundColor: status.color }">
            <el-icon :size="24">
              <component :is="status.icon" />
            </el-icon>
          </div>
          <div class="status-icon-label">{{ status.label }}</div>
          <div class="status-icon-value">{{ status.value }}</div>
        </div>
      </el-col>
    </el-row>
  </el-card>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Warning, InfoFilled, SuccessFilled, CircleCheck } from '@element-plus/icons-vue'

export default {
  name: 'PowerDeviceMonitor',
  props: {
    // 实时状态数据
    realtimeData: {
      type: Object,
      default: () => ({
        acVoltage: '',
        dcVoltage: '',
        loadCurrent: '',
        batteryLevel: ''
      })
    },
    // 循环次数
    cycleCount: {
      type: Number,
      default: 0
    },
    // 电池容量
    batteryCapacity: {
      type: Object,
      default: () => ({
        capacity1: '',
        capacity2: ''
      })
    },
    // 充电电流数据
    chargeCurrentData: {
      type: Array,
      default: () => []
    },
    // 24小时负载数据
    loadData: {
      type: Array,
      default: () => []
    },
    // 充放电数据
    chargeDischargeData: {
      type: Object,
      default: () => ({
        charge: [],
        discharge: []
      })
    },
    // 设备运行状态
    deviceStatusData: {
      type: Object,
      default: () => ({
        warningCount: 0,
        faultCount: 0,
        alarmRecoverCount: 0,
        normalCount: 0
      })
    }
  },
  setup(props) {
    const cycleChartRef = ref(null)
    const chargeCurrentChartRef = ref(null)
    const loadChartRef = ref(null)
    const chargeDischargeChartRef = ref(null)
    
    let cycleChart = null
    let chargeCurrentChart = null
    let loadChart = null
    let chargeDischargeChart = null
    
    // 实时状态数据
    const realtimeStatus = reactive([
      {
        label: '市电电压',
        value: '',
        unit: '',
        color: '#67C23A'
      },
      {
        label: '输出电压',
        value: '',
        unit: '',
        color: '#409EFF'
      },
      {
        label: '负载电流',
        value: '',
        unit: '',
        color: '#E6A23C'
      },
      {
        label: '电池电量',
        value: '',
        unit: '',
        color: '#67C23A'
      },
      {
        label: '预计可用时长',
        value:  '',
        unit: '',
        color: '#909399'
      }
    ])
    
    // 根据电池电量获取颜色（低于20%显示红色）
    function getBatteryColor(level) {
      const numLevel = parseInt(level) || 100
      if (numLevel <= 20) return '#F56C6C'
      if (numLevel <= 40) return '#E6A23C'
      return '#67C23A'
    }
    
    // 根据电池电量计算预计可用时长
    function calculateRuntime(level) {
      const numLevel = parseInt(level) || 100
      // 假设100%电量可用240分钟（4小时）
      const minutes = Math.floor(numLevel * 2.4)
      if (minutes >= 60) {
        const hours = Math.floor(minutes / 60)
        const mins = minutes % 60
        return `${hours}小时${mins}分钟`
      }
      return `${minutes}分钟`
    }
    
    // 设备运行状态
    const deviceStatus = reactive([
      {
        type: 'warning',
        label: '警告数量',
        value: '',
        color: '#FF9800',
        icon: Warning
      },
      {
        type: 'fault',
        label: '故障数量',
        value: '',
        color: '#F44336',
        icon: Warning
      },
      {
        type: 'recover',
        label: '告警恢复',
        value: '',
        color: '#2196F3',
        icon: InfoFilled
      },
      {
        type: 'normal',
        label: '正常运行',
        value: '',
        color: '#4CAF50',
        icon: CircleCheck
      }
    ])
    
    // 生成空数据
    const generateEmptyData = () => {
      return {
        chargeCurrent: [],
        load: [],
        chargeDischarge: {
          charge: [],
          discharge: []
        }
      }
    }
    
    // 初始化循环次数图表
    const initCycleChart = () => {
      if (!cycleChartRef.value) return
      
      if (!cycleChart) {
        cycleChart = echarts.init(cycleChartRef.value)
      }
      
      const currentCycle = props.cycleCount || 0
      
      const option = {
        tooltip: {
          formatter: '{b}: {c}次'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: ['循环次数'],
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '次数',
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        series: [
          {
            name: '循环次数',
            type: 'bar',
            data: [currentCycle],
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      
      cycleChart.setOption(option)
    }
    
    // 初始化充电电流曲线图
    const initChargeCurrentChart = () => {
      if (!chargeCurrentChartRef.value) return
      
      if (!chargeCurrentChart) {
        chargeCurrentChart = echarts.init(chargeCurrentChartRef.value)
      }
      
      const data = props.chargeCurrentData.length > 0 
        ? props.chargeCurrentData 
        : []
      
      const months = data.map(item => item.month || item.time)
      const values = data.map(item => item.value || item.current)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}mA'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: months,
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '电流(mA)',
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        series: [
          {
            name: '充电电流',
            type: 'line',
            smooth: true,
            data: values,
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {
                    offset: 0,
                    color: 'rgba(64, 158, 255, 0.7)'
                  },
                  {
                    offset: 1,
                    color: 'rgba(64, 158, 255, 0.1)'
                  }
                ]
              }
            }
          }
        ]
      }
      
      chargeCurrentChart.setOption(option)
    }
    
    // 初始化24小时负载情况折线图
    const initLoadChart = () => {
      if (!loadChartRef.value) return
      
      if (!loadChart) {
        loadChart = echarts.init(loadChartRef.value)
      }
      
      const data = props.loadData.length > 0 
        ? props.loadData 
        : []
      
      const hours = data.map(item => item.hour || item.time)
      const values = data.map(item => item.value || item.load)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}%'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: hours,
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '负载(%)',
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        series: [
          {
            name: '负载情况',
            type: 'line',
            smooth: true,
            data: values,
            itemStyle: {
              color: '#E6A23C'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {
                    offset: 0,
                    color: 'rgba(230, 162, 60, 0.7)'
                  },
                  {
                    offset: 1,
                    color: 'rgba(230, 162, 60, 0.1)'
                  }
                ]
              }
            }
          }
        ]
      }
      
      loadChart.setOption(option)
    }
    
    // 初始化充放电曲线
    const initChargeDischargeChart = () => {
      if (!chargeDischargeChartRef.value) return
      
      if (!chargeDischargeChart) {
        chargeDischargeChart = echarts.init(chargeDischargeChartRef.value)
      }
      
      const chargeData = props.chargeDischargeData.charge?.length > 0 
        ? props.chargeDischargeData.charge 
        : []
      const dischargeData = props.chargeDischargeData.discharge?.length > 0 
        ? props.chargeDischargeData.discharge 
        : []
      
      const hours = chargeData.map(item => item.hour || item.time)
      const chargeValues = chargeData.map(item => item.value || item.charge)
      const dischargeValues = dischargeData.map(item => item.value || item.discharge)
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              result += param.seriesName + ': ' + param.value + 'mAh<br/>'
            })
            return result
          }
        },
        legend: {
          data: ['充电量', '放电量'],
          top: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: hours,
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        yAxis: {
          type: 'value',
          name: '电量(mAh)',
          axisLine: {
            lineStyle: {
              color: '#666'
            }
          }
        },
        series: [
          {
            name: '充电量',
            type: 'line',
            smooth: true,
            data: chargeValues,
            itemStyle: {
              color: '#67C23A'
            }
          },
          {
            name: '放电量',
            type: 'line',
            smooth: true,
            data: dischargeValues,
            itemStyle: {
              color: '#F56C6C'
            }
          }
        ]
      }
      
      chargeDischargeChart.setOption(option)
    }
    
    // 窗口大小变化时重新渲染图表
    const handleResize = () => {
      if (cycleChart) cycleChart.resize()
      if (chargeCurrentChart) chargeCurrentChart.resize()
      if (loadChart) loadChart.resize()
      if (chargeDischargeChart) chargeDischargeChart.resize()
    }
    
    // 渲染所有图表
    const renderCharts = () => {
      nextTick(() => {
        initCycleChart()
        initChargeCurrentChart()
        initLoadChart()
        initChargeDischargeChart()
      })
    }
    
    // 监听 props 变化
    watch(() => props.realtimeData, (newVal) => {
      if (newVal) {
        realtimeStatus[0].value = newVal.acVoltage || realtimeStatus[0].value
        realtimeStatus[1].value = newVal.dcVoltage || realtimeStatus[1].value
        realtimeStatus[2].value = newVal.loadCurrent || realtimeStatus[2].value
        realtimeStatus[3].value = newVal.batteryLevel || realtimeStatus[3].value
      }
    }, { deep: true })
    
    watch(() => props.deviceStatusData, (newVal) => {
      if (newVal) {
        deviceStatus[0].value = newVal.warningCount || ''
        deviceStatus[1].value = newVal.faultCount || ''
        deviceStatus[2].value = newVal.alarmRecoverCount || ''
        deviceStatus[3].value = newVal.normalCount || ''
      }
    }, { deep: true })
    
    watch(() => props.cycleCount, () => {
      initCycleChart()
    })
    
    watch(() => props.chargeCurrentData, () => {
      initChargeCurrentChart()
    }, { deep: true })
    
    watch(() => props.loadData, () => {
      initLoadChart()
    }, { deep: true })
    
    watch(() => props.chargeDischargeData, () => {
      initChargeDischargeChart()
    }, { deep: true })
    
    onMounted(() => {
      renderCharts()
      window.addEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      if (cycleChart) {
        cycleChart.dispose()
        cycleChart = null
      }
      if (chargeCurrentChart) {
        chargeCurrentChart.dispose()
        chargeCurrentChart = null
      }
      if (loadChart) {
        loadChart.dispose()
        loadChart = null
      }
      if (chargeDischargeChart) {
        chargeDischargeChart.dispose()
        chargeDischargeChart = null
      }
      window.removeEventListener('resize', handleResize)
    })
    
    return {
      cycleChartRef,
      chargeCurrentChartRef,
      loadChartRef,
      chargeDischargeChartRef,
      realtimeStatus,
      deviceStatus,
      batteryCapacity: props.batteryCapacity
    }
  }
}
</script>

<style scoped>
.device-monitor-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: normal;
}

/* 实时状态展示 */
.realtime-status-section {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  flex-wrap: wrap;
  gap: 20px;
}

.status-item {
  flex: 1;
  min-width: 150px;
  text-align: center;
  padding: 15px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.status-item:hover {
  transform: translateY(-5px);
}

.status-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.status-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.status-unit {
  font-size: 12px;
  color: #909399;
}

/* 图表区域 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 100%;
}

.chart-header {
  height: 40px;
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: normal;
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 电池容量信息 */
.battery-capacity-info {
  display: flex;
  justify-content: space-around;
  padding: 15px 0;
  margin-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.capacity-item {
  text-align: center;
}

.capacity-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.capacity-value {
  display: block;
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

/* 设备运行状态图标 */
.status-icons-row {
  margin-top: 20px;
}

.status-icon-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: transform 0.3s;
}

.status-icon-item:hover {
  transform: translateY(-5px);
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.status-icon-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.status-icon-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
</style>