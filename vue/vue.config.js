const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 开发环境API代理配置
  devServer: {
    host: 'localhost', // 使用localhost避免IP地址解析问题
    port: 8082, // 明确指定前端开发服务器端口
    https: false, // 确保使用HTTP而不是HTTPS
    open: false, // 启动时不自动打开浏览器
    proxy: {
      '/api': {
        target: 'http://localhost:8081', // 后端API地址
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api' // 保留/api前缀
        }
      }
    },
    // WebSocket配置 - 明确指定WebSocket连接地址
    client: {
      webSocketURL: 'ws://localhost:8082/ws'
    },
    // 允许所有主机访问
    allowedHosts: 'all'
  }
})
