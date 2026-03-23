# 数据大屏构建和运行指南

## 构建步骤

### 1. 进入 tk 目录
```bash
cd tk
```

### 2. 安装依赖（如果还没有安装）
```bash
npm install
```

### 3. 构建项目
```bash
npm run build
```

构建完成后，会在 `tk/dist` 目录下生成静态文件。

## 运行步骤

### 方式一：使用 http-server（推荐用于生产环境）

1. 确保已全局安装 http-server（如果没有，先安装）：
```bash
npm install -g http-server
```

2. 进入 dist 目录并启动服务：
```bash
cd dist
http-server -p 3000
```

或者从 tk 目录直接运行：
```bash
cd tk
http-server dist -p 3000
```

### 方式二：使用开发服务器（用于开发调试）

```bash
cd tk
npm run serve
```

开发服务器通常运行在 `http://localhost:8080`（端口可能不同，查看终端输出）

## 注意事项

1. **每次修改代码后都需要重新构建**：
   - 修改了 `src` 目录下的任何文件
   - 需要运行 `npm run build` 重新构建
   - 然后重启 http-server

2. **构建输出目录**：
   - 默认输出到 `tk/dist` 目录
   - 可以通过 `vue.config.js` 中的 `outputDir` 配置修改

3. **主系统配置**：
   - 确保 `vue/src/views/DashboardView.vue` 中的 `dashboardUrl` 指向正确的地址
   - 例如：`http://192.168.56.1:3000`（根据实际 IP 和端口调整）

## 快速命令

```bash
# 一键构建并运行（在 tk 目录下）
npm run build && cd dist && http-server -p 3000
```

