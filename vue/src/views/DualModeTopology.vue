<template>
  <div class="dual-mode-topology">
    <div class="main-canvas">
       <div ref="chartDom" class="chart-container"></div>
    </div>
    
    <div class="control-panel">
      <div class="panel-header">自愈演练控制台</div>
      
      <div class="control-group">
        <span class="label">有线信号 (SNR): {{ simulation.snr }} dB</span>
        <el-slider v-model="simulation.snr" :min="0" :max="50" @input="updateTopology" :disabled="simulation.hplcBroken" />
      </div>
      
      <div class="control-group">
        <span class="label">无线信号 (RSSI): {{ simulation.rssi }} dBm</span>
        <el-slider v-model="simulation.rssi" :min="-100" :max="-40" @input="updateTopology" :disabled="simulation.rfBroken" />
      </div>
      
      <div class="button-group">
        <el-button type="warning" @click="simulateHplcBreak" class="full-width" :disabled="simulation.hplcBroken || simulation.allBroken">模拟有线断路 (HPLC中断)</el-button>
        <el-button type="danger" @click="simulateAllBreak" class="full-width" :disabled="simulation.allBroken">模拟全阻断 (双通道故障)</el-button>
        <el-button type="info" @click="resetSimulation" class="full-width">重置演示环境</el-button>
      </div>

      <div class="status-monitor">
        <div class="status-item">
          <span class="status-label">当前主用通道:</span>
          <span class="status-value" :class="activeChannelClass">{{ activeChannelText }}</span>
        </div>
         <div class="status-item">
          <span class="status-label">节点状态:</span>
          <span class="status-value" :class="nodeStatusClass">{{ nodeStatusText }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { ElMessage, ElNotification } from 'element-plus'

// Import SVG Icons
import ccoIcon from '@/assets/topology/cco.svg'
import meterBoxIcon from '@/assets/topology/meter-box.svg'

export default {
  name: 'DualModeTopology',
  data() {
    return {
      chartInstance: null,
      simulation: {
        snr: 45,       // Default excellent
        rssi: -60,     // Default good
        hplcBroken: false,
        rfBroken: false,
        allBroken: false
      }
    }
  },
  computed: {
    activeChannelText() {
        if (this.simulation.allBroken) return '无 (全阻断)';
        if (this.simulation.hplcBroken) return 'RF 无线 (备用)';
        return 'HPLC 有线 (主用)';
    },
    activeChannelClass() {
        if (this.simulation.allBroken) return 'text-danger';
        if (this.simulation.hplcBroken) return 'text-warning';
        return 'text-success';
    },
    nodeStatusText() {
        return this.simulation.allBroken ? '离线' : '在线';
    },
     nodeStatusClass() {
        return this.simulation.allBroken ? 'text-gray' : 'text-success';
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
    handleResize() {
      if (this.chartInstance) this.chartInstance.resize();
    },
    initChart() {
      this.chartInstance = echarts.init(this.$refs.chartDom);
      this.updateTopology();
    },
    getColorBySignal(value, type) {
        // Type: 'snr' or 'rssi'
        // SNR: >30 Green, 15-30 Yellow, <15 Red
        // RSSI: >-70 Green, -70 to -90 Yellow, <-90 Red (Simplified logic)
        
        let isGood, isMedium;
        
        if (type === 'snr') {
            isGood = value > 30;
            isMedium = value >= 15 && value <= 30;
        } else {
            isGood = value > -70; // e.g. -60
            isMedium = value <= -70 && value >= -90;
        }

        if (isGood) return '#67C23A';
        if (isMedium) return '#E6A23C';
        return '#F56C6C';
    },
    updateTopology() {
      if (!this.chartInstance) return;

      const { snr, rssi, hplcBroken, allBroken } = this.simulation;
      
      // Determine Line Colors based on sliders or broken state
      let hplcColor = allBroken || hplcBroken ? '#F56C6C' : this.getColorBySignal(snr, 'snr');
      let rfColor = allBroken ? '#F56C6C' : this.getColorBySignal(rssi, 'rssi'); // RF usually stays valid unless all broken or specifically simulated (not btn for just RF)
      
      // Node Color
      // let nodeColor = allBroken ? '#909399' : '#409EFF'; // SVG handles color internally or we can use filters if needed, but for now fixed SVG colors is fine or we rely on opacity/grayscale if supported. 
      // ECharts doesn't easily tint SVGs. We'll leave them as is, maybe just opacity if broken.
      
      // Line Types
      let hplcType = allBroken || hplcBroken ? 'dotted' : 'solid';
      let rfType = 'solid'; // RF always physically solid representation for this demo, usually dashed if backup, but let's make it solid lower curve
      
      // Effect Animation (Traffic Flow)
      // If Normal: Flow on Top (HPLC)
      // If HPLC Broken: Flow on Bottom (RF)
      // If All Broken: No Flow
      let hplcEffect = { show: !allBroken && !hplcBroken, period: 4, symbolSize: 4, color: '#fff' };
      let rfEffect = { show: !allBroken && hplcBroken, period: 3, symbolSize: 4, color: '#fff' }; // Faster on RF? Or just different.
      
      const nodes = [
          {
              name: 'CCO\n(集中器)',
              x: 100,
              y: 300,
              symbol: 'image://' + ccoIcon,
              symbolSize: 70,
              itemStyle: { opacity: allBroken ? 0.5 : 1 },
              label: { fontWeight: 'bold' }
          },
          {
              name: 'STA_01\n(表箱#1)',
              x: 400,
              y: 150,
              symbol: 'image://' + meterBoxIcon,
              symbolSize: 60,
              itemStyle: { opacity: allBroken ? 0.5 : 1 }
          },
          {
              name: 'STA_02\n(表箱#2)',
              x: 400,
              y: 450,
              symbol: 'image://' + meterBoxIcon,
              symbolSize: 60,
              itemStyle: { opacity: allBroken ? 0.5 : 1 }
          }
      ];

      const links = [];
      
      // Function to create dual links
      const createDualLink = (targetName, curvenessBase) => {
          // Link 1: HPLC (Upper)
          links.push({
              source: 'CCO\n(集中器)',
              target: targetName,
              lineStyle: {
                  curveness: curvenessBase, // Upward curve
                  color: hplcColor,
                  width: 3,
                  type: hplcType
              },
              label: {
                  show: true,
                  formatter: allBroken || hplcBroken ? '❌ HPLC' : `HPLC (${snr}dB)`,
                  position: 'middle'
              },
              effect: hplcEffect
          });
          
          // Link 2: RF (Lower)
          links.push({
              source: 'CCO\n(集中器)',
              target: targetName,
              lineStyle: {
                  curveness: -curvenessBase, // Downward curve (negative relative to straight line? Actually echarts curveness is positive for one side)
                  // Wait, if straight line is horizontal, curveness > 0 is one side, < 0 is other.
                  // Let's try 0.2 and -0.2
                  color: rfColor,
                  width: 2,
                  type: 'dashed' // RF visually distinguished
              },
               label: {
                  show: true,
                  formatter: `RF (${rssi}dBm)`,
                  position: 'middle'
              },
              effect: rfEffect
          });
      };

      // Connect to STA_01 (High) - maybe curve logic is tricky with coordinates.
      // Let's rely on standard curveness.
      // CCO(100,300) -> STA_01(400, 150)
      // Normal curve is relative to the segment.
      
      // For STA_01
      links.push({
           source: 'CCO\n(集中器)',
           target: 'STA_01\n(表箱#1)',
           lineStyle: { curveness: 0.2, color: hplcColor, width: 3, type: hplcType },
           label: { show: true, formatter: hplcBroken || allBroken ? '❌ HPLC' : 'HPLC', position: 'middle' },
           effect: hplcEffect
      });
      links.push({
           source: 'CCO\n(集中器)',
           target: 'STA_01\n(表箱#1)',
           lineStyle: { curveness: -0.2, color: rfColor, width: 2, type: 'solid' }, // Using solid for lower line to be visible
           label: { show: true, formatter: 'RF', position: 'middle' },
           effect: rfEffect
      });

      // For STA_02
       links.push({
           source: 'CCO\n(集中器)',
           target: 'STA_02\n(表箱#2)',
           lineStyle: { curveness: 0.2, color: hplcColor, width: 3, type: hplcType },
           label: { show: true, formatter: hplcBroken || allBroken ? '❌ HPLC' : 'HPLC', position: 'middle' },
           effect: hplcEffect
      });
      links.push({
           source: 'CCO\n(集中器)',
           target: 'STA_02\n(表箱#2)',
           lineStyle: { curveness: -0.2, color: rfColor, width: 2, type: 'solid' },
           label: { show: true, formatter: 'RF', position: 'middle' },
           effect: rfEffect
      });

      const option = {
        title: {
          text: '有线(HPLC)/无线(RF) 双模拓扑监控',
          left: 'center',
          top: 20
        },
        tooltip: {},
        animationDurationUpdate: 1000,
        series: [
          {
            type: 'graph',
            layout: 'none',
            symbolSize: 50,
            label: {
              show: true,
              position: 'bottom'
            },
            data: nodes,
            links: links,
            lineStyle: {
              opacity: 0.9,
              width: 2,
            }
          }
        ]
      };
      
      this.chartInstance.setOption(option);
    },
    simulateHplcBreak() {
      this.simulation.hplcBroken = true;
      this.simulation.snr = 0; // Simulate loss
      this.updateTopology();
      
      ElMessage.warning('检测到 HPLC 通道中断，正在切换至 RF 无线通道...');
      setTimeout(() => {
          ElMessage.success('自愈成功：业务已迁移至 RF 通道');
      }, 1000);
    },
    simulateAllBreak() {
        this.simulation.allBroken = true;
        this.simulation.hplcBroken = true;
        this.simulation.rfBroken = true;
        this.simulation.snr = 0;
        this.simulation.rssi = -120; // Dead
        this.updateTopology();
        
        ElNotification({
            title: '严重告警',
            message: '节点 [MAC:AA:BB:CC:DD] 通信全阻断，自愈失败，请派单处理',
            type: 'error',
            duration: 0
        });
    },
    resetSimulation() {
        this.simulation.hplcBroken = false;
        this.simulation.rfBroken = false;
        this.simulation.allBroken = false;
        this.simulation.snr = 45;
        this.simulation.rssi = -60;
        this.updateTopology();
        ElMessage.success('演示环境已重置');
    }
  }
}
</script>

<style scoped>
.dual-mode-topology {
  display: flex;
  height: calc(100vh - 100px);
  background-color: #f5f7fa;
}
.main-canvas {
  flex: 1;
  background: #fff;
  margin: 10px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  overflow: hidden;
}
.chart-container {
    width: 100%;
    height: 100%;
}
.control-panel {
  width: 320px;
  background: #fff;
  margin: 10px 10px 10px 0;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
}
.panel-header {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    color: #303133;
}
.control-group {
    margin-bottom: 25px;
}
.label {
    display: block;
    margin-bottom: 10px;
    font-size: 14px;
    color: #606266;
    font-weight: 500;
}
.button-group {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-top: 10px;
}
.full-width {
    width: 100%;
    margin-left: 0 !important;
}
.status-monitor {
    margin-top: auto;
    background: #fdf6ec;
    border: 1px solid #faecd8;
    padding: 15px;
    border-radius: 4px;
}
.status-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 14px;
}
.status-item:last-child {
    margin-bottom: 0;
}
.status-label {
    color: #909399;
}
.status-value {
    font-weight: bold;
}
.text-success { color: #67C23A; }
.text-warning { color: #E6A23C; }
.text-danger { color: #F56C6C; }
.text-gray { color: #909399; }
</style>
