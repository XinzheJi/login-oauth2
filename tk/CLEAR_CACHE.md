# 清除缓存和重新构建指南

## 问题
如果仍然看到 SecurityError，可能是浏览器缓存了旧的 JavaScript 文件。

## 解决步骤

### 1. 清除浏览器缓存
- **Chrome/Edge**: 按 `Ctrl + Shift + Delete`，选择"缓存的图片和文件"，清除
- 或者按 `Ctrl + F5` 强制刷新页面
- 或者在开发者工具中右键刷新按钮，选择"清空缓存并硬性重新加载"

### 2. 重新构建项目
```bash
cd tk
npm run build
```

### 3. 确保使用新的构建文件
- 停止旧的 http-server
- 重新启动：
```bash
cd dist
http-server -p 3000
```

### 4. 验证
- 打开浏览器开发者工具
- 查看 Network 标签
- 确认加载的 JavaScript 文件是最新的（检查文件修改时间）
- 或者查看 Sources 标签，确认代码中不再有 `window.parent.handleDashboardJump` 的直接访问

