<template>
  <div class="health-check-view">
    <div class="page-header">
      <h1>系统健康检查</h1>
      <p>实时监控系统各项服务的运行状态，确保系统稳定运行</p>
    </div>

    <!-- 整体健康状态卡片 -->
    <el-card class="overall-health-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统整体状态</span>
          <el-button type="primary" @click="refreshHealth" :loading="loading">
            <el-icon><RefreshRight /></el-icon>
            刷新检查
          </el-button>
        </div>
      </template>

      <div class="overall-status">
        <div class="status-indicator">
          <el-icon :class="overallHealthStatus === 'UP' ? 'status-up' : 'status-down'">
            <CircleCheck v-if="overallHealthStatus === 'UP'" />
            <CircleClose v-else />
          </el-icon>
          <span class="status-text">{{ overallHealthStatus === 'UP' ? '正常' : '异常' }}</span>
        </div>
        <div class="status-details">
          <p>最后检查时间: {{ formatTime(lastCheckTime) }}</p>
          <p>检查周期: {{ checkInterval }}秒</p>
        </div>
      </div>
    </el-card>

    <!-- 各组件健康状态 -->
    <div class="health-components">
      <el-row :gutter="20">
        <!-- 应用服务 -->
        <el-col :span="12">
          <el-card class="component-card" shadow="hover">
            <template #header>
              <div class="component-header">
                <el-icon class="component-icon">
                  <Monitor />
                </el-icon>
                <span>应用服务</span>
                <el-tag :type="getStatusType(applicationHealth.status)" size="small">
                  {{ applicationHealth.status === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </template>
            <div class="component-details">
              <p><strong>服务名称:</strong> {{ applicationHealth.service }}</p>
              <p><strong>运行状态:</strong> {{ applicationHealth.details?.uptime || '未知' }}</p>
              <p><strong>内存使用:</strong> {{ applicationHealth.details?.memory || '未知' }}</p>
              <p><strong>CPU使用:</strong> {{ applicationHealth.details?.cpu || '未知' }}</p>
            </div>
          </el-card>
        </el-col>

        <!-- 数据库 -->
        <el-col :span="12">
          <el-card class="component-card" shadow="hover">
            <template #header>
              <div class="component-header">
                <el-icon class="component-icon">
                  <Coin />
                </el-icon>
                <span>数据库服务</span>
                <el-tag :type="getStatusType(databaseHealth.status)" size="small">
                  {{ databaseHealth.status === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </template>
            <div class="component-details">
              <p><strong>数据库类型:</strong> {{ databaseHealth.database }}</p>
              <p><strong>连接状态:</strong> {{ databaseHealth.details?.connection || '未知' }}</p>
              <p><strong>响应时间:</strong> {{ databaseHealth.details?.responseTime || '未知' }}</p>
              <p><strong>最后检查:</strong> {{ formatTime(databaseHealth.timestamp) }}</p>
            </div>
          </el-card>
        </el-col>

        <!-- 缓存服务 -->
        <el-col :span="12">
          <el-card class="component-card" shadow="hover">
            <template #header>
              <div class="component-header">
                <el-icon class="component-icon">
                  <Lightning />
                </el-icon>
                <span>缓存服务</span>
                <el-tag :type="getStatusType(cacheHealth.status)" size="small">
                  {{ cacheHealth.status === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </template>
            <div class="component-details">
              <p><strong>缓存类型:</strong> {{ cacheHealth.cache }}</p>
              <p><strong>连接状态:</strong> {{ cacheHealth.details?.connection || '未知' }}</p>
              <p><strong>内存使用:</strong> {{ cacheHealth.details?.memoryUsage || '未知' }}</p>
              <p><strong>命中率:</strong> {{ cacheHealth.details?.hitRate || '未知' }}</p>
            </div>
          </el-card>
        </el-col>

        <!-- 网络连接 -->
        <el-col :span="12">
          <el-card class="component-card" shadow="hover">
            <template #header>
              <div class="component-header">
                <el-icon class="component-icon">
                  <Connection />
                </el-icon>
                <span>网络连接</span>
                <el-tag :type="getStatusType(networkHealth.status)" size="small">
                  {{ networkHealth.status === 'UP' ? '正常' : '异常' }}
                </el-tag>
              </div>
            </template>
            <div class="component-details">
              <p><strong>协议类型:</strong> {{ networkHealth.network }}</p>
              <p><strong>响应时间:</strong> {{ networkHealth.details?.responseTime || '未知' }}</p>
              <p><strong>延迟状态:</strong> {{ networkHealth.details?.latency || '未知' }}</p>
              <p><strong>最后检查:</strong> {{ formatTime(networkHealth.timestamp) }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 检查历史记录 -->
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>最近检查历史</span>
          <el-button type="info" @click="clearHistory" size="small">清空历史</el-button>
        </div>
      </template>

      <el-table :data="checkHistory" stripe style="width: 100%">
        <el-table-column prop="timestamp" label="检查时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="overallStatus" label="整体状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.overallStatus)">
              {{ scope.row.overallStatus === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="components.application.status" label="应用服务" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.components?.application?.status)" size="small">
              {{ scope.row.components?.application?.status === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="components.database.status" label="数据库" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.components?.database?.status)" size="small">
              {{ scope.row.components?.database?.status === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="components.cache.status" label="缓存" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.components?.cache?.status)" size="small">
              {{ scope.row.components?.cache?.status === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="components.network.status" label="网络" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.components?.network?.status)" size="small">
              {{ scope.row.components?.network?.status === 'UP' ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="检查耗时" width="120">
          <template #default="scope">
            {{ scope.row.duration ? scope.row.duration + 'ms' : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 设置面板 -->
    <el-card class="settings-card" shadow="hover">
      <template #header>
        <span>检查设置</span>
      </template>

      <el-form :model="settings" label-width="120px">
        <el-form-item label="自动检查">
          <el-switch v-model="settings.autoCheck" @change="toggleAutoCheck" />
        </el-form-item>
        <el-form-item label="检查间隔(秒)" v-if="settings.autoCheck">
          <el-slider
            v-model="settings.checkInterval"
            :min="5"
            :max="300"
            :step="5"
            show-input
            @change="updateCheckInterval"
          />
        </el-form-item>
        <el-form-item label="显示通知">
          <el-switch v-model="settings.showNotifications" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  RefreshRight,
  CircleCheck,
  CircleClose,
  Monitor,
  Coin,
  Lightning,
  Connection
} from '@element-plus/icons-vue'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import {
  getHealthReport,
  checkApplicationHealth,
  checkDatabaseHealth,
  checkCacheHealth,
  checkNetworkHealth
} from '@/api/health'

// 响应式数据
const loading = ref(false)
const overallHealthStatus = ref('UNKNOWN')
const lastCheckTime = ref(null)
const checkInterval = ref(30)
const autoCheckTimer = ref(null)

// 各组件健康状态
const applicationHealth = ref({
  status: 'UNKNOWN',
  service: 'system-b',
  details: {}
})

const databaseHealth = ref({
  status: 'UNKNOWN',
  database: 'MySQL 8.0+',
  details: {}
})

const cacheHealth = ref({
  status: 'UNKNOWN',
  cache: 'Redis 6.x',
  details: {}
})

const networkHealth = ref({
  status: 'UNKNOWN',
  network: 'HTTP/HTTPS',
  details: {}
})

// 检查历史记录
const checkHistory = ref([])

// 设置
const settings = ref({
  autoCheck: false,
  checkInterval: 30,
  showNotifications: true
})

// 方法
const formatTime = (timestamp) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  return status === 'UP' ? 'success' : 'danger'
}

const refreshHealth = async () => {
  loading.value = true
  try {
    const startTime = Date.now()
    const report = await getHealthReport()
    const endTime = Date.now()

    // 更新整体状态
    overallHealthStatus.value = report.status
    lastCheckTime.value = report.timestamp

    // 更新各组件状态
    applicationHealth.value = report.components.application
    databaseHealth.value = report.components.database
    cacheHealth.value = report.components.cache
    networkHealth.value = report.components.network

    // 添加到历史记录
    checkHistory.value.unshift({
      timestamp: report.timestamp,
      overallStatus: report.status,
      components: report.components,
      duration: endTime - startTime
    })

    // 保留最近20条记录
    if (checkHistory.value.length > 20) {
      checkHistory.value = checkHistory.value.slice(0, 20)
    }

    // 保存到本地存储
    saveHistory()

    // 通知
    if (settings.value.showNotifications) {
      if (report.status === 'UP') {
        ElMessage.success('系统健康检查完成，所有服务正常')
      } else {
        ElMessage.warning('系统健康检查完成，发现异常服务')
      }
    }

  } catch (error) {
    console.error('健康检查失败:', error)
    ElMessage.error('健康检查失败: ' + error.message)
    overallHealthStatus.value = 'DOWN'
    lastCheckTime.value = Date.now()
  } finally {
    loading.value = false
  }
}

const clearHistory = () => {
  ElMessageBox.confirm('确定要清空所有检查历史记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    checkHistory.value = []
    localStorage.removeItem('healthCheckHistory')
    ElMessage.success('历史记录已清空')
  }).catch(() => {
    // 用户取消操作
  })
}

const toggleAutoCheck = (value) => {
  if (value) {
    startAutoCheck()
  } else {
    stopAutoCheck()
  }
  saveSettings()
}

const updateCheckInterval = (value) => {
  checkInterval.value = value
  if (settings.value.autoCheck) {
    stopAutoCheck()
    startAutoCheck()
  }
  saveSettings()
}

const startAutoCheck = () => {
  if (autoCheckTimer.value) {
    clearInterval(autoCheckTimer.value)
  }
  autoCheckTimer.value = setInterval(() => {
    refreshHealth()
  }, settings.value.checkInterval * 1000)
}

const stopAutoCheck = () => {
  if (autoCheckTimer.value) {
    clearInterval(autoCheckTimer.value)
    autoCheckTimer.value = null
  }
}

const saveSettings = () => {
  localStorage.setItem('healthCheckSettings', JSON.stringify(settings.value))
}

const saveHistory = () => {
  localStorage.setItem('healthCheckHistory', JSON.stringify(checkHistory.value))
}

const loadSettings = () => {
  const saved = localStorage.getItem('healthCheckSettings')
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      settings.value = { ...settings.value, ...parsed }
      checkInterval.value = parsed.checkInterval || 30
    } catch (error) {
      console.error('加载设置失败:', error)
    }
  }
}

const loadHistory = () => {
  const saved = localStorage.getItem('healthCheckHistory')
  if (saved) {
    try {
      checkHistory.value = JSON.parse(saved)
    } catch (error) {
      console.error('加载历史记录失败:', error)
    }
  }
}

// 生命周期钩子
onMounted(() => {
  loadSettings()
  loadHistory()
  refreshHealth() // 初始检查

  if (settings.value.autoCheck) {
    startAutoCheck()
  }
})

onUnmounted(() => {
  stopAutoCheck()
})
</script>

<style scoped>
.health-check-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  margin: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overall-health-card {
  margin-bottom: 20px;
}

.overall-status {
  display: flex;
  align-items: center;
  gap: 30px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator .el-icon {
  font-size: 24px;
}

.status-up {
  color: #67c23a;
}

.status-down {
  color: #f56c6c;
}

.status-text {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.status-details p {
  margin: 5px 0;
  color: #666;
  font-size: 14px;
}

.health-components {
  margin-bottom: 20px;
}

.component-card {
  height: 100%;
}

.component-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.component-icon {
  font-size: 20px;
  color: #409eff;
}

.component-details p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.history-card {
  margin-bottom: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .health-check-view {
    padding: 10px;
  }

  .overall-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .component-header {
    flex-wrap: wrap;
  }
}
</style>
