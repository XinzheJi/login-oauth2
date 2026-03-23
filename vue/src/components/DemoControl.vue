<template>
  <div class="demo-control-container">
    <!-- Floating Button -->
    <el-button
      type="primary"
      circle
      size="large"
      class="demo-float-btn"
      @click="openPanel"
    >
      <el-icon><Tools /></el-icon>
    </el-button>

    <!-- Control Panel Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="演示控制面板"
      direction="rtl"
      size="320px"
    >
      <div class="control-panel-content">
        <p class="panel-desc">请选择要模拟的场景：</p>
        
        <div class="scenario-item">
          <h4>场景一：光纤/4G 切换</h4>
          <el-button 
            type="danger" 
            style="width: 100%" 
            :loading="scenario1Loading"
            @click="runScenario1"
          >
            模拟主用光纤中断
          </el-button>
        </div>

        <div class="scenario-item">
          <h4>场景二：载波通道故障</h4>
          <el-button 
            type="warning" 
            style="width: 100%" 
            :loading="scenario2Loading"
            @click="runScenario2"
          >
            模拟载波主通道故障
          </el-button>
        </div>

        <div class="scenario-item">
          <h4>场景三：AI 智能诊断</h4>
          <el-button 
            type="primary" 
            style="width: 100%" 
            :loading="scenario3Loading"
            @click="runScenario3"
          >
            模拟外部施工振动
          </el-button>
        </div>

        <div class="scenario-item">
          <h4>场景四：备用电源电量低</h4>
          <el-button 
            type="danger" 
            style="width: 100%" 
            :loading="scenario4Loading"
            @click="runScenario4"
          >
            模拟备用电源电量低于阈值
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- Scenario 2 Chart Dialog -->
    <el-dialog
      v-model="chartDialogVisible"
      title="设备 8821 - 通道信号质量 (近30天)"
      width="700px"
      @opened="initChart"
    >
      <div ref="chartContainer" style="width: 100%; height: 400px;"></div>
    </el-dialog>
  </div>
</template>

<script>
/**
 * DemoControl.vue
 * 
 * Instructions for integration in DashboardView.vue:
 * 1. Import this component: `import DemoControl from '@/components/DemoControl.vue'`
 * 2. Register it in components: `components: { DemoControl, ... }`
 * 3. Add `<DemoControl />` to the template, preferably at the root level div.
 */

import { ElMessage, ElNotification } from 'element-plus'
import * as echarts from 'echarts'
import { Tools } from '@element-plus/icons-vue'

export default {
  name: 'DemoControl',
  components: {
    Tools
  },
  data() {
    return {
      drawerVisible: false,
      scenario1Loading: false,
      scenario2Loading: false,
      scenario3Loading: false,
      scenario4Loading: false,
      chartDialogVisible: false,
      chartInstance: null
    }
  },
  methods: {
    openPanel() {
      this.drawerVisible = true
    },

    // --- Scenario 1: Fiber/4G Switching ---
    runScenario1() {
      this.scenario1Loading = true
      // 1. Loading
      const loadingMsg = ElMessage({
        message: '正在检测链路状态...',
        type: 'info',
        duration: 0
      })

      setTimeout(() => {
        loadingMsg.close()
        this.scenario1Loading = false
        
        // 2. Error Message
        ElMessage.error({
          message: '告警：主用光纤通道中断！设备ID：10005',
          duration: 4000
        })

        // 3. Warning Message
        setTimeout(() => {
          ElMessage.warning({
            message: '正在执行自愈：切换至 4G 通道...',
            duration: 3000
          })

          // 4. Action & 5. Success
          setTimeout(() => {
             // Emit mocked event if you want parent to handle log
            this.$emit('mock-alert', { 
              time: new Date().toLocaleTimeString(), 
              device: '10005', 
              content: '光纤中断，已切换至 4G', 
              status: '成功' 
            })

            ElNotification({
              title: '切换成功',
              message: '当前通道：4G (信号强度：强)',
              type: 'success',
              duration: 5000
            })
          }, 2000)

        }, 1500)
      }, 1000)
    },

    // --- Scenario 2: Dual-mode Carrier ---
    runScenario2() {
      this.scenario2Loading = true
      
      // 1. Warning
      ElMessage.warning('主通道信号微弱，正在切换至备用通道...')

      setTimeout(() => {
        this.scenario2Loading = false
        // 3. Success
        ElMessage.success('备用通道启用成功。')
        
        // 4. Open Chart
        setTimeout(() => {
          this.chartDialogVisible = true
        }, 500)
      }, 1500)
    },

    initChart() {
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }
      this.chartInstance = echarts.init(this.$refs.chartContainer)
      
      // 30 days labels
      const days = Array.from({length: 30}, (_, i) => `第 ${i+1} 天`)
      
      // Data: stable around 80-90, then drops to 0 at the end
      const data = []
      for(let i=0; i<28; i++) {
        data.push(Math.floor(Math.random() * (95 - 80 + 1) + 80))
      }
      data.push(40) // drop start
      data.push(0)  // fault

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: days
        },
        yAxis: {
          type: 'value',
          name: '信号质量'
        },
        series: [
          {
            name: '信号强度',
            data: data,
            type: 'line',
            smooth: true,
            lineStyle: {
              color: '#F56C6C',
              width: 3
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(245, 108, 108, 0.5)' },
                { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
              ])
            }
          }
        ]
      }
      
      this.chartInstance.setOption(option)
    },

    // --- Scenario 3: AI Fault Diagnosis ---
    runScenario3() {
      this.scenario3Loading = true
      // 1. Loading
      const loadingInstance = ElMessage({
        message: 'AI 模型正在分析波形特征...',
        type: 'info',
        icon: 'el-icon-loading',
        duration: 0
      })

      setTimeout(() => {
        loadingInstance.close()
        
        // 2. Info Result
        ElMessage({
          message: 'AI 诊断结果：外部施工震动 (置信度：98.5%)',
          type: 'info',
          duration: 3000
        })

        setTimeout(() => {
          // 3. Warning
          ElMessage.warning('检测到风险，正在切换至公网服务...')
          
          setTimeout(() => {
             this.scenario3Loading = false
             // 4. Success
             ElMessage.success('公网服务切换完成。')
          }, 1500)

        }, 1500)

      }, 2000)
    },

    // --- Scenario 4: Low Battery Warning ---
    runScenario4() {
      this.scenario4Loading = true
      
      // 1. 初始状态 - 市电中断，切换到备用电源
      ElNotification({
        title: '⚠️ 市电中断',
        message: '市电供电中断，系统已切换至备用电源（UPS）',
        type: 'warning',
        duration: 4000
      })

      // 2. 模拟电量逐步下降
      setTimeout(() => {
        ElMessage({
          message: '📊 当前备用电源电量：45%，预计可用时长：108分钟',
          type: 'info',
          duration: 3000
        })

        // 3. 电量继续下降到告警阈值
        setTimeout(() => {
          ElMessage.warning('📉 电量降至：25%，预计可用时长：60分钟')

          // 4. 电量低于20%阈值，触发紧急告警
          setTimeout(() => {
            this.scenario4Loading = false
            
            ElNotification({
              title: '🔴 紧急告警 - 备用电源电量低',
              message: '备用电源电量已低于20%阈值！当前电量：18%，预计可用时长：43分钟。请立即检查市电或更换电池！',
              type: 'error',
              duration: 10000
            })

            // 5. 显示预警建议
            setTimeout(() => {
              ElMessage({
                message: '💡 建议：已通知运维人员，请尽快恢复市电供应',
                type: 'success',
                duration: 5000
              })
            }, 2000)
          }, 2000)
        }, 2000)
      }, 1500)
    }
  }
}
</script>

<style scoped>
.demo-float-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.scenario-item {
  margin-bottom: 24px;
  padding: 12px;
  border-radius: 8px;
  background-color: #f5f7fa;
}

.scenario-item h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #606266;
}

.control-panel-content {
  padding: 0 10px;
}
</style>
