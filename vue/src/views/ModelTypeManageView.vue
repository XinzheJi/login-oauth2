<template>
    <div>
      <el-card>
        <template #header>
          <span>设备型号管理</span>
        </template>
        <div style="margin-bottom: 16px;">

          <el-select
            v-model="isOpticalBypass"
            placeholder="光旁路标志"
            clearable
            style="width: 150px; margin-right: 12px;"
          >
            <el-option label="全部" :value="null" />
            <el-option label="是" value="1" />
            <el-option label="否" value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </div>
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column type="index" label="序号" width="60" :index="indexMethod" />
          <el-table-column prop="name" label="型号名称" />
          <el-table-column prop="alias" label="别名" />
          <el-table-column prop="isOpticalBypass" label="光旁路标志">
            <template #default="scope">
              <el-tag v-if="scope.row.isOpticalBypass == 1 || scope.row.isOpticalBypass === '1'" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="型号分类" />
          <el-table-column prop="manufacturerId" label="所属厂商" />
          <el-table-column prop="snmpKey" label="型号SNMP KEY" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag v-if="scope.row.status == 1" type="success">启用</el-tag>
              <el-tag v-else type="info">禁用</el-tag>
            </template>
          </el-table-column>
          <!-- 可根据实际需求继续补充字段 -->
        </el-table>
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNum"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
          style="margin-top: 20px; text-align: right;"
        />
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { fetchModelTypeList } from '@/api/modelType'
  const tableData = ref([])
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const isOpticalBypass = ref(null)
  
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
  
  const indexMethod = (index) => {
    return (pageNum.value - 1) * pageSize.value + index + 1
  }
  
  onMounted(() => {
    fetchData()
  })
  </script>