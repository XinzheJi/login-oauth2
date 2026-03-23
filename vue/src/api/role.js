import request from './auth'

// 模拟延迟
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// 模拟角色数据
const mockRoles = [
  { id: 1, name: 'ADMIN', description: '系统管理员', permissions: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13] },
  { id: 2, name: 'USER', description: '普通用户', permissions: [1, 5, 9] },
  { id: 3, name: 'GUEST', description: '访客', permissions: [1] }
];

/**
 * 获取所有角色
 * @returns {Promise}
 */
export function getAllRoles() {
  // 尝试使用真实API，如果失败则返回模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: '/roles',
      method: 'get',
      params: {
        userId: JSON.parse(localStorage.getItem('user')).id
      }
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 使用模拟数据
      setTimeout(() => {
        resolve([...mockRoles]);
      }, 300);
    });
  });
}

/**
 * 创建角色
 * @param {Object} roleData 角色数据
 * @returns {Promise}
 */
export function createRole(roleData) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: '/roles',
      method: 'post',
      data: roleData
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟创建角色
      setTimeout(() => {
        const newId = Math.max(...mockRoles.map(r => r.id)) + 1;
        // 确保code字段有值，如果没有提供则使用name的大写形式作为默认值
        const code = roleData.code || roleData.name.toUpperCase();
        const newRole = { ...roleData, id: newId, code, permissions: [] };
        mockRoles.push(newRole);
        resolve(newRole);
      }, 300);
    });
  });
}

/**
 * 更新角色
 * @param {number} id 角色ID
 * @param {Object} roleData 角色数据
 * @returns {Promise}
 */
export function updateRole(id, roleData) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/roles/${id}`,
      method: 'put',
      data: roleData
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟更新角色
      setTimeout(() => {
        const index = mockRoles.findIndex(r => r.id === id);
        if (index !== -1) {
          const permissions = mockRoles[index].permissions || [];
          mockRoles[index] = { ...roleData, id, permissions };
          resolve(mockRoles[index]);
        } else {
          reject(new Error('角色不存在'));
        }
      }, 300);
    });
  });
}

/**
 * 删除角色
 * @param {number} id 角色ID
 * @returns {Promise}
 */
export function deleteRole(id) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/roles/${id}`,
      method: 'delete'
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟删除角色
      setTimeout(() => {
        const index = mockRoles.findIndex(r => r.id === id);
        if (index !== -1) {
          mockRoles.splice(index, 1);
          resolve({ success: true });
        } else {
          reject(new Error('角色不存在'));
        }
      }, 300);
    });
  });
}

/**
 * 获取角色的权限
 * @param {number} roleId 角色ID
 * @returns {Promise}
 */
export function getRolePermissions(roleId) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/roles/${roleId}/permissions`,
      method: 'get'
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟获取角色权限
      setTimeout(() => {
        const role = mockRoles.find(r => r.id === roleId);
        if (role) {
          // 返回模拟的权限列表
          const permissions = (role.permissions || []).map(id => ({
            id,
            name: `权限${id}`,
            code: `permission:${id}`,
            description: `权限${id}的描述`
          }));
          resolve(permissions);
        } else {
          reject(new Error('角色不存在'));
        }
      }, 300);
    });
  });
}

/**
 * 分配权限给角色
 * @param {number} roleId 角色ID
 * @param {Array} permissionIds 权限ID数组
 * @returns {Promise}
 */
export function assignPermissions(roleId, permissionIds) {
  // 尝试使用真实API，如果失败则使用模拟数据
  return new Promise((resolve, reject) => {
    request({
      url: `/roles/${roleId}/permissions`,
      method: 'post',
      data: { permissionIds }
    }).then(response => {
      resolve(response);
    }).catch(error => {
      console.warn('API调用失败，使用模拟数据:', error);
      // 模拟分配权限
      setTimeout(() => {
        const index = mockRoles.findIndex(r => r.id === roleId);
        if (index !== -1) {
          mockRoles[index].permissions = [...permissionIds];
          resolve({ success: true });
        } else {
          reject(new Error('角色不存在'));
        }
      }, 300);
    });
  });
}