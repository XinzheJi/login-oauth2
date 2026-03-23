import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useDeviceStore = defineStore('device', () => {
  // 设备列表数据
  const devices = ref([])
  
  // 当前选中的设备ID
  const selectedDeviceId = ref('')
  
  // 设备运行数据存储（按设备ID存储）
  const deviceDataMap = ref(new Map())
  
  // 获取所有设备
  const allDevices = computed(() => devices.value)
  
  // 获取当前选中的设备
  const selectedDevice = computed(() => {
    return devices.value.find(device => device.deviceId === selectedDeviceId.value)
  })
  
  // 添加设备
  const addDevice = (device) => {
    const existingIndex = devices.value.findIndex(d => d.deviceId === device.deviceId)
    if (existingIndex >= 0) {
      devices.value[existingIndex] = device
    } else {
      devices.value.push(device)
    }
  }
  
  // 批量添加设备
  const addDevices = (deviceList) => {
    deviceList.forEach(device => {
      addDevice(device)
    })
  }
  
  // 更新设备信息
  const updateDevice = (deviceId, updates) => {
    const device = devices.value.find(d => d.deviceId === deviceId)
    if (device) {
      Object.assign(device, updates)
    }
  }
  
  // 删除设备
  const removeDevice = (deviceId) => {
    const index = devices.value.findIndex(d => d.deviceId === deviceId)
    if (index >= 0) {
      devices.value.splice(index, 1)
      // 同时删除设备数据
      deviceDataMap.value.delete(deviceId)
      // 如果删除的是当前选中的设备，清空选择
      if (selectedDeviceId.value === deviceId) {
        selectedDeviceId.value = ''
      }
    }
  }
  
  // 设置当前选中的设备
  const setSelectedDevice = (deviceId) => {
    selectedDeviceId.value = deviceId
  }
  
  // 根据ID查找设备
  const findDeviceById = (deviceId) => {
    return devices.value.find(device => device.deviceId === deviceId)
  }
  
  // 清空所有设备
  const clearDevices = () => {
    devices.value = []
    deviceDataMap.value.clear()
    selectedDeviceId.value = ''
  }
  
  // 生成设备运行数据（支持不同设备特征）
  const generateDeviceData = (deviceId, timeRange = '30d') => {
    const data = []
    const now = new Date()
    let days = 30
    
    // 根据时间范围确定天数
    switch (timeRange) {
      case '7d':
        days = 7
        break
      case '30d':
        days = 30
        break
      case '90d':
        days = 90
        break
    }
    
    // 根据设备ID生成不同的基础参数（模拟不同设备的老化特征）
    const deviceSeed = deviceId.charCodeAt(deviceId.length - 1) % 5
    let agingBase = 20 + deviceSeed * 5  // 不同设备初始老化程度不同
    let riskBase = 10 + deviceSeed * 3   // 不同设备初始风险不同
    
    // 设备特征参数
    const agingRate = 0.2 + deviceSeed * 0.1  // 老化速率
    const riskSensitivity = 0.3 + deviceSeed * 0.1  // 风险敏感度
    const stability = 0.8 - deviceSeed * 0.1  // 稳定性（影响波动幅度）
    
    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
      
      // 老化评分：基于设备特征
      const timeProgress = (days - i) / days
      const baseAging = (days - i) * agingRate
      const dailyCycle = Math.sin(i * 0.2) * 2 * stability
      const weeklyCycle = Math.sin(i * 0.03) * 3 * stability
      const envFactor = Math.sin(i * 0.1) * 1.5 * stability
      const maintenanceEffect = Math.random() > 0.95 ? -8 : 0
      
      agingBase += (Math.random() - 0.5) * 0.4 * stability
      const agingScore = agingBase + baseAging + dailyCycle + weeklyCycle + envFactor + maintenanceEffect
      
      // 风险评分：基于设备特征和老化评分
      const baseRisk = agingScore * riskSensitivity + (days - i) * 0.2
      const loadPeak = Math.sin(i * 0.2) * 8 * stability
      const tempEffect = Math.sin(i * 0.15) * 4 * stability
      const randomEvent = Math.random() > 0.92 ? (Math.random() * 20 + 5) : 0
      
      riskBase += (Math.random() - 0.5) * 0.3 * stability
      const riskScore = riskBase + baseRisk + loadPeak + tempEffect + randomEvent
      
      // 其他参数（根据设备类型调整）
      const voltage = 220 + Math.sin(i * 0.1) * 5 + (Math.random() - 0.5) * 2
      const temperature = 35 + Math.sin(i * 0.2) * 8 + (Math.random() - 0.5) * 3
      const memoryUsage = 45 + Math.sin(i * 0.3) * 15 + (Math.random() - 0.5) * 5
      const alarmCount = riskScore > 60 ? Math.floor(Math.random() * 3) : 0
      
      data.push({
        timestamp: date.toISOString().split('T')[0],
        aging_score: Math.max(0, Math.min(100, Math.round(agingScore * 10) / 10)),
        risk_score: Math.max(0, Math.min(100, Math.round(riskScore * 10) / 10)),
        voltage: Math.round(voltage * 10) / 10,
        temperature: Math.round(temperature * 10) / 10,
        memory_usage: Math.max(0, Math.min(100, Math.round(memoryUsage * 10) / 10)),
        alarm_count: alarmCount
      })
    }
    
    return data
  }
  
  // 获取设备运行数据
  const getDeviceData = (deviceId, timeRange = '30d') => {
    const cacheKey = `${deviceId}_${timeRange}`
    if (deviceDataMap.value.has(cacheKey)) {
      return deviceDataMap.value.get(cacheKey)
    }
    
    const data = generateDeviceData(deviceId, timeRange)
    deviceDataMap.value.set(cacheKey, data)
    return data
  }
  
  // 更新设备运行数据
  const updateDeviceData = (deviceId, timeRange = '30d') => {
    const cacheKey = `${deviceId}_${timeRange}`
    const data = generateDeviceData(deviceId, timeRange)
    deviceDataMap.value.set(cacheKey, data)
    return data
  }
  
  // 获取设备运行时长
  const getDeviceRunningTime = (device) => {
    if (!device.installDate) return '未知'
    
    const installDate = new Date(device.installDate)
    const now = new Date()
    const diffTime = now - installDate
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
    const years = Math.floor(diffDays / 365)
    const months = Math.floor((diffDays % 365) / 30)
    
    return `${years}年${months}个月`
  }
  
  return {
    // 状态
    devices,
    selectedDeviceId,
    deviceDataMap,
    
    // 计算属性
    allDevices,
    selectedDevice,
    
    // 方法
    addDevice,
    addDevices,
    updateDevice,
    removeDevice,
    setSelectedDevice,
    findDeviceById,
    clearDevices,
    generateDeviceData,
    getDeviceData,
    updateDeviceData,
    getDeviceRunningTime
  }
})