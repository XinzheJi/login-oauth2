const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': { // Ensure your backend API base path matches this
        target: 'http://localhost:8080', // Your backend server
        changeOrigin: true,
        pathRewrite: {
          '^/api': '' // Optional: rewrite /api to /
        }
      }
    }
  },
  chainWebpack: config => {
    config.plugin('define').tap(definitions => {
      Object.assign(definitions[0], {
        __VUE_OPTIONS_API__: true, // 通常为 true 以支持选项式 API
        __VUE_PROD_DEVTOOLS__: false, // 生产环境禁用 devtools
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false // 禁用生产环境水合不匹配的详细信息
      });
      return definitions;
    });
  }
}) 