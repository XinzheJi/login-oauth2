import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import axios from 'axios'
import { setupPermission } from './directives/permission'

// 引入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { markUserActivity } from './api/auth'

// 引入公共组件
import CommonComponents from './components/common'
import { AppLayout, AppHeader, AppSidebar } from './components/layout'

// 注册全局会话活跃度监听
const activityEvents = ['click', 'keydown', 'mousemove', 'scroll', 'touchstart']
activityEvents.forEach(event => {
  window.addEventListener(event, () => markUserActivity(), { passive: true })
})
markUserActivity()

// 全局挂载axios
const app = createApp(App)
app.config.globalProperties.$http = axios

// 注册权限指令
setupPermission(app)

// 注册全局组件
app.component('AppLayout', AppLayout)
app.component('AppHeader', AppHeader)
app.component('AppSidebar', AppSidebar)
app.use(CommonComponents)

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})
app.mount('#app')
