<template>
  <div id="app">
    <!-- 登录页面独立显示，不使用布局组件 -->
    <router-view v-if="$route.meta.guest" />
    
    <!-- 非登录页面使用布局组件 -->
    <app-layout v-else>
      <router-view />
    </app-layout>
  </div>
</template>

<script>
import AppLayout from '@/components/layout'

export default {
  name: 'App',
  components: {
    AppLayout
  }
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

#app {
  height: 100vh;
}
</style>
