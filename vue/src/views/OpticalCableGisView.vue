<template>
  <div class="gis-monitor-container">
    <!-- Map Container -->
    <div id="amap-container" class="map-container"></div>

    <!-- Dashboard Overlay -->
    <div class="dashboard-overlay">
      <div class="stat-card">
        <div class="stat-title">光缆总里程</div>
        <div class="stat-value">45.8 <span class="unit">km</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-title">安全运行</div>
        <div class="stat-value">1,205 <span class="unit">天</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-title">光纤利用率</div>
        <div class="stat-value">68.5 <span class="unit">%</span></div>
      </div>
    </div>

    <!-- Timeline/Message Banner -->
    <transition name="el-fade-in-linear">
      <div v-if="alertMessage" class="alert-banner" :class="alertType">
        <el-icon class="banner-icon">
          <Warning v-if="alertType==='error'" />
          <CircleCheck v-else-if="alertType==='success'" />
          <InfoFilled v-else />
        </el-icon>
        <span class="alert-text">{{ alertMessage }}</span>
      </div>
    </transition>

    <!-- Control Panel -->
    <div class="control-panel">
      <div class="panel-header">光缆自愈演练</div>
      <el-button-group class="control-group">
        <el-button type="danger" @click="simulateBreak" :disabled="isBroken || isSimulating">模拟光缆中断</el-button>
        <el-button type="warning" @click="executeSelfHealing" :disabled="!isBroken || isHealed">执行自愈切换</el-button>
        <el-button type="info" @click="resetSimulation" :disabled="!isBroken && !isHealed">重置演示</el-button>
      </el-button-group>
      <div class="step-indicator">
        <el-steps :active="activeStep" simple finish-status="success">
          <el-step title="正常运行" icon="CircleCheck"></el-step>
          <el-step title="光缆中断" icon="Warning"></el-step>
          <el-step title="OTDR定位" icon="Aim"></el-step>
          <el-step title="4G接管" icon="Connection"></el-step>
        </el-steps>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Warning, CircleCheck, InfoFilled, Aim, Connection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// State
const isBroken = ref(false)
const isHealed = ref(false)
const isSimulating = ref(false)
const activeStep = ref(0)
const alertMessage = ref('')
const alertType = ref('info')

let map = null
let primaryPolyline = null
let backupPolyline = null
let breakMarker = null
let otdrMarker = null

// Coordinates (Lng, Lat) for AMap
const pathCoords = [
  [121.4737, 31.2304], // Shanghai People's Square (A)
  [121.4800, 31.2350],
  [121.4900, 31.2400],
  [121.5000, 31.2450], // Mid
  [121.5100, 31.2500], // Break Point area
  [121.5200, 31.2550],
  [121.5300, 31.2600]  // End (B)
]

const backupPathCoords = [
  [121.4737, 31.2304], // Start
  [121.4800, 31.2400], // Detour
  [121.5000, 31.2500], // Base Station
  [121.5200, 31.2600], // Detour
  [121.5300, 31.2600]  // End
]

const breakPoint = [121.5100, 31.2500]

const initMap = () => {
  if (!window.AMap) {
    ElMessage.error('高德地图脚本未能加载，请检查网络或Key配置')
    return
  }

  // Initialize Map
  map = new window.AMap.Map('amap-container', {
    zoom: 13,
    center: [121.5000, 31.2450],
    viewMode: '3D', // Use 3D mode
    pitch: 45,
    mapStyle: 'amap://styles/darkblue' // Tech style
  })

  // Load plugins
  window.AMap.plugin(['AMap.MoveAnimation'], function(){
      console.log('AMap.MoveAnimation loaded');
  });

  map.on('complete', () => {
    // 1. Draw Primary Line (Green)
    primaryPolyline = new window.AMap.Polyline({
      path: pathCoords,
      strokeColor: '#67C23A',
      strokeWeight: 6,
      strokeOpacity: 0.9,
      zIndex: 50,
      showDir: true
    })
    map.add(primaryPolyline)

    // 2. Draw Backup Line (Initially Hidden)
    backupPolyline = new window.AMap.Polyline({
      path: backupPathCoords,
      strokeColor: '#409EFF',
      strokeWeight: 4,
      strokeStyle: 'dashed',
      strokeDasharray: [10, 5],
      strokeOpacity: 0, // Hidden
      zIndex: 40
    })
    map.add(backupPolyline)

    // 3. Add Station Markers
    const startMarker = new window.AMap.Marker({
      position: pathCoords[0],
      title: '配电房 A',
      label: { content: '配电房 A', offset: new window.AMap.Pixel(0, -20), direction: 'top' }
    })
    const endMarker = new window.AMap.Marker({
      position: pathCoords[pathCoords.length - 1],
      title: '主站 B',
      label: { content: '主站 B', offset: new window.AMap.Pixel(0, -20), direction: 'top' }
    })
    const baseStationMarker = new window.AMap.Marker({
      position: [121.5000, 31.2500],
      title: '4G 基站',
      icon: new window.AMap.Icon({
        size: new window.AMap.Size(25, 34),
        image: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
        imageSize: new window.AMap.Size(25, 34)
      }),
      label: { content: '4G 基站', offset: new window.AMap.Pixel(0, -20), direction: 'right' }
    })
    
    map.add([startMarker, endMarker, baseStationMarker])
  })
}

const simulateBreak = () => {
  if (!map) return
  isSimulating.value = true
  alertMessage.value = '正在监测光缆传输状态...'
  activeStep.value = 1

  // Use a moving marker to simulate OTDR scan
  const otdrStart = pathCoords[0]
  const otdrEnd = breakPoint

  otdrMarker = new window.AMap.Marker({
    position: otdrStart,
    icon: new window.AMap.Icon({
      size: new window.AMap.Size(20, 20),
      image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
      imageSize: new window.AMap.Size(20, 20)
    }),
    offset: new window.AMap.Pixel(-10, -10),
    zIndex: 100
  })
  map.add(otdrMarker)

  otdrMarker.moveTo(otdrEnd, {
    duration: 1500,
    easing: (t) => t * t
  })

  otdrMarker.on('moveend', () => {
    // Break detected
    isBroken.value = true
    isSimulating.value = false
    activeStep.value = 2
    alertMessage.value = '告警：监测到主用光缆中断！OTDR已定位断点。'
    alertType.value = 'error'

    // Add Break Marker
    breakMarker = new window.AMap.Marker({
      position: breakPoint,
      content: '<div style="background:red;color:white;padding:5px;border-radius:4px;font-weight:bold;">❌ 断点</div>',
      offset: new window.AMap.Pixel(-20, -30),
      zIndex: 101
    })
    map.add(breakMarker)

    // Change Primary Line Style to indicate break (e.g., turn red)
    primaryPolyline.setOptions({ strokeColor: '#F56C6C', strokeStyle: 'dashed' })
    
    map.remove(otdrMarker)
  })
}

const executeSelfHealing = () => {
  if (!map) return
  activeStep.value = 3
  
  // Fade in backup line
  let opacity = 0
  const interval = setInterval(() => {
    opacity += 0.1
    if (opacity > 1) {
      opacity = 1
      clearInterval(interval)
      
      isHealed.value = true
      activeStep.value = 4
      alertMessage.value = '业务已成功切换至公网加密通道，业务恢复。'
      alertType.value = 'success'
    }
    backupPolyline.setOptions({ strokeOpacity: opacity })
  }, 50)
  
  // Optional: Pulse Animation on backup line if supported or just leave as is
}

const resetSimulation = () => {
  isBroken.value = false
  isHealed.value = false
  isSimulating.value = false
  activeStep.value = 0
  alertMessage.value = ''
  alertType.value = 'info'

  if (map) {
    if (breakMarker) map.remove(breakMarker)
    if (otdrMarker) map.remove(otdrMarker)
    
    primaryPolyline.setOptions({ strokeColor: '#67C23A', strokeStyle: 'solid' })
    backupPolyline.setOptions({ strokeOpacity: 0 })
    
    map.setFitView()
  }
}

onMounted(() => {
  initMap()
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style scoped>
.gis-monitor-container {
  position: relative;
  height: calc(100vh - 60px);
  width: 100%;
  background: #000;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.dashboard-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 10;
}

.stat-card {
  background: rgba(16, 12, 42, 0.85);
  border: 1px solid rgba(64, 158, 255, 0.3);
  padding: 15px 20px;
  border-radius: 4px;
  width: 200px;
  color: #fff;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.2);
}

.stat-title {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-top: 5px;
}
.unit {
  font-size: 14px;
  color: #fff;
  margin-left: 5px;
}

.alert-banner {
  position: absolute;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 30px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0,0,0,0.5);
  z-index: 10;
}
.alert-banner.info { background: rgba(255, 255, 255, 0.95); color: #606266; }
.alert-banner.error { background: rgba(245, 108, 108, 0.95); color: #fff; }
.alert-banner.success { background: rgba(103, 194, 58, 0.95); color: #fff; }

.control-panel {
  position: absolute;
  bottom: 30px;
  right: 30px;
  background: rgba(255, 255, 255, 0.95);
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
  z-index: 10;
}

.panel-header {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}

.control-group {
  display: flex;
  justify-content: space-between; 
  width: 100%;
}
.control-group .el-button {
  flex: 1;
}

.step-indicator {
  margin-top: 20px;
}

.banner-icon {
  font-size: 18px;
}

/* Transitions */
.el-fade-in-linear-enter-active, .el-fade-in-linear-leave-active {
  transition: opacity 0.3s;
}
.el-fade-in-linear-enter-from, .el-fade-in-linear-leave-to {
  opacity: 0;
}
</style>
