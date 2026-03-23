import request from './auth'

/**
 * 获取所有用户
 * @returns {Promise}
 */
export function getAllUsers() {
  return request({
    url: '/users',
    method: 'get'
  })
}

/**
 * 创建用户
 * @param {Object} userData 用户数据
 * @returns {Promise}
 */
export function createUser(userData) {
  // 确保传递name字段到后端
  const data = {
    username: userData.username,
    password: userData.password,
    name: userData.name,
    tenantId: userData.tenantId
  }
  
  return request({
    url: '/users',
    method: 'post',
    data: data
  })
}

/**
 * 更新用户
 * @param {number} id 用户ID
 * @param {Object} userData 用户数据
 * @returns {Promise}
 */
export function updateUser(id, userData) {
  // 确保传递name字段到后端
  const data = {
    ...userData,
    name: userData.name || ''  // 确保name字段存在
  }
  
  return request({
    url: `/users/${id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除用户
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/users/${id}`,
    method: 'delete'
  })
}

/**
 * 分配角色给用户
 * @param {number} userId 用户ID
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function assignRole(userId, roleId) {
  return request({
    url: `/users/${userId}/roles/${roleId}`,
    method: 'post'
  })
}

/**
 * 移除用户的角色
 * @param {number} userId 用户ID
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function removeRole(userId, roleId) {
  return request({
    url: `/users/${userId}/roles/${roleId}`,
    method: 'delete'
  })
}