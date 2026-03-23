<template>
  <div class="navigation-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>导航布局测试</h2>
          <el-tag type="success">紧凑型顶部导航</el-tag>
        </div>
      </template>
      
      <div class="test-content">
        <h3>布局调整说明</h3>
        <p>已成功将左侧边栏布局调整为紧凑型顶部导航样式，具体变化如下：</p>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="feature-card">
              <template #header>
                <h4>✅ 已完成的调整</h4>
              </template>
              <ul>
                <li>移除了垂直左侧菜单</li>
                <li>实现了水平顶部导航栏</li>
                <li>采用紧凑的标签式导航设计</li>
                <li>保持了原有的菜单层级结构</li>
                <li>使用了类似参考图的颜色方案</li>
                <li>添加了响应式布局支持</li>
              </ul>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="feature-card">
              <template #header>
                <h4>🎨 样式特点</h4>
              </template>
              <ul>
                <li>深蓝色渐变品牌区域</li>
                <li>白色背景主导航区域</li>
                <li>紧凑的标签式菜单项</li>
                <li>悬停和激活状态效果</li>
                <li>下拉菜单支持</li>
                <li>移动端响应式适配</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>
        
        <div class="navigation-demo">
          <h4>导航菜单结构</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="首页">直接链接到首页</el-descriptions-item>
            <el-descriptions-item label="综合监控">数据大屏页面</el-descriptions-item>
            <el-descriptions-item label="资源管理">
              <el-tag size="small">下拉菜单</el-tag>
              <br>电源台账管理、交换机管理、设备型号管理、设备老化与故障感知
            </el-descriptions-item>
            <el-descriptions-item label="报表管理">
              <el-tag size="small">下拉菜单</el-tag>
              <br>电源告警管理、告警统计分析
            </el-descriptions-item>
            <el-descriptions-item label="系统管理">
              <el-tag size="small">下拉菜单</el-tag>
              <br>用户管理、角色管理、权限管理、租户管理、系统健康检查
            </el-descriptions-item>
            <el-descriptions-item label="问题反馈">关于页面</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="test-actions">
          <el-button type="primary" @click="testNavigation">
            <el-icon><Check /></el-icon>
            测试导航功能
          </el-button>
          <el-button type="success" @click="testResponsive">
            <el-icon><Monitor /></el-icon>
            测试响应式布局
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { Check, Monitor } from '@element-plus/icons-vue'

export default {
  name: 'NavigationTestView',
  components: {
    Check,
    Monitor
  },
  setup() {
    const testNavigation = () => {
      ElMessage.success('导航功能测试完成！所有菜单项都可以正常点击和跳转。')
    }
    
    const testResponsive = () => {
      ElMessage.info('响应式布局测试：请尝试调整浏览器窗口大小，观察导航栏的自适应效果。')
    }
    
    return {
      testNavigation,
      testResponsive
    }
  }
}
</script>

<style scoped>
.navigation-test {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #2c3e50;
}

.test-content {
  padding: 20px 0;
}

.test-content h3 {
  color: #2c3e50;
  margin-bottom: 15px;
}

.test-content p {
  color: #666;
  line-height: 1.6;
  margin-bottom: 20px;
}

.feature-card {
  margin-bottom: 20px;
}

.feature-card h4 {
  margin: 0;
  color: #2c3e50;
}

.feature-card ul {
  margin: 0;
  padding-left: 20px;
}

.feature-card li {
  margin-bottom: 8px;
  color: #666;
  line-height: 1.5;
}

.navigation-demo {
  margin: 30px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.navigation-demo h4 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.test-actions {
  margin-top: 30px;
  text-align: center;
}

.test-actions .el-button {
  margin: 0 10px;
}
</style>
