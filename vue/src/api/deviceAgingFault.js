import request from './auth'

// 获取设备老化数据（预留，当前页面未使用）
export function getDeviceAgingData(params) {
  return request({
    url: '/api/device-aging/data',
    method: 'get',
    params
  })
}

// 获取设备故障趋势数据（预留，当前页面未使用）
export function getDeviceFaultData(params) {
  return request({
    url: '/api/device-fault/data',
    method: 'get',
    params
  })
}

// 查询历史预测结果（AGING/FAULT），对应接口 /api/tool/predict/history
export function getPredictHistory(params) {
  return request({
    url: '/tool/predict/history',
    method: 'get',
    params
  })
}

// 获取设备历史健康数据，对应接口 /api/tool/predict/device-health-history
export function getDeviceHealthHistory(params) {
  return request({
    url: '/tool/predict/device-health-history',
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

// 调用后端老化预测接口
export function predictDeviceAging(data) {
  return request({
    url: '/tool/predict/aging',
    method: 'post',
    data
  }).then(res => {
    if (res && res.result) {
      res.result.forecast = Array.isArray(res.result.forecast) ? res.result.forecast : []
    }
    return res
  })
}

// 调用后端故障趋势预测接口
export function predictDeviceFault(data) {
  return request({
    url: '/tool/predict/fault',
    method: 'post',
    data
  }).then(res => {
    if (res && res.result) {
      res.result.forecast = Array.isArray(res.result.forecast) ? res.result.forecast : []
    }
    return res
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
