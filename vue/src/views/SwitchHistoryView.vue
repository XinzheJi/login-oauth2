<template>
  <div class="switch-history-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
           <div class="search-filters">
             <span class="filter-label">时间范围:</span>
             <el-date-picker
                v-model="dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="default"
              />
              
             <span class="filter-label" style="margin-left: 20px;">切换类型:</span>
             <el-select v-model="switchType" placeholder="请选择" size="default">
               <el-option label="全部" value="all" />
               <el-option label="光纤 -> 4G" value="fiber_to_4g" />
               <el-option label="4G -> 光纤" value="4g_to_fiber" />
             </el-select>
             
             <el-button type="primary" icon="Search" style="margin-left: 20px;" @click="handleSearch">查询</el-button>
           </div>
        </div>
      </template>

      <!-- Quality Chart -->
      <div class="chart-section">
        <div class="chart-title">过去 24 小时通信质量与切换事件</div>
        <div ref="chartDom" style="width: 100%; height: 350px;"></div>
      </div>

      <!-- History Table -->
      <div class="table-section">
        <div class="section-title">切换记录列表</div>
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="deviceName" label="装置名称" width="180" />
          <el-table-column prop="eventType" label="事件类型" width="150">
             <template #default="scope">
               <el-tag :type="scope.row.eventType === '光纤 -> 4G' ? 'danger' : 'success'">
                 {{ scope.row.eventType }}
               </el-tag>
             </template>
          </el-table-column>
          <el-table-column prop="reason" label="触发原因" />
          <el-table-column prop="duration" label="持续时长" width="120" />
          <el-table-column label="详情" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click="viewSnapshot(scope.row)">查看快照</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'SwitchHistoryView',
  data() {
    return {
      dateRange: [],
      switchType: 'all',
      chartInstance: null,
      allTableData: [
        {
          time: '2023-12-16 10:05:00',
          deviceName: '路由装置_#01',
          eventType: '光纤 -> 4G',
          reason: '光功率过低 (-30dBm)',
          duration: '至今'
        },
        {
          time: '2023-12-16 09:00:00',
          deviceName: '路由装置_#02',
          eventType: '4G -> 光纤',
          reason: '光纤信号恢复',
          duration: '2小时'
        },
         {
          time: '2023-12-15 14:20:11',
          deviceName: '路由装置_#01',
          eventType: '4G -> 光纤',
          reason: '人工切换',
          duration: '5小时'
        },
        {
          time: '2023-12-15 09:15:33',
          deviceName: '路由装置_#01',
          eventType: '光纤 -> 4G',
          reason: '光模块故障',
          duration: '5小时'
        }
      ],
      tableData: []
    }
  },
  created() {
      this.tableData = [...this.allTableData];
  },
  mounted() {
    this.initChart();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize);
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    handleSearch() {
        this.tableData = this.allTableData.filter(item => {
            let matchType = true;
            if (this.switchType && this.switchType !== 'all') {
                const map = {
                    'fiber_to_4g': '光纤 -> 4G',
                    '4g_to_fiber': '4G -> 光纤'
                };
                matchType = item.eventType === map[this.switchType];
            }

            let matchDate = true;
            if (this.dateRange && this.dateRange.length === 2) {
                const itemTime = new Date(item.time).getTime();
                const startTime = this.dateRange[0].getTime();
                const endTime = this.dateRange[1].getTime();
                matchDate = itemTime >= startTime && itemTime <= endTime;
            }

            return matchType && matchDate;
        });
        ElMessage.success('查询已更新');
    },
    viewSnapshot(row) {
        ElMessage.info(`查看 ${row.time} 的故障快照`);
    },
    handleResize() {
      if (this.chartInstance) this.chartInstance.resize();
    },
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chartDom);
      
      // Mock 24h Data
      const hours = [];
      const latency = [];
      const packetLoss = [];
      
      for (let i = 0; i < 24; i++) {
          hours.push(`${i}:00`);
          // Random latency between 10ms and 50ms, with spikes
          let lat = Math.floor(Math.random() * 40) + 10;
          let loss = Math.random() * 0.5; 
          
          if (i === 10) { // Simulate problem at 10:00
              lat = 200;
              loss = 15; 
          }
          
          latency.push(lat);
          packetLoss.push(loss);
      }

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['网络时延 (ms)', '丢包率 (%)']
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
          data: hours
        },
        yAxis: [
          {
            type: 'value',
            name: '时延 (ms)',
            position: 'left',
            axisLine: { show: true, lineStyle: { color: '#5470C6' } }
          },
          {
            type: 'value',
            name: '丢包率 (%)',
            position: 'right',
            min: 0,
            max: 100,
            axisLine: { show: true, lineStyle: { color: '#91CC75' } },
            splitLine: { show: false }
          }
        ],
        series: [
          {
            name: '网络时延 (ms)',
            type: 'line',
            data: latency,
            color: '#5470C6',
            markPoint: {
                data: [
                    { name: '切换点', value: '光纤故障', xAxis: 10, yAxis: 200, itemStyle: { color: '#F56C6C' } }
                ]
            },
            markLine: {
                data: [
                    { xAxis: 10, label: { formatter: '10:00 切换至4G' } }
                ]
            }
          },
          {
            name: '丢包率 (%)',
            type: 'line',
            yAxisIndex: 1,
            data: packetLoss,
            color: '#91CC75'
          }
        ]
      };

      this.chartInstance.setOption(option);
    }
  }
}
</script>

<style scoped>
.switch-history-container {
  padding: 20px;
}
.search-filters {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
}
.filter-label {
    font-size: 14px;
    color: #606266;
    margin-right: 10px;
    font-weight: bold;
}
.chart-section {
    margin: 20px 0;
    padding: 20px;
    background: #fdfdfd;
    border: 1px solid #ebeef5;
    border-radius: 4px;
}
.chart-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 20px;
    border-left: 4px solid #409EFF;
    padding-left: 10px;
}
.section-title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 15px;
    border-left: 4px solid #67C23A;
    padding-left: 10px;
}
.table-section {
    margin-top: 30px;
}
</style>
