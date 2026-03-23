<template>
  <div class="carrier-module-ledger-container">
    <el-card class="box-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="MAC地址">
            <el-input v-model="searchForm.mac" placeholder="请输入MAC地址" clearable />
          </el-form-item>
          <el-form-item label="所属台区">
            <el-input v-model="searchForm.area" placeholder="请输入台区名称" clearable />
          </el-form-item>
          <el-form-item label="厂家">
            <el-select v-model="searchForm.manufacturer" placeholder="请选择厂家" clearable>
              <el-option label="华为海思" value="hisilicon" />
              <el-option label="智芯微" value="zhixin" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
            <el-button icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Action Bar -->
      <div class="action-bar">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增模块</el-button>
        <el-button type="success" icon="Upload" @click="handleImport">批量导入</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="mac" label="设备MAC" width="180" />
        <el-table-column prop="model" label="模块型号" width="150" />
        <el-table-column prop="manufacturer" label="厂家" width="120" />
        <el-table-column prop="version" label="软件版本" width="120" />
        <el-table-column prop="associatedDevice" label="关联电表/终端" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '在线' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleConfig(scope.row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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
import { Search, Refresh, Plus, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'CarrierModuleLedgerView',
  data() {
    return {
      searchForm: {
        mac: '',
        area: '',
        manufacturer: ''
      },
      loading: false,
      tableData: [],
      allTableData: [
        {
          mac: 'AA:BB:CC:00:01',
          model: 'HPLC-RF-01',
          manufacturer: '华为海思',
          version: 'V2.0.1',
          associatedDevice: '集中器_#1',
          status: '在线'
        },
        {
          mac: 'AA:BB:CC:00:02',
          model: 'HPLC-RF-01',
          manufacturer: '智芯微',
          version: 'V2.0.1',
          associatedDevice: '表箱_#5',
          status: '离线'
        },
        {
          mac: 'AA:BB:CC:11:03',
          model: 'HPLC-RF-02',
          manufacturer: '华为海思',
          version: 'V2.1.0',
          associatedDevice: '智能电表_#120',
          status: '在线'
        },
        {
          mac: 'AA:BB:CC:22:04',
          model: 'HPLC-RF-01',
          manufacturer: '智芯微',
          version: 'V2.0.1',
          associatedDevice: '表箱_#8',
          status: '在线'
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
      // Simulate API delay
      setTimeout(() => {
        const { mac, area, manufacturer } = this.searchForm;
        this.tableData = this.allTableData.filter(item => {
          const matchMac = !mac || item.mac.toLowerCase().includes(mac.toLowerCase());
          const matchArea = !area || (item.associatedDevice && item.associatedDevice.includes(area));
          // For manufacturer, the value is 'hisilicon' or 'zhixin', but data is '华为海思' or '智芯微'
          // We need to map or check against the labels if the value passed is the value.
          // looking at the template: <el-option label="华为海思" value="hisilicon" />
          // So this.searchForm.manufacturer will be 'hisilicon'. Data is '华为海思'.
          let matchManufacturer = true;
          if (manufacturer) {
             const map = { 'hisilicon': '华为海思', 'zhixin': '智芯微' };
             matchManufacturer = item.manufacturer === map[manufacturer];
          }
          
          return matchMac && matchArea && matchManufacturer;
        });
        this.loading = false;
        ElMessage.success('查询成功');
      }, 300);
    },
    resetSearch() {
      this.searchForm = {
        mac: '',
        area: '',
        manufacturer: ''
      };
      this.tableData = [...this.allTableData];
    },
    handleAdd() {
      ElMessage.info('新增模块功能待开发');
    },
    handleImport() {
      ElMessage.info('批量导入功能待开发');
    },
    handleConfig(row) {
      ElMessage.info(`配置设备: ${row.mac}`);
    },
    handlePageChange(val) {
      console.log(`Current page: ${val}`);
    }
  }
}
</script>

<style scoped>
.carrier-module-ledger-container {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
}
.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
