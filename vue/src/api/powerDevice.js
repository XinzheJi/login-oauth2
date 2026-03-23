import request from './auth'

/**
 * 分页查询电源设备
 * @param {Object} queryParams 查询参数
 * @returns {Promise}
 */
export function getDeviceList(queryParams) {
  return request({
    url: '/power/devices',
    method: 'get',
    params: queryParams
  })
}

/**
 * 根据设备ID查询电源设备
 * @param {string} deviceId 设备ID
 * @returns {Promise}
 */
export function getDeviceById(deviceId) {
  return request({
    url: `/power/devices/${deviceId}`,
    method: 'get'
  })
}

/**
 * 创建电源设备
 * @param {Object} deviceData 设备数据
 * @returns {Promise}
 */
export function createDevice(deviceData) {
  return request({
    url: '/power/devices',
    method: 'post',
    data: deviceData
  })
}

/**
 * 更新电源设备
 * @param {string} deviceId 设备ID
 * @param {Object} deviceData 设备数据
 * @returns {Promise}
 */
export function updateDevice(deviceId, deviceData) {
  return request({
    url: `/power/devices/${deviceId}`,
    method: 'put',
    data: deviceData
  })
}

/**
 * 删除电源设备
 * @param {string} deviceId 设备ID
 * @returns {Promise}
 */
export function deleteDevice(deviceId) {
  return request({
    url: `/power/devices/${deviceId}`,
    method: 'delete'
  })
} 