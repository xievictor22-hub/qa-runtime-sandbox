import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/auth/user'

/**
 * 按钮权限控制指令 v-hasPerm
 * 使用示例：<el-button v-hasPerm="['system:user:add']">新增</el-button>
 * 说明：判断当前用户是否有指定的权限标识。如果没有权限，则移除该 DOM 元素。
 */
export const hasPerm: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const all_permission = '*:*:*' // 超级管理员权限标识
    const userStore = useUserStore()
    const permissions = userStore.permissions||[]

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(permission => {
        return all_permission === permission || permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        // 没有权限，移除该 DOM 元素
        el.parentNode && el.parentNode.removeChild(el)
      }

    } else {
      throw new Error(`需要指定权限标识! 例如 v-permission="['system:user:add']"`)
    }
  }
}