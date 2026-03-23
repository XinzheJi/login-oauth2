<template>
  <div class="home">
    <el-card class="welcome-section">
      <div class="user-greeting">
        <div class="greeting-icon">👋</div>
      <h2>欢迎，{{ userStore.userInfo.username }}</h2>
      <div class="roles">
          <el-tag 
            v-for="(role, index) in userStore.userInfo.roles" 
            :key="index" 
            type="primary" 
            effect="light" 
            class="role-tag"
          >
          {{ typeof role === 'object' ? role.name : role }}
          </el-tag>
        </div>
      </div>
      <div class="welcome-message">
        <p>欢迎使用权限管理系统。此系统允许您管理用户、角色和权限，实现灵活的访问控制。</p>
        <p>请使用左侧菜单或下方功能卡片导航到各个功能模块。</p>
    </div>
    </el-card>
    
    <!-- 告警数据卡片 -->
    <el-card v-if="hasAlarmPermission" class="alarm-section">
      <template #header>
        <div class="card-header">
          <h3>告警概览</h3>
          <el-button type="primary" size="small" @click="navigateTo('/power-alarms')">查看更多</el-button>
        </div>
      </template>
      
      <el-row :gutter="20" class="alarm-row">
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="alarm-card">
            <div class="alarm-content">
              <el-icon class="alarm-icon warning"><WarningFilled /></el-icon>
              <div class="alarm-value">{{ alarmStats.unprocessedAlarms }}</div>
              <div class="alarm-label">未处理告警</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="alarm-card">
            <div class="alarm-content">
              <el-icon class="alarm-icon danger"><CircleCloseFilled /></el-icon>
              <div class="alarm-value">{{ alarmStats.totalAlarms }}</div>
              <div class="alarm-label">总告警数</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="8">
          <el-card shadow="hover" class="alarm-card">
            <div class="alarm-content">
              <el-icon class="alarm-icon success"><SuccessFilled /></el-icon>
              <div class="alarm-value">{{ alarmStats.resolvedAlarms }}</div>
              <div class="alarm-label">已解决告警</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="alarm-actions">
        <el-button type="warning" size="small" @click="navigateTo('/power-alarm-statistics')">
          <el-icon><DataAnalysis /></el-icon>告警分析
        </el-button>
        <el-button type="danger" size="small" @click="navigateTo('/power-alarms')">
          <el-icon><BellFilled /></el-icon>处理告警
        </el-button>
      </div>
    </el-card>
    
    <el-card class="dashboard-section">
      <template #header>
        <div class="card-header">
      <h3>系统功能</h3>
        </div>
      </template>
      
      <el-row :gutter="20" class="feature-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'user:view'">
          <el-card shadow="hover" class="feature-card" @click="navigateTo('/users')">
            <div class="card-content">
              <el-icon class="card-icon"><User /></el-icon>
              <div class="card-title">用户管理</div>
              <div class="card-desc">创建和管理系统用户账号</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'role:view'">
          <el-card shadow="hover" class="feature-card" @click="navigateTo('/roles')">
            <div class="card-content">
              <el-icon class="card-icon"><Key /></el-icon>
          <div class="card-title">角色管理</div>
              <div class="card-desc">定义系统角色及其权限</div>
        </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'permission:view'">
          <el-card shadow="hover" class="feature-card highlight" @click="navigateTo('/permissions')">
            <div class="card-content">
              <el-icon class="card-icon"><Lock /></el-icon>
          <div class="card-title">权限管理</div>
              <div class="card-desc">配置和分配系统权限</div>
              <el-button type="primary" size="small" class="enter-btn">
                立即进入 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'tenant:view'">
          <el-card shadow="hover" class="feature-card" @click="navigateTo('/tenants')">
            <div class="card-content">
              <el-icon class="card-icon"><OfficeBuilding /></el-icon>
              <div class="card-title">租户管理</div>
              <div class="card-desc">管理多租户环境</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'power:device'">
          <el-card shadow="hover" class="feature-card" @click="navigateTo('/power-devices')">
            <div class="card-content">
              <el-icon class="card-icon"><Cpu /></el-icon>
              <div class="card-title">电源台账管理</div>
              <div class="card-desc">管理电源设备台账</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-permission="'power:alarm'">
          <el-card shadow="hover" class="feature-card" @click="navigateTo('/power-alarms')">
            <div class="card-content">
              <el-icon class="card-icon"><BellFilled /></el-icon>
              <div class="card-title">告警管理</div>
              <div class="card-desc">处理电源设备告警</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    
    <el-card class="stats-section">
      <template #header>
        <div class="card-header">
          <h3>系统概览</h3>
        </div>
      </template>
      
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6" :md="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><User /></el-icon>
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户数量</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><Avatar /></el-icon>
              <div class="stat-value">{{ stats.roleCount }}</div>
              <div class="stat-label">角色数量</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><Lock /></el-icon>
              <div class="stat-value">{{ stats.permissionCount }}</div>
              <div class="stat-label">权限数量</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
              <div class="stat-value">{{ stats.tenantCount }}</div>
              <div class="stat-label">租户数量</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="4">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <el-icon class="stat-icon"><Cpu /></el-icon>
              <div class="stat-value">{{ stats.deviceCount }}</div>
              <div class="stat-label">电源设备数量</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 快速访问区域 -->
    <el-card class="quick-access-section">
      <template #header>
        <div class="card-header">
          <h3>快速访问</h3>
        </div>
      </template>
      
      <div class="quick-access-content">
        <el-button type="primary" @click="navigateTo('/permissions')">
          <el-icon><Lock /></el-icon>权限管理
        </el-button>
        <el-button type="success" @click="navigateTo('/roles')">
          <el-icon><Key /></el-icon>角色管理
        </el-button>
        <el-button type="info" @click="navigateTo('/users')">
          <el-icon><User /></el-icon>用户管理
        </el-button>
        <el-button type="warning" @click="navigateTo('/power-devices')">
          <el-icon><Cpu /></el-icon>电源台账
        </el-button>
        <el-button v-if="hasAlarmPermission" type="danger" @click="navigateTo('/power-alarms')">
          <el-icon><BellFilled /></el-icon>告警管理
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
// @ is an alias to /src
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { ref, onMounted, computed } from 'vue'
import { getAlarmStatistics, getUnprocessedAlarmCount } from '@/api/powerAlarm'
import { 
  User, 
  Key, 
  Lock, 
  OfficeBuilding, 
  Avatar, 
  ArrowRight,
  Cpu,
  BellFilled,
  WarningFilled,
  CircleCloseFilled,
  SuccessFilled,
  DataAnalysis
} from '@element-plus/icons-vue'

export default {
  name: 'HomeView',
  components: {
    User,
    Key,
    Lock,
    OfficeBuilding,
    Avatar,
    ArrowRight,
    Cpu,
    BellFilled,
    WarningFilled,
    CircleCloseFilled,
    SuccessFilled,
    DataAnalysis
  },
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    
    // 统计数据
    const stats = ref({
      userCount: 12,
      roleCount: 5,
      permissionCount: 24,
      tenantCount: 3,
      deviceCount: 8
    })
    
    // 告警统计数据
    const alarmStats = ref({
      totalAlarms: 0,
      unprocessedAlarms: 0,
      resolvedAlarms: 0
    })
    
    // 检查用户是否有告警权限
    const hasAlarmPermission = computed(() => {
      const userPermissions = userStore.userInfo.permissions || []
      return userPermissions.includes('power:alarm')
    })
    
    // 导航到指定路由
    const navigateTo = (path) => {
      router.push(path)
    }
    
    // 获取告警统计数据
    const fetchAlarmStats = async () => {
      if (!hasAlarmPermission.value) return
      
      try {
        const res = await getAlarmStatistics()
        if (res.data) {
          alarmStats.value.totalAlarms = res.data.totalAlarms || 0
          alarmStats.value.unprocessedAlarms = res.data.unprocessedAlarms || 0
          alarmStats.value.resolvedAlarms = res.data.resolvedAlarms || 0
        }
      } catch (error) {
        console.error('获取告警统计数据失败:', error)
      }
    }
    
    // 获取统计数据
    onMounted(() => {
      // 真实场景中，这里应该调用API获取系统概览数据
      // 这里使用模拟数据
      
      // 获取告警统计数据
      fetchAlarmStats()
    })
    
    return {
      userStore,
      navigateTo,
      stats,
      alarmStats,
      hasAlarmPermission
    }
  }
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.welcome-section,
.dashboard-section,
.stats-section,
.alarm-section,
.quick-access-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.user-greeting {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.greeting-icon {
  font-size: 24px;
  margin-right: 10px;
}

.role-tag {
  margin-right: 5px;
  margin-top: 5px;
}

.feature-row {
  margin-bottom: 10px;
}

.feature-card {
  height: 160px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 20px;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.feature-card.highlight {
  border-color: #409EFF;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.card-icon {
  font-size: 32px;
  margin-bottom: 10px;
  color: #409EFF;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.card-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
}

.enter-btn {
  margin-top: 10px;
}

.stats-row,
.alarm-row {
  margin-bottom: 10px;
}

.stat-card,
.alarm-card {
  margin-bottom: 20px;
}

.stat-content,
.alarm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10px;
}

.stat-icon,
.alarm-icon {
  font-size: 24px;
  margin-bottom: 10px;
  color: #409EFF;
}

.alarm-icon.warning {
  color: #E6A23C;
}

.alarm-icon.danger {
  color: #F56C6C;
}

.alarm-icon.success {
  color: #67C23A;
}

.stat-value,
.alarm-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label,
.alarm-label {
  font-size: 14px;
  color: #666;
}

.quick-access-content {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.alarm-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
