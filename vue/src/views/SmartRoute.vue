<template>
  <div class="smart-route-container">
    <el-card class="status-card" :class="{ 'warning-bg': isBackupChannel }">
      <template #header>
        <div class="card-header">
          <span>通信通道状态</span>
        </div>
      </template>
      <div class="status-content">
        <el-icon class="status-icon"><Connection /></el-icon>
        <span class="status-text">{{ currentChannel }}</span>
      </div>
    </el-card>

    <div class="main-content">
      <div class="operation-bar">
        <el-button 
          type="danger" 
          @click="simulateInterruption" 
          :loading="loading" 
          :disabled="isBackupChannel"
        >
          {{ isBackupChannel ? '当前为 4G 备用通道' : '模拟光纤中断' }}
        </el-button>
        <el-button 
          v-if="isBackupChannel" 
          type="success" 
          @click="resetChannel" 
          :loading="loading"
        >
          恢复光纤通信
        </el-button>
      </div>

      <el-table :data="deviceList" style="width: 100%" border v-loading="loading">
        <el-table-column prop="name" label="设备名称" width="180" />
        <el-table-column prop="location" label="安装位置" width="180" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="status" label="在线状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在线' ? 'success' : 'danger'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="当前通道">
             <template #default="scope">
                <el-tag :type="scope.row.channel === '光纤' ? '' : 'warning'">{{ scope.row.channel }}</el-tag>
             </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
// Element Plus Icons
import { Connection } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'SmartRoute',
  components: { Connection },
  data() {
    return {
      currentChannel: '光纤通信-正常',
      isBackupChannel: false,
      loading: false,
      deviceList: [
        { name: '智能网关-01', location: '1号楼配电室', ip: '192.168.1.101', status: '在线', channel: '光纤' },
        { name: '智能网关-02', location: '2号楼机房', ip: '192.168.1.102', status: '在线', channel: '光纤' },
        { name: '监控摄像头-A', location: '园区北门', ip: '192.168.1.201', status: '在线', channel: '光纤' },
        { name: '环境传感器-05', location: '3号仓库', ip: '192.168.1.155', status: '在线', channel: '光纤' },
        { name: '门禁控制器-B', location: '办公楼大厅', ip: '192.168.1.188', status: '在线', channel: '光纤' }
      ]
    }
  },
  methods: {
    simulateInterruption() {
      this.loading = true;
      
      setTimeout(() => {
        this.loading = false;
        
        // 1. Error Message
        ElMessage.error('警告：监测到主用光纤中断！');
        
        // 2. Change Status
        this.currentChannel = '4G 通道（备用）';
        this.isBackupChannel = true;
        
        // Update table data
        this.deviceList.forEach(device => {
            device.channel = '4G';
        });

        // 3. Success Message
        setTimeout(() => {
           ElMessage.success('已自动切换至无线 4G 通道，通信恢复。');
        }, 500);
        
      }, 1000);
    },
    resetChannel() {
        this.loading = true;
        setTimeout(() => {
            this.loading = false;
            this.currentChannel = '光纤通信-正常';
            this.isBackupChannel = false;
             this.deviceList.forEach(device => {
                device.channel = '光纤';
            });
            ElMessage.success('光纤通信已恢复。');
        }, 1000);
    }
  }
}
</script>

<style scoped>
.smart-route-container {
  padding: 20px;
}
.status-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}
.warning-bg {
    background-color: #fdf6ec; 
    border-color: #faecd8;
}
.status-content {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  color: #67c23a; 
}
.warning-bg .status-content {
    color: #e6a23c;
}

.status-icon {
  margin-right: 10px;
}
.operation-bar {
  margin-bottom: 20px;
}
</style>
