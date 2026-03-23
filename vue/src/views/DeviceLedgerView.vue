<template>
  <div class="device-ledger-container">
    <el-card class="box-card">
      <!-- Top Search & Action Bar -->
      <div class="search-action-bar">
        <div class="search-area">
          <el-input
            v-model="searchQuery.name"
            placeholder="装置名称"
            clearable
            class="search-item"
            style="width: 200px;"
          />
          <el-input
            v-model="searchQuery.location"
            placeholder="安装位置"
            clearable
            class="search-item"
            style="width: 200px;"
          />
          <el-input
            v-model="searchQuery.ip"
            placeholder="IP地址"
            clearable
            class="search-item"
            style="width: 200px;"
          />
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
          <el-button icon="Refresh" @click="resetSearch">重置</el-button>
        </div>
        <div class="action-area">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增装置</el-button>
          <el-button type="success" icon="Upload" @click="handleImport">批量导入</el-button>
          <el-button type="warning" icon="Download" @click="handleExport">导出</el-button>
        </div>
      </div>

      <!-- Main Table -->
      <el-table :data="tableData" style="width: 100%" border stripe v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="装置名称" min-width="150" />
        <el-table-column prop="location" label="安装位置" min-width="150" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="commMode" label="当前通信模式" width="140" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.commMode === 'fiber' ? 'success' : 'warning'" effect="dark">
              {{ scope.row.commMode === 'fiber' ? '光纤主用' : '4G备用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="设备状态" width="100" align="center">
           <template #default="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'" effect="plain">
              {{ scope.row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最近心跳时间" width="180" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination (Mock) -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { Search, Refresh, Plus, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'DeviceLedgerView',
  components: {
    // Icons need to be registered or imported can be used if setup script or global registration.
    // Assuming global registration or auto-import for icons in this project context usually, 
    // but importing them to be safe if using options API without global setup.
    // However, icons in templates like icon="Search" work if registered globally.
    // Let's rely on standard Element Plus usage.
  },
  data() {
    return {
      searchQuery: {
        name: '',
        location: '',
        ip: ''
      },
      loading: false,
      tableData: [],
      allTableData: [
        {
          id: 1,
          name: '路由装置_#01',
          location: '10kV配电房A',
          ip: '192.168.1.10',
          commMode: 'fiber',
          status: 'online',
          lastHeartbeat: '2023-12-16 10:00:00'
        },
        {
          id: 2,
          name: '路由装置_#02',
          location: '户外柜B区',
          ip: '192.168.1.11',
          commMode: '4g',
          status: 'online',
          lastHeartbeat: '2023-12-16 10:00:01'
        },
        {
          id: 3,
          name: '路由装置_#03',
          location: '监控中心机房',
          ip: '192.168.1.12',
          commMode: 'fiber',
          status: 'online',
          lastHeartbeat: '2023-12-16 10:05:22'
        },
        {
          id: 4,
          name: '路由装置_#04',
          location: '3号楼地下室',
          ip: '192.168.1.13',
          commMode: 'fiber',
          status: 'offline',
          lastHeartbeat: '2023-12-15 18:30:00'
        }
      ],
      total: 4,
      pageSize: 10
    }
  },
  created() {
      this.tableData = [...this.allTableData];
  },
  methods: {
    handleSearch() {
      this.loading = true;
      // Simulate API call
      setTimeout(() => {
        const { name, location, ip } = this.searchQuery;
        this.tableData = this.allTableData.filter(item => {
            const matchName = !name || item.name.toLowerCase().includes(name.toLowerCase());
            const matchLoc = !location || item.location.includes(location);
            const matchIp = !ip || item.ip.includes(ip);
            return matchName && matchLoc && matchIp;
        });
        this.loading = false;
        ElMessage.success('查询成功');
      }, 500);
    },
    resetSearch() {
      this.searchQuery = {
        name: '',
        location: '',
        ip: ''
      };
      this.tableData = [...this.allTableData];
      // handleSearch will re-filter based on empty which works, but setting it directly is faster for reset and consistent with others
    },
    handleAdd() {
      ElMessage.info('点击了新增装置');
    },
    handleImport() {
      ElMessage.info('点击了批量导入');
    },
    handleExport() {
      ElMessage.success('正在导出数据...');
    },
    handleDetail(row) {
      ElMessage.info(`查看详情: ${row.name}`);
    },
    handleEdit(row) {
      ElMessage.info(`编辑装置: ${row.name}`);
    },
    handlePageChange(val) {
      console.log(`当前页: ${val}`);
    }
  }
}
</script>

<style scoped>
.device-ledger-container {
  padding: 20px;
}

.search-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.search-area {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-item {
  width: 200px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
