
import request from './auth'
// 获取设备类型分页列表
export function fetchModelTypeList(params) {
  return request({
    url: '/equipment-type/devices/page',
    method: 'get',
    params: params
  })
}
