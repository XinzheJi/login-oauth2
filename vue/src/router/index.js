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
import TabsTestView from '../views/TabsTestView.vue'
import PageStatusView from '../views/PageStatusView.vue'
import TabNavigationTestView from '../views/TabNavigationTestView.vue'
import SimpleTabTestView from '../views/SimpleTabTestView.vue'
import RouteTestView from '../views/RouteTestView.vue'
import NavigationTestView from '../views/NavigationTestView.vue'
const routes = [
  {
    path: '/',
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
  {    path: '/tenants',    name: 'tenants',    component: TenantManageView,    meta: { requiresAuth: true, permission: 'tenant:view' }  },
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
    path: '/tabs-test',
    name: 'tabsTest',
    component: TabsTestView,
    meta: { requiresAuth: true, title: '标签页测试', icon: 'el-icon-s-platform' }
  },
  {
    path: '/page-status',
    name: 'pageStatus',
    component: PageStatusView,
    meta: { requiresAuth: true, title: '页面状态检查', icon: 'el-icon-s-platform' }
  },
  {
    path: '/tab-navigation-test',
    name: 'tabNavigationTest',
    component: TabNavigationTestView,
    meta: { requiresAuth: true, title: '标签页跳转测试', icon: 'el-icon-s-platform' }
  },
  {
    path: '/simple-tab-test',
    name: 'simpleTabTest',
    component: SimpleTabTestView,
    meta: { requiresAuth: true, title: '简单标签测试', icon: 'el-icon-s-platform' }
  },
  {
    path: '/route-test',
    name: 'routeTest',
    component: RouteTestView,
    meta: { requiresAuth: true, title: '路由测试', icon: 'el-icon-s-platform' }
  },
  {
    path: '/navigation-test',
    name: 'navigationTest',
    component: NavigationTestView,
    meta: { requiresAuth: true, title: '导航布局测试', icon: 'el-icon-s-platform' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 优先从sessionStorage获取token
  const token = sessionStorage.getItem('token') || localStorage.getItem('token');
  
  // 从sessionStorage获取用户信息，如果没有再从localStorage获取
  let userInfo = {};
  try {
    userInfo = JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || '{}');
  } catch (e) {
    console.error('解析用户信息出错:', e);
    userInfo = {};
  }
  
  // 将信息记录到控制台（非生产环境）
  if (process.env.NODE_ENV !== 'production') {
    console.log('路由守卫: 检查导航', {
      to: to.path,
      from: from.path,
      hasToken: !!token,
      hasUserInfo: !!Object.keys(userInfo).length,
      requiresAuth: to.matched.some(record => record.meta.requiresAuth)
    });
  }
  
  // 需要登录但没有token
  if (to.matched.some(record => record.meta.requiresAuth) && !token) {
    console.warn('路由守卫: 需要登录权限，但未找到token，重定向到登录页');
    next({ name: 'login' });
    return;
  } 
  
  // 已登录用户访问登录页
  if (to.matched.some(record => record.meta.guest) && token) {
    console.log('路由守卫: 已登录用户访问登录页，重定向到首页');
    next({ name: 'home' });
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
      next({ name: 'home' });
      return;
    }
  }
  
  // 其他情况放行
  next();
});

export default router
