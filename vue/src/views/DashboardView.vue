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
      dashboardUrl: 'http://172.30.154.32:3000'
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
