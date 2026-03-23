
import request from './auth'
// 获取交换机分页列表
export function fetchSwitchList(params) {

  return request({
    url: '/trs/devices/page',
    method: 'get',
    params: params
  })
}

// 你可以在这里继续扩展其它相关接口，如新增、编辑、删除等
// export function addSwitchDevice(data) { ... }
// export function updateSwitchDevice(id, data) { ... }
// export function deleteSwitchDevice(id) { ... }