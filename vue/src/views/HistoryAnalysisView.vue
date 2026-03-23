<template>
  <div class="history-analysis-container">
    <el-card class="box-card">
      <template #header>
        <div class="filter-container">
          <span class="filter-label">时间维度:</span>
          <el-radio-group v-model="timeDimension" size="default" @change="updateCharts">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="year">全年</el-radio-button>
          </el-radio-group>
          
          <span class="filter-label" style="margin-left: 20px;">选择设备:</span>
          <el-select v-model="selectedDevice" placeholder="请选择" size="default" @change="updateCharts">
            <el-option label="集中器_#01" value="cco1" />
            <el-option label="集中器_#02" value="cco2" />
          </el-select>
        </div>
      </template>

      <div class="charts-grid">
        <!-- utilization Chart -->
        <div class="chart-box full-width">
           <div class="chart-title">双通道利用率趋势</div>
           <div ref="utilizationChart" style="height: 300px;"></div>
        </div>

        <!-- Switch Stats -->
        <div class="chart-box half-width">
           <div class="chart-title">自愈切换次数统计</div>
           <div ref="switchChart" style="height: 300px;"></div>
        </div>

        <!-- Success Rate -->
        <div class="chart-box half-width">
           <div class="chart-title">通信成功率分布</div>
           <div ref="pieChart" style="height: 300px;"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'HistoryAnalysisView',
  data() {
    return {
      timeDimension: 'month',
      selectedDevice: 'cco1',
      charts: {}
    }
  },
  mounted() {
    this.initCharts();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    Object.values(this.charts).forEach(chart => chart.dispose());
  },
  methods: {
    handleResize() {
      Object.values(this.charts).forEach(chart => chart.resize());
    },
    initCharts() {
      this.charts.utilization = echarts.init(this.$refs.utilizationChart);
      this.charts.switch = echarts.init(this.$refs.switchChart);
      this.charts.pie = echarts.init(this.$refs.pieChart);
      this.updateCharts();
    },
    updateCharts() {
        this.renderUtilizationChart();
        this.renderSwitchChart();
        this.renderPieChart();
    },
    renderUtilizationChart() {
        let xAxisData = [];
        let seriesDataWired = [];
        let seriesDataWireless = [];
        
        // Adjust base values based on device to simulate different data
        const baseOffset = this.selectedDevice === 'cco2' ? -10 : 0;

        if (this.timeDimension === 'week') {
            xAxisData = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            seriesDataWired = [85, 88, 82, 90, 85, 88, 86].map(v => v + baseOffset);
            seriesDataWireless = [15, 12, 18, 10, 15, 12, 14].map(v => v - (baseOffset / 2));
        } else if (this.timeDimension === 'month') {
            // 生成30天数据
            for (let i = 1; i <= 30; i++) {
                xAxisData.push(`${i}日`);
                // 模拟数据波动
                seriesDataWired.push(Math.floor(80 + Math.random() * 15) + baseOffset);
                seriesDataWireless.push(Math.floor(5 + Math.random() * 15) - (baseOffset / 2));
            }
        } else {
             // year
             xAxisData = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
             seriesDataWired = [82, 85, 88, 86, 89, 90, 92, 91, 88, 85, 87, 89].map(v => v + baseOffset);
             seriesDataWireless = [18, 15, 12, 14, 11, 10, 8, 9, 12, 15, 13, 11].map(v => v - (baseOffset / 2));
        }

        const option = {
            tooltip: { trigger: 'axis' },
            legend: { data: ['HPLC 有线', 'RF 无线'] },
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: xAxisData
            },
            yAxis: {
                type: 'value',
                name: '利用率 (%)',
                max: 100
            },
            series: [
                {
                    name: 'HPLC 有线',
                    type: 'line',
                    data: seriesDataWired,
                    itemStyle: { color: '#409EFF' },
                    areaStyle: { opacity: 0.1 }
                },
                {
                    name: 'RF 无线',
                    type: 'line',
                    data: seriesDataWireless,
                    itemStyle: { color: '#E6A23C' },
                    areaStyle: { opacity: 0.1 }
                }
            ]
        };
        this.charts.utilization.setOption(option);
    },
    renderSwitchChart() {
        const isDevice2 = this.selectedDevice === 'cco2';
        const data = isDevice2 
            ? [
                { value: 5, itemStyle: { color: '#F56C6C' } },
                { value: 4, itemStyle: { color: '#67C23A' } }
              ]
            : [
                { value: 12, itemStyle: { color: '#F56C6C' } },
                { value: 10, itemStyle: { color: '#67C23A' } }
              ];

        const option = {
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            legend: {},
            grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
            xAxis: {
                type: 'category',
                data: ['有线断路 -> 切无线', '恢复有线']
            },
            yAxis: { type: 'value', name: '次数' },
            series: [
                {
                    name: '切换次数',
                    type: 'bar',
                    barWidth: '40%',
                    data: data
                }
            ]
        };
        this.charts.switch.setOption(option);
    },
    renderPieChart() {
        const option = {
            tooltip: { trigger: 'item' },
            legend: { top: '5%', left: 'center' },
            series: [
                {
                    name: '通信结果',
                    type: 'pie',
                    radius: ['40%', '70%'],
                    avoidLabelOverlap: false,
                    itemStyle: {
                        borderRadius: 10,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: { show: false, position: 'center' },
                    emphasis: {
                        label: { show: true, fontSize: 20, fontWeight: 'bold' }
                    },
                    labelLine: { show: false },
                    data: [
                        { value: 985, name: '成功', itemStyle: { color: '#67C23A' } },
                        { value: 15, name: '失败', itemStyle: { color: '#F56C6C' } }
                    ]
                }
            ]
        };
        this.charts.pie.setOption(option);
    }
  }
}
</script>

<style scoped>
.history-analysis-container {
  padding: 20px;
}
.filter-container {
    display: flex;
    align-items: center;
}
.filter-label {
    margin-right: 10px;
    font-weight: bold;
    color: #606266;
}
.charts-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-top: 20px;
}
.chart-box {
    background: #fff;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 15px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.full-width {
    width: 100%;
}
.half-width {
    width: calc(50% - 10px);
}
.chart-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    padding-left: 10px;
    border-left: 4px solid #409EFF;
}
</style>
