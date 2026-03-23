<template>
  <div class="dashboard-view">
    <DashboardIframe :url="dashboardUrl" />
  </div>
</template>

<script>
import DashboardIframe from '@/components/common/DashboardIframe.vue'

export default {
  name: 'DashboardView',
  components: {
    DashboardIframe
  },
  data() {
    return {
      // 默认走同源 /screen/（推荐 nginx 反代）；需要跨域时用 VUE_APP_DASHBOARD_URL 覆盖（编译期变量）
      dashboardUrl: process.env.VUE_APP_DASHBOARD_URL || '/screen/'
    }
  },
  mounted() {
    // 设置页面标题
    document.title = '数据大屏 - 系统管理'
  }
}
</script>

<style scoped>
.dashboard-view {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
</style>
