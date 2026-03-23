import request from './auth'

/**
 * 获取所有租户
 * @returns {Promise}
 */
export function getAllTenants() {
  return request({
    url: '/tenants',
    method: 'get'
  })
}

/**
 * 创建租户
 * @param {Object} tenantData 租户数据
 * @returns {Promise}
 */
export function createTenant(tenantData) {
  // 转换前端租户字段名称为后端字段名称
  const backendTenantData = {
    tenantCode: tenantData.code,
    tenantName: tenantData.name,
    status: tenantData.status || 'active'
  }
  
  return request({
    url: 'tenants',
    method: 'post',
    data: backendTenantData
  })
}

/**
 * 更新租户
 * @param {number} id 租户ID
 * @param {Object} tenantData 租户数据
 * @returns {Promise}
 */
export function updateTenant(id, tenantData) {
  // 转换前端租户字段名称为后端字段名称
  const backendTenantData = {
    tenantName: tenantData.name,
    status: tenantData.status
  }
  
  return request({
    url: `/tenants/${id}`,
    method: 'put',
    data: backendTenantData
  })
}

/**
 * 删除租户
 * @param {number} id 租户ID
 * @returns {Promise}
 */
export function deleteTenant(id) {
  return request({
    url: `/tenants/${id}`,
    method: 'delete'
  })
}

/**
 * 切换租户状态
 * @param {number} id 租户ID
 * @param {string} status 状态 ('active' 或 'inactive')
 * @returns {Promise}
 */
export function toggleTenantStatus(id, status) {
  return request({
    url: `/tenants/${id}/status`,
    method: 'put',
    data: { status }
  })
}