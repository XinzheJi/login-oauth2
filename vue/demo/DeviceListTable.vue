<template>
  <div>
    <common-table
      ref="CommonTableRef"
      :loading="loading"
      :table-data="tableData"
      :pagination="filter"
      :search-overflow="8"
      @do-reset="doReset"
      @do-search="doSearch"
      @on-load="loadData"
      @sort-change="sortChangeHandler"
    >
      <template #operate-button>
        <template v-if="!onlyView">
          <el-button
            v-has-permission="['DeviceList:update']"
            type="primary"
            icon="el-icon-plus"
            @click="addDevice"
          >
            新增设备
          </el-button>
          <el-button @click="batchModifyDevice">批量修改</el-button>
          <el-button
            v-if="website.enableNetworkManagement"
            @click="batchCommandDevice"
          >批量命令下发
          </el-button>
          <el-button @click="gotoTemplate">
            设备属性模版
          </el-button>
          <el-button @click="goToImportData">
            导入数据
          </el-button>
          <el-button v-has-permission="['DeviceList:refreshDeviceGather']" @click="refreshDeviceGatherInfoHandler">
            同步采集信息
          </el-button>
          <el-button v-has-permission="['DeviceList:batchCheckStatus']" @click="batchCheckStatus">批量核对设备
          </el-button>
        </template>
      </template>
      <template #operate-search>
        <area-select
          v-model="filter.areaId"
          class="search-item tree-popup"
        />
        <ip-input v-model="filter.ip" class="search-item" placeholder="请输入管理地址" />
        <el-select
          v-model="filter.deviceRole"
          class="search-item"
          multiple
          collapse-tags
          placeholder="请选择设备性质"
          clearable
        >
          <el-option
            v-for="{dictKey,dictValue} in dict.device_role"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
        </el-select>
        <el-input v-model="filter.name" class="search-item" placeholder="请输入设备名称" clearable />
        <el-select
          v-model="filter.operatingState"
          collapse-tags
          multiple
          class="search-item"
          placeholder="请选择投运状态"
          clearable
        >
          <el-option
            v-for="{dictKey,dictValue} in dict.device_operation_state"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
        </el-select>
        <el-select v-model="filter.onlineStatus" class="search-item" placeholder="请选择设备状态" clearable>
          <el-option
            v-for="{dictKey,dictValue} in dict.online_status"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
        </el-select>
        <el-select v-model="filter.snmpStatus" class="search-item" placeholder="请选择SNMP状态" clearable>
          <el-option label="正常" value="1" />
          <el-option label="异常" value="0" />
        </el-select>
        <el-select v-model="filter.snmpVersion" class="search-item" placeholder="请选择SNMP版本" clearable>
          <el-option
            v-for="{dictKey,dictValue} in dict.snmp_version"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
        </el-select>
        <el-select
          v-model="filter.manufacturerId"
          class="search-item"
          placeholder="请选择厂商"
          filterable
          clearable
          @change="manufacturerIdChangeCallback"
        >
          <el-option v-for="item in manufacturerOptions" :key="item.id" :label="item.name" :value="item.id" />
        </el-select>
        <el-select
          v-model="filter.modelId"
          class="search-item"
          placeholder="请选择设备型号"
          filterable
          :disabled="_.isEmpty(filter.manufacturerId)"
          clearable
        >
          <el-option
            v-for="item in modelOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-model="filter.modelValide"
          collapse-tags
          class="search-item"
          placeholder="请选择型号匹配结果"
          clearable
        >
          <el-option label="型号匹配" value="正确" />
          <el-option label="型号不匹配" value="错误" />
        </el-select>
        <el-input v-model="filter.sysObjectId" class="search-item" placeholder="请输入设备型号KEY" clearable />
        <el-input v-model="filter.stationName" class="search-item" placeholder="请输入所属站点" clearable />
        <ip-input v-model="filter.maskBizIpAndMaskBit" class="search-item" placeholder="请输入接入业务地址" />
        <el-select
          v-model="filter.businessType"
          collapse-tags
          class="search-item"
          placeholder="请选接入业务"
          clearable
        >
          <el-option
            v-for="{dictKey,dictValue} in dict.business_type"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
          <el-option label="未接入" value="未接入" />
        </el-select>
        <el-select
          v-model="filter.asset"
          collapse-tags
          class="search-item"
          placeholder="请选择台账资产"
          clearable
        >
          <el-option
            v-for="{dictKey,dictValue} in dict.asset_owner"
            :key="dictKey"
            :value="dictKey"
            :label="dictValue"
          />
        </el-select>
        <el-input v-model="filter.sourceId" class="search-item" placeholder="请输入GISID" clearable />
        <i-date-range-picker
          v-model="filter.date"
          class="search-item"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择创建日期"
        />
        <el-select
          v-model="filter.sshTelnet"
          collapse-tags
          class="search-item"
          placeholder="请选择远程登录是否成功"
          clearable
        >
          <el-option label="能远程登录" value="1" />
          <el-option label="不能远程登录" value="0" />
        </el-select>
      </template>
      <template #operate-button-right>
        <el-button :loading="loadingExport" type="success" @click="handleExport">导出</el-button>
      </template>
      <template #table-columns>
        <el-table-column type="index" label="序号" align="center" />
        <el-table-column prop="name" label="设备名称" min-width="200px" show-overflow-tooltip sortable="custom">
          <template v-slot="{row}">
            <a
              class="table-column-link has-icon"
              href="javascript:void(0)"
              style="margin-left: 5px"
              @mouseenter="showPopover($event,row)"
              @mouseleave="hidePopover"
              @click="gotoDetail(row)"
            >
              <svg-icon
                icon-class="device-status"
                :class-name="'device-status-icon ' + getDeviceStatusClass(_.get(row,'onlineStatus')) "
              />
              <span>{{ row.name }}</span>
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="deviceRole" label="设备性质" width="140px" align="center">
          <template v-slot="{row}">
            {{ getDictValue(dict.device_role, row.deviceRole) }}
          </template>
        </el-table-column>
        <el-table-column prop="manufacturerName" label="设备厂商" align="center" show-overflow-tooltip width="80px" />
        <el-table-column prop="modelName" label="厂商型号" align="center" show-overflow-tooltip width="200px" />
        <el-table-column prop="onlineStatus" label="设备状态" width="100px" align="center" sortable="custom">
          <template v-slot="{row}">
            <span :style="{color: row.onlineStatus === 1 ? '#67C23A' : '#F56C6C'}">
              {{ getDictValue(dict.online_status, row.onlineStatus, '未知') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="snmpStatus" label="SNMP状态" width="120px" align="center" sortable="custom">
          <template v-slot="{row}">
            <span :style="{color: row.snmpStatus === 1 ? '#67C23A' : '#F56C6C'}">
              {{ _.isNil(row.snmpStatus) ? '未知' : row.snmpStatus === 1 ? '正常' : '异常' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="snmpVersion" label="SNMP版本" width="120px" align="center" sortable="custom">
          <template v-slot="{row}">
            {{ getDictValue(dict.snmp_version, row.snmpVersion) }}
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="管理地址" align="center" width="130px" sortable="custom" />
        <el-table-column prop="stationName" label="所属站点" align="center" show-overflow-tooltip min-width="150px" />
        <el-table-column
          prop="area12345.area2"
          label="所属地市"
          align="center"
          show-overflow-tooltip
          min-width="140px"
        />
        <el-table-column
          prop="area12345.area3"
          label="所属区局"
          align="center"
          show-overflow-tooltip
          min-width="160px"
        />
        <el-table-column
          prop="area12345.area4"
          label="所属供电所"
          align="center"
          show-overflow-tooltip
          min-width="160px"
        />
        <el-table-column prop="businessType" label="接入业务" align="center" show-overflow-tooltip min-width="120px" />
        <el-table-column prop="bizIp" label="接入业务地址" align="center" show-overflow-tooltip min-width="120px" />
        <el-table-column prop="operatingState" label="投运状态" align="center" width="120px">
          <template v-slot="{row}">
            {{ getDictValue(dict.device_operation_state, row.operatingState, '未知') }}
          </template>
        </el-table-column>
        <el-table-column prop="operatingTime" label="投运日期" align="center" width="160px" sortable="custom">
          <template v-slot="{row}">
            {{ row.operatingTime ? $dayjs(row.operatingTime).format('YYYY-MM-DD') : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="bizCode" label="最新移交单号" align="center" show-overflow-tooltip width="140px" />
        <el-table-column prop="bizName" label="项目名称" align="center" show-overflow-tooltip width="140px" />
        <el-table-column prop="sourceId" label="GISID" align="center" show-overflow-tooltip width="140px" />
        <el-table-column prop="asset" label="台账资产" align="center" width="130px">
          <template v-slot="{row}">
            {{ getDictValue(dict.asset_owner, row.asset, '未知') }}
          </template>
        </el-table-column>
        <el-table-column prop="code" label="设备资产编码" align="center" width="120px" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建日期" align="center" show-overflow-tooltip width="140px">
          <template v-slot="{row}">
            {{ $dayjs(row.createTime).format('YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column prop="versionDesc" label="软件版本信息" align="center" width="150px" show-overflow-tooltip />
        <el-table-column prop="modelValide" label="型号匹配结果" align="center" width="100px">
          <template v-slot="{row}">
            <span :style="{color: row.modelValide === '正确' ? '#67C23A' : '#F56C6C'}">
              {{ _.isNil(row.modelValide) ? '未知' : row.modelValide }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sysObjectId" label="设备型号KEY" align="center" width="110px" show-overflow-tooltip />
        <el-table-column prop="sysObjectId" label="能否远程登录" align="center" width="110px">
          <template v-slot="{row}">
            {{ _.isNil(row.sshTelnet) ? '未知' : row.sshTelnet ? '能远程登录' : '不能远程登录' }}
          </template>
        </el-table-column>
        <el-table-column prop="portCount" label="端口数量" align="center" width="80px" />
        <el-table-column prop="electricsPortCount" label="电口数量" align="center" width="80px" />
        <el-table-column prop="fiberPortCount" label="光口数量" align="center" width="80px" />
        <el-table-column prop="cpuUsedRate" label="CPU利用率（%）" align="center" width="120px" />
        <el-table-column prop="memoryUsedRate" label="内存利用率（%）" align="center" width="120px" />
        <el-table-column prop="assetUniqueCode" label="通科唯一编码" align="center" width="140px" show-overflow-tooltip />
        <el-table-column prop="power1" label="电源1" align="center" width="140px" />
        <el-table-column prop="power2" label="电源2" align="center" width="140px" />
        <el-table-column label="操作" fixed="right" width="250px" align="center">
          <template v-slot="{row}">
            <el-button type="text" size="mini" @click="gotoDetail(row)">查看</el-button>
            <el-button
              v-if="!onlyView"
              v-has-permission="['DeviceList:update']"
              size="mini"
              type="text"
              @click="editDevice(row.id)"
            >编辑
            </el-button>
            <el-button
              v-if="!onlyView"
              v-has-permission="['DeviceList:delete']"
              size="mini"
              type="text"
              @click="deleteDevice(row)"
            >删除
            </el-button>
            <el-dropdown @command="(e) => { gotoTopology(e, row) }">
              <el-button
                type="text"
                size="mini"
                style="margin-left: 10px;"
              >定位
              </el-button>
              <el-dropdown-menu>
                <el-dropdown-item command="1">网络拓扑图</el-dropdown-item>
                <el-dropdown-item command="2">光缆网沿布图</el-dropdown-item>
                <el-dropdown-item command="3">IP地址拓扑图</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </template>
    </common-table>
    <show-ip-topology-location-dialog ref="ShowIpTopologyLocationDialogRef" />
    <device-popover ref="DevicePopoverRef" />
    <show-topology-location-dialog ref="ShowTopologyLocationDialogRef" />
  </div>
</template>

<script>
import AreaSelect from '@/components/AreaSelect.vue'
import IpInput from '@/components/IpInput.vue'
import { tableMixin } from '@/mixins/table'
import website from '@/config/website'
import DeviceApi from '@/api/transmit/device'
import ManufacturerApi from '@/api/pub/manufacturer'
import ModelApi from '@/api/pub/model'
import { debounce } from 'lodash'
import ShowIpTopologyLocationDialog
from '@/views/resources/device-management/device-list/components/ShowIpTopologyLocationDialog.vue'
import { getDictValue } from '@/util/dict'
import DevicePopover from '@/components/DevicePopover.vue'
import ShowTopologyLocationDialog
from '@/views/resources/device-management/device-list/components/ShowTopologyLocationDialog.vue'

export default {
  name: 'DeviceListTable',
  components: { ShowTopologyLocationDialog, DevicePopover, ShowIpTopologyLocationDialog, IpInput, AreaSelect },
  mixins: [tableMixin],
  inject: ['dict'],
  props: {
    onlyView: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      website,
      filter: {
        current: 1,
        total: 0,
        pageSize: 10,
        areaId: null,
        asset: null,
        ip: '',
        name: '',
        onlineStatus: '',
        snmpStatus: '',
        snmpVersion: '',
        modelValide: null,
        manufacturerId: '',
        modelId: '',
        stationName: '',
        operatingState: ['1'],
        deviceRole: [],
        businessType: '',
        sourceId: '',
        sysObjectId: '',
        sort: '',
        order: '',
        date: null,
        sshTelnet: null,
        maskBizIpAndMaskBit: ''
      },
      manufacturerOptions: [],
      modelOptions: []
    }
  },
  mounted() {
    this.loadManufacturerOptions()
  },
  methods: {
    getDictValue,
    async init(queryParams = {}) {
      this.filter = this._.assign(this.filter, queryParams)
      await this.doSearch()
    },
    showSearchMore() {
      this.$refs.CommonTableRef.showSearchMore()
    },
    doReset() {
      this.filter = this._.assign(this.filter, {
        areaId: null,
        date: null,
        asset: null,
        ip: '',
        name: '',
        onlineStatus: '',
        programId: null,
        snmpStatus: '',
        snmpVersion: '',
        modelValide: null,
        sysObjectId: null,
        manufacturerId: '',
        modelId: '',
        stationName: '',
        operatingState: ['1'],
        deviceRole: [],
        sourceId: '',
        maskBizIpAndMaskBit: ''
      })
      this.filter.operatingState = ['1']
      this.doSearch()
    },
    async loadData() {
      this.loading = true
      this.tableData = []
      try {
        const { data: res } = await DeviceApi.getDevicePage(this.filter)
        if (res.success) {
          this.tableData = res.data.records
          this.filter.total = res.data.total
        }
      } catch (e) {
        console.warn(e)
      } finally {
        this.loading = false
      }
    },
    async loadManufacturerOptions() {
      this.manufacturerOptions = []
      try {
        const { data: res } = await ManufacturerApi.getManufacturerAllList()
        if (res.success) {
          this.manufacturerOptions = this._.map(res.data, (item) => {
            return { id: item.id, name: item.title }
          })
        }
      } catch (e) {
        console.warn(e)
      }
    },
    async manufacturerIdChangeCallback() {
      this.filter.modelId = ''
      this.modelOptions = []
      try {
        if (!this._.isEmpty(this.filter.manufacturerId)) {
          let { data: res } = await ModelApi.queryModel({
            current: 1,
            pageSize: -1,
            manufacturerId: this.filter.manufacturerId
          })
          if (res.success) {
            this.modelOptions = res.data.records
          }
        }
      } catch (e) {
        console.warn(e)
      }
    },
    async handleExport() {
      this.loadingExport = true
      try {
        const response = await DeviceApi.exportExl(this.filter)
        if (response.data) {
          if (this.$store.state.exportExl.timerDict.has('device-list')) {
            this.$message({ type: 'error', message: '正在导出，请勿重复操作！' })
            return
          }
          this.$store.commit('SET_EXPORT_LOOP', {
            url: '/icomx-trs/device/export-latest-device',
            key: 'device-list'
          })
        }
      } catch (e) {
        console.warn(e)
      } finally {
        this.loadingExport = false
      }
    },
    batchModifyDevice() {
      this.$router.push({
        name: 'BatchModifyDevicePage'
      })
    },
    batchCommandDevice() {
      this.$router.push({
        name: 'BatchModifyDevicePage',
        query: {
          command: 1
        }
      })
    },
    goToImportData() {
      this.$router.push({
        name: 'DeviceDataImport'
      })
    },
    async refreshDeviceGatherInfoHandler() {
      try {
        const { data: res } = await DeviceApi.refreshDeviceGatherInfo()
        if (res.success) {
          this.$message.success('操作成功')
        }
      } catch (e) {
        console.warn(e)
      }
    },
    async batchCheckStatus() {
      try {
        const { data: res } = await DeviceApi.batchCheckStatus()
        if (res.success) {
          this.$message.success('操作成功')
        }
      } catch (e) {
        console.warn(e)
      }
    },
    gotoDetail(row) {
      this.hidePopover()
      this.$router.push(`/resources/device-management/device-detail/${row.id}`)
      this.hidePopover()
    },
    getDeviceStatusClass(status) {
      return status === 1 ? 'online-icon' : 'offline-icon'
    },
    hidePopover() {
      this.$refs.DevicePopoverRef.hide()
    },
    showPopover: debounce(function(event, row) {
      // 延迟加载popover的数据
      this.$refs.DevicePopoverRef.show(event.target, row)
    }, 500),
    gotoTemplate() {
      this.$router.push({
        name: 'DeviceTemplateList'
      })
    },
    addDevice() {
      this.$emit('on-add')
    },
    editDevice(id) {
      this.$emit('on-edit', id)
    },
    deleteDevice(row) {
      this.$emit('on-delete', row)
    },
    async locateInTopology(id) {
      try {
        this.$refs.ShowTopologyLocationDialogRef.view(id)
      } catch (e) {
        console.warn(e)
      }
    },
    // 光缆网拓扑图定位
    locateInMapHandler(stationId) {
      if (stationId) {
        this.$store.commit('SET_LOCATION_RESOURCE', { id: stationId })
        this.$router.push({
          path: '/resources/optical-cable/topology/index'
        })
      } else {
        this.$message.warning('该设备没有对应的站点')
      }
    },
    gotoTopology(command, row) {
      switch (command) {
        case '1':
          this.locateInTopology(row.id)
          break
        case '2':
          this.locateInMapHandler(row.stationId)
          break
        case '3':
          this.$refs.ShowIpTopologyLocationDialogRef.view({ id: row.id })
          break
        default:
          break
      }
    }
  }
}
</script>

