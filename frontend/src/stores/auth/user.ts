import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi } from '@/api/auth/auth'
import type { LoginData } from '@/types/auth/auth.d'
import { usePermissionStore } from '@/stores/auth/permission'
import { authManager } from '@/api/auth/auth-manager'
/**
 * 用户状态管理
 * 路径: src/stores/auth/user.ts
 */
export const useUserStore = defineStore(
  'user', // 唯一 ID
  () => {
    // --- State ---
    const token = ref<string>('')
    const refreshToken = ref<string>('')
    const nickname = ref<string>('')
    const avatar = ref<string>('')
    const roles = ref<string[]>([]) // 预留角色权限
    const permissions = ref<string[]>([]) // 预留按钮权限
    const userId = ref<number>(0) // 新增：用户 ID
    const deviceId = ref<string>('') // 新增：设备 ID
    if(!deviceId.value){
      deviceId.value = getDeviceId()
    }

    // --- Actions ---
    
    /**
     * 登录动作
     */
    async function login(loginForm: LoginData) {
      const permissionStore = usePermissionStore()
      permissionStore.resetRoutes()
      try {
        const data = await loginApi(loginForm)
        // 赋值 State，插件会自动同步到 localStorage
        token.value = data.token || ''
        refreshToken.value = data.refreshToken || ''
        nickname.value = data.userInfo.nickname || ''
        userId.value = data.userInfo.id || 0 // 新增：赋值用户 ID
        avatar.value = data.userInfo.avatar || ''
        roles.value = data.userInfo.roles || []
        permissions.value = data.permissions || []
        await permissionStore.generateRoutes()
        return Promise.resolve(data)
      } catch (error) {
        return Promise.reject(error)
      }
    }
    async function setToken(newToken: string) {
      token.value = newToken
    }
    async function setRefreshToken(newRefreshToken: string) {
      refreshToken.value = newRefreshToken
    }

    /**
     * 登出动作
     */
    async function logout(redirectToLogin: boolean = true) {
     await authManager.logout('user logout', redirectToLogin)
    }

    return {
      token,
      refreshToken,
      nickname,
      avatar,
      roles,
      permissions,
      userId,
      deviceId,
      login,
      logout,
      setToken,
      setRefreshToken,
    }
  },
  {
    // --- Persistence Options ---
    // 开启持久化：默认存储所有 State 到 localStorage，key 为 store 的 id ('user')
    persist: true
    
    // 如果想要更细粒度的控制（例如只存 token，或者存到 sessionStorage），可以使用对象配置：
    /*
    persist: {
      key: 'mogo-user-store', // 自定义存储的 key
      storage: localStorage,  // 存储位置
      paths: ['token'],       // 只持久化 token，其他字段刷新即丢
    }
    */
  }
)

function getDeviceId() {
  if(typeof crypto !== 'undefined' && 'randomUUID' in crypto){
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}


