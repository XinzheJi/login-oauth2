import { createRouter, createWebHashHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import UserManageView from '../views/UserManageView.vue'
import RoleManageView from '../views/RoleManageView.vue'
import TenantManageView from '../views/TenantManageView.vue'
import PermissionManageView from '../views/PermissionManageView.vue'
import PowerDeviceManageView from '../views/PowerDeviceManageView.vue'
import PowerAlarmManageView from '../views/PowerAlarmManageView.vue'
import PowerAlarmStatisticsView from '../views/PowerAlarmStatisticsView.vue'
import PowerAlarmDetailView from '../views/PowerAlarmDetailView.vue'
import SwitchManageView from '../views/SwitchManageView.vue'
import ModelTypeManageView from '../views/ModelTypeManageView.vue'
import HealthCheckView from '../views/HealthCheckView.vue'
import DashboardView from '../views/DashboardView.vue'
import DeviceAgingFaultView from '../views/DeviceAgingFaultView.vue'
import PageStatusView from '../views/PageStatusView.vue'
import { getToken, getTokenExpiresAt, refreshToken, hasSessionTimedOut, markUserActivity, REFRESH_THRESHOLD_MS } from '@/api/auth'
const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/home',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guest: true }
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/users',
    name: 'users',
    component: UserManageView,
    meta: { requiresAuth: true, permission: 'user:view' }
  },
  {
    path: '/roles',
    name: 'roles',
    component: RoleManageView,
    meta: { requiresAuth: true, permission: 'role:view' }
  },
  { path: '/tenants', name: 'tenants', component: TenantManageView, meta: { requiresAuth: true, permission: 'tenant:view' } },
  {
    path: '/permissions',
    name: 'permissions',
    component: PermissionManageView,
    meta: { requiresAuth: true, permission: 'permission:view' }
  },
  {
    path: '/power-devices',
    name: 'powerDevices',
    component: PowerDeviceManageView,
    meta: { requiresAuth: true, permission: 'power:device' }
  },
  {
    path: '/power-alarms',
    name: 'powerAlarms',
    component: PowerAlarmManageView,
    meta: { requiresAuth: true, permission: 'power:alarm' }
  },
  {
    path: '/power-alarms/:id',
    name: 'powerAlarmDetail',
    component: PowerAlarmDetailView,
    props: true,
    meta: { requiresAuth: true, permission: 'power:alarm' }
  },
  {
    path: '/power-alarm-statistics',
    name: 'powerAlarmStatistics',
    component: PowerAlarmStatisticsView,
    meta: { requiresAuth: true, permission: 'power:alarm' }
  },
  {
    path: '/switch-list',
    component: SwitchManageView,
    name: 'SwitchManageView',
    meta: { title: '交换机管理', icon: 'el-icon-s-platform' }
  },
  {
    path: '/model-type',
    component: ModelTypeManageView,
    name: 'ModelTypeManageView',
    meta: { title: '设备型号管理', icon: 'el-icon-s-platform' }
  },
  {
    path: '/health-check',
    name: 'healthCheck',
    component: HealthCheckView,
    meta: { requiresAuth: true, title: '系统健康检查', icon: 'el-icon-monitor' }
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardView,
    meta: { requiresAuth: true, title: '数据大屏', icon: 'el-icon-monitor' }
  },
  {
    path: '/device-aging-fault',
    name: 'deviceAgingFault',
    component: DeviceAgingFaultView,
    meta: { requiresAuth: true, title: '设备老化与故障感知', icon: 'el-icon-trend-charts' }
  },
  {
    path: '/page-status',
    name: 'pageStatus',
    component: PageStatusView,
    meta: { requiresAuth: true, title: '页面状态检查', icon: 'el-icon-s-platform' }
  },
  {
    path: '/device-ledger',
    name: 'deviceLedger',
    component: () => import('../views/DeviceLedgerView.vue'),
    meta: { requiresAuth: true, title: '装置台账管理', icon: 'Cpu' }
  },
  {
    path: '/topology-monitor',
    name: 'topologyMonitor',
    component: () => import('../views/TopologyMonitor.vue'),
    meta: { requiresAuth: true, title: '通信拓扑监控', icon: 'Share' }
  },
  {
    path: '/switch-history',
    name: 'switchHistory',
    component: () => import('../views/SwitchHistoryView.vue'),
    meta: { requiresAuth: true, title: '切换记录与历史查询', icon: 'DataLine' }
  },
  {
    path: '/carrier-module-ledger',
    name: 'carrierModuleLedger',
    component: () => import('../views/CarrierModuleLedgerView.vue'),
    meta: { requiresAuth: true, title: '载波模块台账', icon: 'Cpu' }
  },
  {
    path: '/dual-mode-topology',
    name: 'dualModeTopology',
    component: () => import('../views/DualModeTopology.vue'),
    meta: { requiresAuth: true, title: '双模拓扑监控', icon: 'Connection' }
  },
  {
    path: '/history-analysis',
    name: 'historyAnalysis',
    component: () => import('../views/HistoryAnalysisView.vue'),
    meta: { requiresAuth: true, title: '历史效能分析', icon: 'PieChart' }
  },
  {
    path: '/optical-cable-gis',
    name: 'opticalCableGis',
    component: () => import('../views/OpticalCableGisView.vue'),
    meta: { requiresAuth: true, title: '光缆GIS全景监控', icon: 'MapLocation' }
  },
  {
    path: '/vibration-analysis',
    name: 'vibrationAnalysis',
    component: () => import('../views/VibrationAnalysisView.vue'),
    meta: { requiresAuth: true, title: 'AI振动分析实验室', icon: 'Histogram' }
  },
  {
    path: '/optical-health',
    name: 'opticalHealth',
    component: () => import('../views/OpticalHealthView.vue'),
    meta: { requiresAuth: true, title: '光缆健康与老化分析', icon: 'DataAnalysis' }
  },
  {
    path: '/dirty-data-monitor',
    name: 'dirtyDataMonitor',
    component: () => import('../views/power/DirtyDataMonitor.vue'),
    meta: { requiresAuth: true, title: '脏数据管理与监控', icon: 'Aim' }
  },
  {
    path: '/data-timeliness',
    name: 'dataTimeliness',
    component: () => import('../views/resource/DataTimelinessMonitor.vue'),
    meta: { requiresAuth: true, title: '数据及时性监测', icon: 'Timer' }
  },
  {
    path: '/data-aggregation',
    name: 'dataAggregation',
    component: () => import('../views/resource/DataAggregationView.vue'),
    meta: { requiresAuth: true, title: '数据汇聚界面', icon: 'Connection' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

const clearLocalAuth = () => {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('token_expires_at')
  sessionStorage.removeItem('last_activity_ts')
  localStorage.removeItem('token')
  sessionStorage.removeItem('user')
  localStorage.removeItem('user')
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
  markUserActivity()
  let token = getToken()
  const expiresAt = getTokenExpiresAt()

  // 从sessionStorage获取用户信息，如果没有再从localStorage获取
  let userInfo = {};
  try {
    userInfo = JSON.parse(sessionStorage.getItem('user') || '{}');
    // 清理旧版本localStorage残留
    if (localStorage.getItem('user')) {
      localStorage.removeItem('user')
    }
  } catch (e) {
    console.error('解析用户信息出错:', e);
    userInfo = {};
  }

  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  // 会话超时
  if (requiresAuth && hasSessionTimedOut()) {
    console.warn('路由守卫: 会话超时，执行登出');
    clearLocalAuth()
    next({ name: 'login' });
    return;
  }

  // 检查token过期
  const now = Date.now()
  if (token && expiresAt && now >= expiresAt) {
    console.warn('路由守卫: token已过期，重定向到登录页');
    clearLocalAuth()
    next({ name: 'login' });
    return;
  }

  // 令牌临近过期时尝试刷新
  if (token && expiresAt && expiresAt - now < REFRESH_THRESHOLD_MS) {
    try {
      const refreshed = await refreshToken()
      if (refreshed && refreshed.token) {
        token = refreshed.token
        sessionStorage.setItem('token', refreshed.token)
        const newExpires = refreshed.expiresAt || (refreshed.expiresIn ? Date.now() + Number(refreshed.expiresIn) * 1000 : 0)
        if (newExpires) {
          sessionStorage.setItem('token_expires_at', String(newExpires))
        }
      }
    } catch (e) {
      console.warn('路由守卫: 刷新令牌失败，重定向到登录页', e?.message || e)
      clearLocalAuth()
      next({ name: 'login' })
      return
    }
  }

  // 将信息记录到控制台（非生产环境）
  if (process.env.NODE_ENV !== 'production') {
    console.log('路由守卫: 检查导航', {
      to: to.path,
      from: from.path,
      hasToken: !!token,
      hasUserInfo: !!Object.keys(userInfo).length,
      requiresAuth,
      hasPermission: !!to.meta.permission,
      permission: to.meta.permission
    });
  }

  // 特别记录 /switch-list 路由的导航
  if (to.path === '/switch-list') {
    console.log('路由守卫: 导航到交换机管理页面', {
      from: from.path,
      to: to.path,
      query: to.query,
      requiresAuth,
      hasPermission: !!to.meta.permission
    });
  }

  // 需要登录但没有token
  if (requiresAuth && !token) {
    console.warn('路由守卫: 需要登录权限，但未找到token，重定向到登录页');
    next({ name: 'login' });
    return;
  }

  // 已登录用户访问登录页
  if (to.matched.some(record => record.meta.guest) && token) {
    console.log('路由守卫: 已登录用户访问登录页，重定向到综合监控');
    next({ name: 'dashboard' });
    return;
  }

  // 权限检查
  if (to.meta.permission) {
    const userPermissions = userInfo.permissions || [];
    const userRoles = userInfo.roles || [];

    // 如果是管理员角色，直接放行
    if (userRoles.some(role => {
      return typeof role === 'object' ? role.name === 'ADMIN' : role === 'ADMIN';
    })) {
      next();
      return;
    }

    // 检查是否有所需权限
    if (!userPermissions.includes(to.meta.permission)) {
      console.warn('路由守卫: 权限不足:', to.meta.permission);
      next({ name: 'dashboard' });
      return;
    }
  }

  // 其他情况放行
  next();
});

export default router
