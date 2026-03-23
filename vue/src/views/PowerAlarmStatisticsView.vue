<template>
  <div class="alarm-statistics-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>告警统计分析</h3>
          <el-button @click="$router.push('/power-alarms')" plain>返回告警列表</el-button>
        </div>
      </template>
      
      <!-- 时间范围选择 -->
      <div class="date-range-select">
        <el-form inline>
          <el-form-item label="统计时间范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchStatistics">统计</el-button>
            <el-button @click="resetDateRange">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 数据卡片 -->
      <div v-loading="loading">
        <el-row :gutter="20" class="statistics-cards">
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="card-item">
                <div class="card-title">总告警数</div>
                <div class="card-value">
                  <span class="number">{{ statistics.totalAlarms || 0 }}</span>
                  <span class="unit">条</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="card-item">
                <div class="card-title">未处理告警</div>
                <div class="card-value">
                  <span class="number">{{ statistics.unprocessedAlarms || 0 }}</span>
                  <span class="unit">条</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover">
              <div class="card-item">
                <div class="card-title">已解决告警</div>
                <div class="card-value">
                  <span class="number">{{ statistics.resolvedAlarms || 0 }}</span>
                  <span class="unit">条</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 按级别统计和按类型统计 -->
        <el-row :gutter="20" class="chart-row">
          <el-col :span="12">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span>告警级别分布</span>
                </div>
              </template>
              <div id="levelPieChart" class="chart"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span>告警类型分布</span>
                </div>
              </template>
              <div id="typePieChart" class="chart"></div>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 告警趋势和区域分布 -->
        <el-row :gutter="20" class="chart-row">
          <el-col :span="16">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span>告警趋势</span>
                </div>
              </template>
              <div id="trendChart" class="chart"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <div class="chart-header">
                  <span>告警区域分布</span>
                </div>
              </template>
              <div id="locationPieChart" class="chart"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAlarmStatistics } from '../api/powerAlarm'
import * as echarts from 'echarts'

export default {
  name: 'PowerAlarmStatisticsView',
  setup() {
    const loading = ref(false)
    const dateRange = ref([])
    const statistics = reactive({
      totalAlarms: 0,
      unprocessedAlarms: 0,
      resolvedAlarms: 0,
      alarmLevelCounts: {},
      alarmTypeCounts: {},
      alarmLocationCounts: {},
      alarmTrend: []
    })
    
    // 保存图表实例
    let levelChart = null
    let typeChart = null
    let trendChart = null
    let locationChart = null
    
    // 获取统计数据
    const fetchStatistics = async () => {
      loading.value = true
      try {
        let startTime = null
        let endTime = null
        
        if (dateRange.value && dateRange.value.length === 2) {
          startTime = dateRange.value[0]
          endTime = dateRange.value[1]
        }
        
        const res = await getAlarmStatistics(startTime, endTime)
        console.log('获取到的告警统计数据:', res)
        
        if (res) {
          // 直接使用API返回的数据
          const statsData = res
          
          // 规范化数据结构
          const normalizedData = {
            totalAlarms: statsData.totalAlarms || 0,
            unprocessedAlarms: statsData.unprocessedAlarms || 0,
            resolvedAlarms: statsData.resolvedAlarms || 0,
            alarmLevelCounts: statsData.alarmLevelCounts || {},
            alarmTypeCounts: statsData.alarmTypeCounts || {},
            alarmLocationCounts: statsData.alarmLocationCounts || {},
            alarmTrend: Array.isArray(statsData.alarmTrend) ? statsData.alarmTrend : []
          }
          
          // 修正alarmTrend数据中可能存在的字段名问题
          if (normalizedData.alarmTrend.length > 0) {
            normalizedData.alarmTrend = normalizedData.alarmTrend.map(item => ({
              date: item.date || '',
              count: item.count || item.alarmCount || 0
            }))
          }
          
          // 更新统计数据
          Object.assign(statistics, normalizedData)
          renderCharts()
        } else {
          ElMessage.error('获取告警统计数据失败')
        }
      } catch (error) {
        console.error('获取告警统计数据失败:', error)
        ElMessage.error('获取告警统计数据失败')
      } finally {
        loading.value = false
      }
    }
    
    // 重置日期范围
    const resetDateRange = () => {
      dateRange.value = []
      fetchStatistics()
    }
    
    // 渲染图表
    const renderCharts = () => {
      renderLevelPieChart()
      renderTypePieChart()
      renderTrendChart()
      renderLocationPieChart()
    }
    
    // 渲染告警级别饼图
    const renderLevelPieChart = () => {
      const chartDom = document.getElementById('levelPieChart')
      if (!chartDom) return
      
      if (!levelChart) {
        levelChart = echarts.init(chartDom)
      }
      
      const levelData = []
      const levelColors = []
      
      console.log('告警级别数据:', statistics.alarmLevelCounts)
      
      // 处理告警级别数据
      if (statistics.alarmLevelCounts) {
        Object.entries(statistics.alarmLevelCounts).forEach(([level, count]) => {
          if (level && count !== undefined) {
            // 根据告警级别获取对应颜色
            let color = '#909399' // 默认颜色
            if (level === '信息') color = '#2196F3'
            else if (level === '警告') color = '#FF9800'
            else if (level === '严重') color = '#F44336'
            else if (level === '紧急') color = '#9C27B0'
            
            levelData.push({
              name: level,
              value: count
            })
            levelColors.push(color)
          }
        })
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: levelData.map(item => item.name)
        },
        series: [
          {
            name: '告警级别',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            color: levelColors,
            data: levelData
          }
        ]
      }
      
      levelChart.setOption(option)
    }
    
    // 渲染告警类型饼图
    const renderTypePieChart = () => {
      const chartDom = document.getElementById('typePieChart')
      if (!chartDom) return
      
      if (!typeChart) {
        typeChart = echarts.init(chartDom)
      }
      
      const typeData = []
      
      console.log('告警类型数据:', statistics.alarmTypeCounts)
      
      // 处理告警类型数据
      if (statistics.alarmTypeCounts) {
        Object.entries(statistics.alarmTypeCounts).forEach(([type, count]) => {
          if (type && count !== undefined) {
            typeData.push({
              name: type || '未知类型',
              value: count
            })
          }
        })
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: typeData.map(item => item.name)
        },
        series: [
          {
            name: '告警类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: typeData
          }
        ]
      }
      
      typeChart.setOption(option)
    }
    
    // 渲染告警趋势图
    const renderTrendChart = () => {
      const chartDom = document.getElementById('trendChart')
      if (!chartDom) return
      
      if (!trendChart) {
        trendChart = echarts.init(chartDom)
      }
      
      const dates = []
      const counts = []
      
      console.log('告警趋势数据:', statistics.alarmTrend)
      
      // 处理趋势数据
      if (Array.isArray(statistics.alarmTrend)) {
        statistics.alarmTrend.forEach(item => {
          if (item && (item.date || item.date === '') && (item.count !== undefined)) {
            dates.push(item.date)
            counts.push(item.count)
          }
        })
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'category',
            data: dates,
            axisTick: {
              alignWithLabel: true
            }
          }
        ],
        yAxis: [
          {
            type: 'value',
            name: '告警数量'
          }
        ],
        series: [
          {
            name: '告警数量',
            type: 'line',
            smooth: true,
            data: counts,
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
      
      trendChart.setOption(option)
    }
    
    // 渲染告警区域饼图
    const renderLocationPieChart = () => {
      const chartDom = document.getElementById('locationPieChart')
      if (!chartDom) return
      
      if (!locationChart) {
        locationChart = echarts.init(chartDom)
      }
      
      const locationData = []
      
      console.log('告警区域数据:', statistics.alarmLocationCounts)
      
      // 处理区域数据
      if (statistics.alarmLocationCounts) {
        Object.entries(statistics.alarmLocationCounts).forEach(([location, count]) => {
          if (count !== undefined) {
            locationData.push({
              name: location || '未知',
              value: count
            })
          }
        })
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          data: locationData.map(item => item.name)
        },
        series: [
          {
            name: '告警区域',
            type: 'pie',
            radius: '75%',
            center: ['40%', '50%'],
            data: locationData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      
      locationChart.setOption(option)
    }
    
    // 窗口大小变化时重新渲染图表
    const handleResize = () => {
      if (levelChart) levelChart.resize()
      if (typeChart) typeChart.resize()
      if (trendChart) trendChart.resize()
      if (locationChart) locationChart.resize()
    }
    
    onMounted(() => {
      fetchStatistics()
      window.addEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      // 销毁图表实例
      if (levelChart) {
        levelChart.dispose()
        levelChart = null
      }
      if (typeChart) {
        typeChart.dispose()
        typeChart = null
      }
      if (trendChart) {
        trendChart.dispose()
        trendChart = null
      }
      if (locationChart) {
        locationChart.dispose()
        locationChart = null
      }
      
      window.removeEventListener('resize', handleResize)
    })
    
    return {
      loading,
      dateRange,
      statistics,
      fetchStatistics,
      resetDateRange
    }
  }
}
</script>

<style scoped>
.alarm-statistics-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.date-range-select {
  margin-bottom: 20px;
}
.statistics-cards {
  margin-bottom: 20px;
}
.card-item {
  text-align: center;
}
.card-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 10px;
}
.card-value .number {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-right: 5px;
}
.card-value .unit {
  font-size: 14px;
  color: #606266;
}
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
}
.chart {
  height: 300px;
  width: 100%;
}
</style> 