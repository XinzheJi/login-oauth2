import { useUserStore } from '@/store/user'

/**
 * 权限指令
 * 用法：v-permission="'user:create'" 或 v-permission="['user:create', 'user:edit']"
 */
export const permission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const { value } = binding
    const userRoles = userStore.userInfo.roles || []
    const userPermissions = userStore.userInfo.permissions || []
    
    // 检查用户是否有权限
    const hasPermission = () => {
      if (!value) return true
      
      // 如果是超级管理员，直接返回true
      if (userRoles.some(role => {
        return typeof role === 'object' ? role.name === 'ADMIN' : role === 'ADMIN'
      })) return true
      
      // 如果value是数组，检查是否有任一权限
      if (Array.isArray(value)) {
        return value.some(permission => userPermissions.includes(permission))
      }
      
      // 如果value是字符串，直接检查
      return userPermissions.includes(value)
    }
    
    // 如果没有权限，则移除元素
    if (!hasPermission()) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * 注册权限指令
 * @param {Object} app Vue应用实例
 */
export function setupPermission(app) {
  app.directive('permission', permission)
}

export default setupPermission