<template>
  <div class="topology-page">
    <div class="sidebar-list">
      <div class="sidebar-header">
        <span>设备列表</span>
      </div>
      <el-tree
        :data="deviceTree"
        :props="defaultProps"
        @node-click="handleNodeClick"
        default-expand-all
        highlight-current
        node-key="id"
        :current-node-key="1"
      />
    </div>

    <div class="main-canvas">
      <div class="control-panel">
        <el-button-group>
          <el-button type="danger" @click="simulateFiberBreak" :disabled="isFiberBroken">模拟光纤中断</el-button>
          <el-button type="success" @click="simulateFiberRecovery" :disabled="!isFiberBroken">模拟光纤恢复</el-button>
        </el-button-group>
        <el-button type="warning" style="margin-left: 10px;" @click="simulate4GUnavailable" plain>模拟4G不可用</el-button>
        
        <el-divider direction="vertical" />
        
        <!-- 电源故障模拟按钮组 -->
        <el-button-group style="margin-left: 10px;">
          <el-button type="danger" @click="simulatePowerOutage" :disabled="isPowerOutage">模拟市电中断</el-button>
          <el-button type="success" @click="simulatePowerRecovery" :disabled="!isPowerOutage">模拟市电恢复</el-button>
        </el-button-group>
      </div>
      
      <div class="chart-wrapper" ref="chartDom"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { ElNotification, ElMessage } from 'element-plus'

// Import SVG Icons
import terminalIcon from '@/assets/topology/terminal.svg'
import routerIcon from '@/assets/topology/router.svg'
import towerIcon from '@/assets/topology/tower.svg'
import switchIcon from '@/assets/topology/switch.svg'

export default {
  name: 'TopologyMonitor',
  data() {
    return {
      chartInstance: null,
      isFiberBroken: false,
      isPowerOutage: false,
      batteryLevel: 100,
      deviceTree: [
        {
          id: 1,
          label: '10kV配电房A',
          children: [
            { id: 11, label: '路由装置_#01 (主)' },
            { id: 12, label: '摄像头_A' },
            { id: 13, label: '温湿度传感器' }
          ]
        },
        {
          id: 2,
          label: '户外柜B区',
          children: [
             { id: 21, label: '路由装置_#02' }
          ]
        },
        {
          id: 3,
          label: '电源管理单元',
          children: [
            { id: 31, label: 'UPS电源_#01' },
            { id: 32, label: '蓄电池组_A' }
          ]
        }
      ],
      defaultProps: {
        children: 'children',
        label: 'label'
      }
    }
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
    handleNodeClick(data) {
      console.log(data);
    },
    handleResize() {
      if (this.chartInstance) this.chartInstance.resize();
    },
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chartDom);
      this.updateChartOption();
    },
    getGraphicElements() {
       return {
           terminal: 'image://' + terminalIcon,
           router: 'image://' + routerIcon,
           tower: 'image://' + towerIcon,
           switch: 'image://' + switchIcon
       }
    },
    updateChartOption() {
      const symbols = this.getGraphicElements();
      
      // Node Data
      const data = [
        {
          name: '业务终端\n(摄像头)',
          x: 100,
          y: 400,
          symbol: symbols.terminal,
          symbolSize: 60,
          itemStyle: { color: 'transparent' } // Let SVG handle color or use transparent
        },
        {
          name: '智能路由装置\n(网关)',
          x: 300,
          y: 400,
          symbol: symbols.router,
          symbolSize: 70,
          // itemStyle: { color: this.isFiberBroken ? '#F56C6C' : '#67C23A' }, // SVG might override, but let's try
          label: {
              fontWeight: 'bold'
          }
        },
        {
          name: '无线通信仓\n(4G基站)',
          x: 500,
          y: 200,
          symbol: symbols.tower,
          symbolSize: 60,
          itemStyle: { color: 'transparent' }
        },
        {
          name: '工业交换机\n(核心)',
          x: 700,
          y: 400,
          symbol: symbols.switch,
          symbolSize: 70,
          itemStyle: { color: 'transparent' }
        },
        {
          name: '电源管理单元\n(UPS)',
          x: 300,
          y: 200,
          symbol: 'rect',
          symbolSize: [80, 50],
          itemStyle: { 
            color: this.isPowerOutage ? '#F56C6C' : '#67C23A',
            borderColor: this.isPowerOutage ? '#C45656' : '#529b2e',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'inside',
            formatter: this.isPowerOutage ? '⚡ 备用供电' : '✓ 市电正常',
            color: '#fff',
            fontSize: 11,
            fontWeight: 'bold'
          }
        }
      ];

      // Links Data
      let links = [];

      // Link 1: Terminal -> Router (Always connected)
      links.push({
        source: '业务终端\n(摄像头)',
        target: '智能路由装置\n(网关)',
        lineStyle: { width: 2, color: '#333' }
      });

      // Link 2: Path A (Fiber): Router -> Switch
      if (this.isFiberBroken) {
          // Broken state
          links.push({
            source: '智能路由装置\n(网关)',
            target: '工业交换机\n(核心)',
            lineStyle: { 
                width: 3, 
                color: '#F56C6C', 
                type: 'solid' 
            },
            label: {
                show: true,
                formatter: '❌ 光纤中断',
                color: 'red',
                fontWeight: 'bold'
            }
          });
      } else {
          // Normal state
          links.push({
            source: '智能路由装置\n(网关)',
            target: '工业交换机\n(核心)',
            lineStyle: { 
                width: 4, 
                color: '#67C23A', 
                curveness: 0 
            },
             effect: {
                show: true,
                period: 4,
                trailLength: 0.7,
                color: '#fff',
                symbolSize: 3
            }
          });
      }

      // Link 3: Path B (4G): Router -> Tower -> Switch
      // Router -> Tower
      links.push({
        source: '智能路由装置\n(网关)',
        target: '无线通信仓\n(4G基站)',
        lineStyle: { 
            width: 2, 
            color: this.isFiberBroken ? '#E6A23C' : '#ccc', 
            type: this.isFiberBroken ? 'solid' : 'dashed',
            curveness: 0.2
        },
        effect: {
            show: this.isFiberBroken, // Only show effect when active
            period: 2, // Faster than fiber
            trailLength: 0.5,
            color: '#fff',
            symbolSize: 4
        }
      });

      // Tower -> Switch
      links.push({
        source: '无线通信仓\n(4G基站)',
        target: '工业交换机\n(核心)',
        lineStyle: { 
            width: 2, 
            color: this.isFiberBroken ? '#E6A23C' : '#ccc', 
            type: this.isFiberBroken ? 'solid' : 'dashed',
            curveness: 0.2
        },
        effect: {
            show: this.isFiberBroken,
            period: 2,
            trailLength: 0.5,
            color: '#fff',
            symbolSize: 4
        }
      });

      const option = {
        title: {
          text: '网络拓扑自愈监控',
          left: 'center',
          top: 20
        },
        tooltip: {},
        animationDurationUpdate: 1000,
        animationEasingUpdate: 'quinticInOut',
        series: [
          {
            type: 'graph',
            layout: 'none',
            symbolSize: 50,
            roam: true,
            label: {
              show: true,
              position: 'bottom',
              fontSize: 12
            },
            edgeSymbol: ['circle', 'arrow'],
            edgeSymbolSize: [4, 10],
            edgeLabel: {
              fontSize: 12
            },
            data: data,
            links: links,
            lineStyle: {
              opacity: 0.9,
              width: 2,
              curveness: 0
            }
          }
        ]
      };

      this.chartInstance.setOption(option);
    },
    simulateFiberBreak() {
      if (this.isFiberBroken) return;
      
      this.isFiberBroken = true;
      this.updateChartOption();
      
      // Notification
      ElNotification({
        title: '告警',
        message: '装置_#01 光纤通道中断，已自动切换至4G通道！',
        type: 'error',
        duration: 5000
      });
    },
    simulateFiberRecovery() {
        if (!this.isFiberBroken) return;
        
        this.isFiberBroken = false;
        this.updateChartOption();

        ElNotification({
            title: '恢复',
            message: '光纤通道已恢复正常，切回主用链路。',
            type: 'success',
            duration: 4000
        });
    },
    simulate4GUnavailable() {
        ElMessage.warning('模拟 4G 信号丢失（此演示由于光纤正常，主要通道不受影响）');
    },
    // 模拟市电中断
    simulatePowerOutage() {
      if (this.isPowerOutage) return;
      
      this.isPowerOutage = true;
      this.batteryLevel = 85; // 切换到备用电源后电量
      this.updateChartOption();
      
      // 告警通知
      ElNotification({
        title: '⚠️ 紧急告警',
        message: '市电供电中断！已自动切换至备用电源（UPS），当前电量：85%',
        type: 'error',
        duration: 8000
      });
      
      // 延迟显示电量监控提示
      setTimeout(() => {
        ElNotification({
          title: '📊 电源状态',
          message: `备用电源供电中，预计可用时长：${Math.floor(this.batteryLevel * 2.4)} 分钟`,
          type: 'warning',
          duration: 6000
        });
      }, 2000);
    },
    // 模拟市电恢复
    simulatePowerRecovery() {
      if (!this.isPowerOutage) return;
      
      this.isPowerOutage = false;
      this.batteryLevel = 100;
      this.updateChartOption();
      
      ElNotification({
        title: '✓ 恢复正常',
        message: '市电已恢复，系统正在切换回市电供电并为电池充电。',
        type: 'success',
        duration: 5000
      });
    }
  }
}
</script>

<style scoped>
.topology-page {
  display: flex;
  height: calc(100vh - 100px); /* Adjust based on header height */
  background-color: #f5f7fa;
}

.sidebar-list {
  width: 250px;
  background: #fff;
  border-right: 1px solid #dcdfe6;
  padding: 10px;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 10px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.main-canvas {
  flex: 1;
  position: relative;
  background: #fff;
  margin: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.control-panel {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  background: #fafafa;
}

.chart-wrapper {
  flex: 1;
  width: 100%;
  height: 100%;
}
</style>
