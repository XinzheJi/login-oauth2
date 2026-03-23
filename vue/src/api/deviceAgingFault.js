import request from '@/utils/request'

// 获取设备老化数据
export function getDeviceAgingData(params) {
  return request({
    url: '/api/device-aging/data',
    method: 'get',
    params
  })
}

// 获取设备故障趋势数据
export function getDeviceFaultData(params) {
  return request({
    url: '/api/device-fault/data',
    method: 'get',
    params
  })
}

// 获取设备详细信息
export function getDeviceDetail(deviceId) {
  return request({
    url: `/api/device/detail/${deviceId}`,
    method: 'get'
  })
}

// 导出设备老化报告
export function exportAgingReport(params) {
  return request({
    url: '/api/device-aging/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出设备故障报告
export function exportFaultReport(params) {
  return request({
    url: '/api/device-fault/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
