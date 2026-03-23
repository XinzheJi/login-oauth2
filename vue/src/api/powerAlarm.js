import request from './auth'

/**
 * 分页查询告警
 * @param {Object} queryParams 查询参数
 * @returns {Promise}
 */
export function getAlarmList(queryParams) {
  return request({
    url: '/power/alarms',
    method: 'get',
    params: queryParams
  })
}

/**
 * 查询告警详情
 * @param {number} alarmId 告警ID
 * @returns {Promise}
 */
export function getAlarmDetail(alarmId) {
  return request({
    url: `/power/alarms/${alarmId}`,
    method: 'get'
  })
}

/**
 * 处理告警
 * @param {Object} processData 处理数据
 * @returns {Promise}
 */
export function processAlarm(processData) {
  return request({
    url: '/power/alarms/process',
    method: 'post',
    data: processData
  })
}

/**
 * 获取告警统计数据
 * @param {string} startTime 开始时间
 * @param {string} endTime 结束时间
 * @returns {Promise}
 */
export function getAlarmStatistics(startTime, endTime) {
  return request({
    url: '/power/alarms/statistics',
    method: 'get',
    params: { startTime, endTime }
  })
}

/**
 * 获取未处理的告警数量
 * @returns {Promise}
 */
export function getUnprocessedAlarmCount() {
  return request({
    url: '/power/alarms/unprocessed/count',
    method: 'get'
  })
}

/**
 * 获取指定设备的告警列表
 * @param {string} deviceId 设备ID
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 * @returns {Promise}
 */
export function getDeviceAlarms(deviceId, pageNum = 1, pageSize = 10) {
  return request({
    url: `/power/alarms/device/${deviceId}`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取最近的告警列表
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 * @returns {Promise}
 */
export function getRecentAlarms(pageNum = 1, pageSize = 10) {
  return request({
    url: '/power/alarms/recent',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

/**
 * 获取所有告警类型选项
 * @returns {Promise}
 */
export function getAlarmTypes() {
  return request({
    url: '/power/alarm-options/types',
    method: 'get'
  })
}

/**
 * 获取所有告警级别选项
 * @returns {Promise}
 */
export function getAlarmLevels() {
  return request({
    url: '/power/alarm-options/levels',
    method: 'get'
  })
} 