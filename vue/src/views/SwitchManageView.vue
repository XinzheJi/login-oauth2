<template>
    <div>
      <el-card>
        <template #header>
          <span>交换机管理</span>
        </template>
        <div style="margin-bottom: 16px;">
          <el-select v-model="isOpticalBypass" placeholder="光旁路筛选" clearable style="width: 180px; margin-right: 12px;">
            <el-option label="全部" :value="null" />
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </div>
        <el-table
          :data="tableData"
          border
          style="width: 100%">
          <el-table-column
            type="index"
            label="序号"
            width="80"
            :index="indexMethod" />
          <el-table-column prop="name" label="设备名称">
            <template #default="scope">
              <el-button 
                type="primary" 
                link 
                @click="goToDeviceAgingFault(scope.row)"
                style="padding: 0; font-size: 14px;"
              >
                {{ scope.row.name }}
              </el-button>
            </template>
          </el-table-column>
          <el-table-column prop="modelFullName" label="设备型号"/>
          <el-table-column prop="manufacturer" label="厂商"/>
          <el-table-column prop="isOpticalBypass" label="光旁路">
            <template #default="scope">
              <el-tag v-if="scope.row.isOpticalBypass == 1" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址"/>
          <el-table-column prop="operatingState" label="投运状态">
            <template #default="scope">
              <el-tag v-if="scope.row.operatingState == 1" type="success">已投运</el-tag>
              <el-tag v-else-if="scope.row.operatingState == 0" type="info">未投运</el-tag>
              <el-tag v-else-if="scope.row.operatingState == 2" type="danger">已退运</el-tag> 
              <el-tag v-else-if="scope.row.operatingState == 3" type="warning">临时停运</el-tag>
              <el-tag v-else type="default">未知</el-tag>
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
  import { useRouter } from 'vue-router'
  import { fetchSwitchList } from '@/api/switchDevice'
  
  const router = useRouter()
  const tableData = ref([])
  const pageNum = ref(1)
  const pageSize = ref(10)
  const total = ref(0)
  const isOpticalBypass = ref(null)
  
  const fetchData = () => {
    fetchSwitchList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      isOpticalBypass: isOpticalBypass.value
    }).then(res => {
        console.log("打印res"+res);
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
  
  const indexMethod = (index) => {
    return (pageNum.value - 1) * pageSize.value + index + 1
  }
  
  // 跳转到设备老化与故障感知页面
  const goToDeviceAgingFault = (device) => {
    // 生成设备ID（基于设备名称和型号）
    const deviceId = `${device.name}_${device.modelFullName}`.replace(/[^a-zA-Z0-9_]/g, '_')
    
    // 跳转到设备老化与故障感知页面，传递设备信息作为查询参数
    router.push({
      name: 'deviceAgingFault',
      query: { 
        deviceId: deviceId,
        deviceName: device.name,
        modelName: device.modelFullName,
        manufacturer: device.manufacturer,
        ip: device.ip
      }
    })
  }
  
  onMounted(() => {
    fetchData()
  })
  </script>