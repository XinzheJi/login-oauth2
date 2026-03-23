import request from './auth'

// 模拟权限数据
const mockPermissions = [
  { id: 1, code: 'user:view', name: '查看用户', description: '允许查看用户列表' },
  { id: 2, code: 'user:create', name: '创建用户', description: '允许创建新用户' },
  { id: 3, code: 'user:update', name: '更新用户', description: '允许更新用户信息' },
  { id: 4, code: 'user:delete', name: '删除用户', description: '允许删除用户' },
  { id: 5, code: 'role:view', name: '查看角色', description: '允许查看角色列表' },
  { id: 6, code: 'role:create', name: '创建角色', description: '允许创建新角色' },
  { id: 7, code: 'role:update', name: '更新角色', description: '允许更新角色信息' },
  { id: 8, code: 'role:delete', name: '删除角色', description: '允许删除角色' },
  { id: 9, code: 'permission:view', name: '查看权限', description: '允许查看权限列表' },
  { id: 10, code: 'permission:create', name: '创建权限', description: '允许创建新权限' },
  { id: 11, code: 'permission:update', name: '更新权限', description: '允许更新权限信息' },
  { id: 12, code: 'permission:delete', name: '删除权限', description: '允许删除权限' },
  { id: 13, code: 'permission:assign', name: '分配权限', description: '允许分配权限给角色' },
];

// 模拟权限-角色映射
const mockPermissionRoles = {
  1: [1, 2], // user:view 权限分配给 ADMIN, USER
  2: [1],    // user:create 权限分配给 ADMIN
  3: [1],    // user:update 权限分配给 ADMIN
  4: [1],    // user:delete 权限分配给 ADMIN
  5: [1, 2], // role:view 权限分配给 ADMIN, USER
  6: [1],    // role:create 权限分配给 ADMIN
  7: [1],    // role:update 权限分配给 ADMIN
  8: [1],    // role:delete 权限分配给 ADMIN
  9: [1, 2], // permission:view 权限分配给 ADMIN, USER
  10: [1],   // permission:create 权限分配给 ADMIN
  11: [1],   // permission:update 权限分配给 ADMIN
  12: [1],   // permission:delete 权限分配给 ADMIN
  13: [1],   // permission:assign 权限分配给 ADMIN
};

/**
 * 获取所有权限
 * @returns {Promise}
 */
export function getAllPermissions() {
  // 尝试使用真实API，如果失败则返回模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: '/permissions',
      method: 'get'
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 使用模拟数据
      setTimeout(() => {
        resolve([...mockPermissions]);
      }, 300);
    });
  });
}

/**
 * 创建权限
 * @param {Object} permissionData 权限数据
 * @returns {Promise}
 */
export function createPermission(permissionData) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: '/permissions',
      method: 'post',
      data: permissionData
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟创建权限
      setTimeout(() => {
        const newId = Math.max(...mockPermissions.map(p => p.id)) + 1;
        const newPermission = { ...permissionData, id: newId };
        mockPermissions.push(newPermission);
        mockPermissionRoles[newId] = [];
        resolve(newPermission);
      }, 300);
    });
  });
}

/**
 * 更新权限
 * @param {number} id 权限ID
 * @param {Object} permissionData 权限数据
 * @returns {Promise}
 */
export function updatePermission(id, permissionData) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/permissions/${id}`,
      method: 'put',
      data: permissionData
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟更新权限
      setTimeout(() => {
        const index = mockPermissions.findIndex(p => p.id === id);
        if (index !== -1) {
          mockPermissions[index] = { ...permissionData, id };
          resolve(mockPermissions[index]);
        } else {
          reject(new Error('权限不存在'));
        }
      }, 300);
    });
  });
}

/**
 * 删除权限
 * @param {number} id 权限ID
 * @returns {Promise}
 */
export function deletePermission(id) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/permissions/${id}`,
      method: 'delete'
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟删除权限
      setTimeout(() => {
        const index = mockPermissions.findIndex(p => p.id === id);
        if (index !== -1) {
          mockPermissions.splice(index, 1);
          delete mockPermissionRoles[id];
          resolve({ success: true });
        } else {
          reject(new Error('权限不存在'));
        }
      }, 300);
    });
  });
}

/**
 * 获取权限的角色
 * @param {number} permissionId 权限ID
 * @returns {Promise}
 */
export function getPermissionRoles(permissionId) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/permissions/${permissionId}/roles`,
      method: 'get'
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟获取权限角色
      setTimeout(() => {
        const roleIds = mockPermissionRoles[permissionId] || [];
        const roles = roleIds.map(id => ({
          id,
          name: id === 1 ? 'ADMIN' : id === 2 ? 'USER' : 'GUEST',
          description: id === 1 ? '系统管理员' : id === 2 ? '普通用户' : '访客'
        }));
        resolve(roles);
      }, 300);
    });
  });
}

/**
 * 分配角色给权限
 * @param {number} permissionId 权限ID
 * @param {Array} roleIds 角色ID数组
 * @returns {Promise}
 */
export function assignRoles(permissionId, roleIds) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/permissions/${permissionId}/roles`,
      method: 'post',
      data: { roleIds }
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟分配角色
      setTimeout(() => {
        mockPermissionRoles[permissionId] = [...roleIds];
        resolve({ success: true });
      }, 300);
    });
  });
}

/**
 * 获取所有角色
 * @returns {Promise}
 */
export function getAllRoles() {
  return request({
    url: '/roles',
    method: 'get'
  });
}

export default {
  getAllPermissions,
  createPermission,
  updatePermission,
  deletePermission,
  getPermissionRoles,
  assignRoles,
  getAllRoles // 添加新的导出
}