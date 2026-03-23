

## 项目描述
前端的数据大屏是通过iframe调用的，需要另外打包发布，文件夹名称为tk，需要在命令行中使用以下命令：
npm run build
npm install -g http-server
cd dist
http-server -p 3000
注：需要在主系统的DashboardView.vue(login-oauth2\vue\src\views)文件中对dashboardUrl的ip参数进行修改。




