<template>
  <div class="power-device-manage-container">
    <h2 class="page-title" style="font-weight: normal;">设备型号管理</h2>

    <!-- 主操作区 -->
    <div class="main-operator-block">
      <!-- 左侧操作按钮 -->
      <div class="left-button">
        <!-- 可添加新增型号按钮 -->
      </div>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <!-- 第一行：搜索组件 -->
        <div class="search-row" style="justify-content: flex-end;">
          <el-select
            v-model="isOpticalBypass"
            class="search-item"
            placeholder="光旁路标志"
            clearable
          >
            <el-option label="全部" :value="null" />
            <el-option label="是" value="1" />
            <el-option label="否" value="0" />
          </el-select>
        </div>
        
        <!-- 第二行：查询和重置按钮 -->
        <div class="button-row">
          <div class="query-button-group">
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 设备列表 -->
    <el-table
      :data="tableData"
      border
      stripe
      :header-cell-style="{background:'#f8f8f9',color:'rgb(80 78 78)'}"
      style="width: 100%"
      @sort-change="sortChangeHandler"
    >
      <el-table-column type="index" label="序号" align="center" width="60" :index="indexMethod" />
      <el-table-column prop="name" label="型号名称" align="center" />
      <el-table-column prop="alias" label="别名" align="center" />
      <el-table-column prop="isOpticalBypass" label="光旁路标志" align="center" width="120px" sortable="custom">
        <template #default="scope">
          <span :style="{color: getOpticalBypassColor(scope.row.isOpticalBypass)}">
            {{ scope.row.isOpticalBypass == 1 || scope.row.isOpticalBypass === '1' ? '是' : '否' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="型号分类" align="center" />
      <el-table-column prop="manufacturerId" label="所属厂商" align="center" />
      <el-table-column prop="snmpKey" label="型号SNMP KEY" align="center" />
      <el-table-column prop="status" label="状态" align="center" width="120px" sortable="custom">
        <template #default="scope">
          <span :style="{color: getStatusColor(scope.row.status)}">
            {{ scope.row.status == 1 ? '启用' : '禁用' }}
          </span>
        </template>
      </el-table-column>
      <!-- 可根据实际需求继续补充字段 -->
    </el-table>
    
    <!-- 分页器 -->
    <div class="paging-operation">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { Search, RefreshLeft, Check, Close } from '@element-plus/icons-vue'
  import { fetchModelTypeList } from '@/api/modelType'
  const tableData = ref([])
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const isOpticalBypass = ref(null)
  const dateRange = ref([])
  
  const fetchData = () => {
    fetchModelTypeList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      isOpticalBypass: isOpticalBypass.value
    }).then(res => {
      if (res) {
        tableData.value = res.list
        total.value = res.total
        pageSize.value = res.pageSize
        pageNum.value = res.pageNum
      } else {
        ElMessage.error(res.data.message || '获取数据失败')
      }
    }).catch(() => {
      ElMessage.error('接口请求失败')
    })
  }
  
  const handlePageChange = (val) => {
    pageNum.value = val
    fetchData()
  }
  
  const handleSizeChange = (val) => {
    pageSize.value = val
    fetchData()
  }
  
  const handleSearch = () => {
    pageNum.value = 1
    fetchData()
  }
  
  const resetQuery = () => {
    isOpticalBypass.value = null
    dateRange.value = []
    pageNum.value = 1
    fetchData()
  }
  
  const indexMethod = (index) => {
    return (pageNum.value - 1) * pageSize.value + index + 1
  }
  
  const getOpticalBypassColor = (value) => {
    return (value == 1 || value === '1') ? '#67C23A' : '#909399'
  }
  
  const getStatusColor = (value) => {
    return value == 1 ? '#67C23A' : '#909399'
  }
  
  const sortChangeHandler = (sort) => {
    if (sort.prop) {
      tableData.value.sort((a, b) => {
        if (sort.order === 'ascending') {
          return a[sort.prop] > b[sort.prop] ? 1 : -1
        } else {
          return a[sort.prop] < b[sort.prop] ? 1 : -1
        }
      })
    }
  }
  
  onMounted(() => {
    fetchData()
  })
  </script>

  <style scoped>
  .power-device-manage-container {
    padding: 20px;
  }
  
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 15px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .main-operator-block {
    margin-top: 10px;
    margin-bottom: 10px;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 10px;
  }
  
  .left-button {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    margin-top: -0px;
  }
  
  .left-button .el-button {
    margin: 0;
  }
  
  .search-area {
    margin-top: 0;
    flex: 1;
    max-width: 780px;
  }
  
  .search-row {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
    align-items: center;
  }
  
  .search-item {
    width: 190px;
  }
  
  .button-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }
  
  .query-button-group {
    display: flex;
    gap: 5px;
  }
  
  .query-button-group .el-button {
    margin: 0;
  }
  
  .paging-operation {
    padding: 10px;
    width: 100%;
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .search-item .el-input,
  .search-item .el-select {
    width: 100%;
  }
  
  @media (max-width: 768px) {
    .search-item {
      margin-bottom: 10px;
    }
    
    .search-item .el-input,
    .search-item .el-select {
      width: 100%;
    }
    
    .query-button-group {
      width: 100%;
      justify-content: flex-end;
      margin-top: 10px;
    }
  }
  </style>