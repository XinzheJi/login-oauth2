<template>
  <div class="data-aggregation-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据汇聚概览</span>
          <el-button type="primary" @click="dialogVisible = true">指标管理</el-button>
        </div>
      </template>
      
      <!-- 简易的 dashboard 展示 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="pieChart" style="height: 400px;"></div>
        </el-col>
        <el-col :span="12">
          <div class="info-panel">
            <h3>数据汇聚状态</h3>
            <p>当前系统接入设备总数: <span class="highlight">1,245</span></p>
            <p>今日汇聚数据量: <span class="highlight">85.6 GB</span></p>
            <p>数据清洗完成率: <span class="highlight">99.9%</span></p>
            <el-alert
              title="系统运行正常"
              type="success"
              :closable="false"
              show-icon
              style="margin-top: 20px;"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 指标管理弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="指标管理"
      width="600px"
    >
      <div class="indicator-grid">
        <div class="indicator-item" @click="handleNavigate('/data-timeliness')">
          <el-icon class="indicator-icon"><Timer /></el-icon>
          <span class="indicator-name">数据及时性监控</span>
          <span class="indicator-desc">监控数据上报延迟与传输质量</span>
        </div>
        <div class="indicator-item">
          <el-icon class="indicator-icon"><DataLine /></el-icon>
          <span class="indicator-name">数据完整性分析</span>
          <span class="indicator-desc">分析数据包丢失与缺项情况 (待开发)</span>
        </div>
        <div class="indicator-item">
          <el-icon class="indicator-icon"><PieChart /></el-icon>
          <span class="indicator-name">数据准确性校验</span>
          <span class="indicator-desc">校验数据数值范围与逻辑一致性 (待开发)</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Timer, DataLine, PieChart } from '@element-plus/icons-vue'

const router = useRouter()
const dialogVisible = ref(false)
const pieChart = ref(null)

const handleNavigate = (path) => {
  dialogVisible.value = false
  router.push(path)
}

const initChart = () => {
    if (!pieChart.value) return;
    
    const myChart = echarts.init(pieChart.value);
    const option = {
        title: {
            text: '汇聚数据类型分布',
            left: 'center'
        },
        tooltip: {
            trigger: 'item'
        },
        legend: {
            orient: 'vertical',
            left: 'left'
        },
        series: [
            {
                name: '数据类型',
                type: 'pie',
                radius: '50%',
                data: [
                    { value: 1048, name: '遥测数据' },
                    { value: 735, name: '遥信数据' },
                    { value: 580, name: '告警数据' },
                    { value: 484, name: '配置参数' },
                    { value: 300, name: '操作日志' }
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ]
    };
    myChart.setOption(option);
    
    window.addEventListener('resize', () => myChart.resize());
}

onMounted(() => {
    nextTick(() => {
        initChart();
    })
})
</script>

<style scoped>
.data-aggregation-view {
    padding: 20px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.info-panel {
    padding: 20px;
}
.info-panel p {
    font-size: 16px;
    margin-bottom: 15px;
    color: #606266;
}
.info-panel .highlight {
    font-size: 20px;
    font-weight: bold;
    color: #409EFF;
    margin-left: 10px;
}
.indicator-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
}
.indicator-item {
    border: 1px solid #EBEEF5;
    border-radius: 4px;
    padding: 20px;
    cursor: pointer;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    transition: all 0.3s;
}
.indicator-item:hover {
    border-color: #409EFF;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    background-color: #ECF5FF;
}
.indicator-icon {
    font-size: 32px;
    color: #409EFF;
    margin-bottom: 10px;
}
.indicator-name {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 5px;
    color: #303133;
}
.indicator-desc {
    font-size: 12px;
    color: #909399;
}
</style>
