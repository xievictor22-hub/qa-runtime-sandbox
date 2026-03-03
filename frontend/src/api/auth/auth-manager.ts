import router from '@/router'
import { useUserStore } from '@/stores/auth/user'
import { usePermissionStore } from '@/stores/auth/permission'
import { refreshTokenApi } from '@/api/auth/refresh'
import { emitLogout, emitTokenUpdated } from './auth-events'

// 认证管理器
// 路径: src/api/auth/auth-manager.ts
// 功能：认证管理器，用于处理 token 刷新、登出等逻辑

// 类型定义
type Pending = (token: string) => void
// 认证管理器
class AuthManager {
  private isRefreshing = false
  private queue: Pending[] = []
  private logoutLock = false // 防止并发重复登出

  private resolveQueue(token: string) {
    this.queue.forEach(cb => cb(token))
    this.queue = []
  }

  private clearQueue() {
    this.queue = []
  }
  // 登出方法
  async logout(reason: string, redirectToLogin = true) {
    if (this.logoutLock) return
    this.logoutLock = true

    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    try {
      // 关键：这里只做“前端权威清理”，后端 logoutApi 是 best-effort（由 userStore 或调用方决定）
      userStore.token = ''
      userStore.nickname = ''
      userStore.avatar = ''
      userStore.roles = []
      userStore.userId = 0 // 新增：清空用户 ID
      userStore.permissions = []
      userStore.refreshToken = ''

      permissionStore.resetRoutes()

      emitLogout()

      if (redirectToLogin) {
        const redirect = encodeURIComponent(location.pathname + location.search)
        await router.replace(`/login?redirect=${redirect}`)
      }
    } finally {
      this.logoutLock = false
    }
  }

  /**
   * 处理 HTTP 401：单飞 refresh + 队列重放
   * @param originalRequest axios 的 error.config
   * @param service 主 axios 实例（用于重放请求）
   */
  async handle401(originalRequest: any, service: any) {
    const userStore = useUserStore()

    // 0) refresh/logout 请求不参与 401 刷新逻辑（防递归）
    const url = String(originalRequest?.url || '')
    if (url.includes('/auth/refresh') || url.includes('/auth/logout')) {
      await this.logout('401 on auth endpoint')
      throw new Error('Unauthorized')
    }

    // 1) 没 refreshToken，直接登出
    if (!userStore.refreshToken) {
      await this.logout('missing refreshToken')
      throw new Error('Unauthorized')
    }

    // 2) 防止同一请求无限重试
    if (originalRequest?._retry) {
      await this.logout('retry exhausted')
      throw new Error('Unauthorized')
    }
    originalRequest._retry = true

    // 3) 如果正在刷新：加入队列等待
    if (this.isRefreshing) {
      return new Promise((resolve) => {
        this.queue.push((newAccess: string) => {
          resolve(service(this.setAuthHeader(originalRequest, newAccess)))
        })
      })
    }

    // 4) 单飞刷新
    this.isRefreshing = true
    try {
      const data = await refreshTokenApi(userStore.refreshToken)
      // rotation：两个都更新
      userStore.setToken(data.accessToken)
      userStore.setRefreshToken(data.refreshToken)

      emitTokenUpdated({ accessToken: data.accessToken })

      this.resolveQueue(data.accessToken)

      // 5) 重放当前请求
      return service(this.setAuthHeader(originalRequest, data.accessToken))
    } catch (e) {
      this.clearQueue()
      await this.logout('refresh failed')
      throw e
    } finally {
      this.isRefreshing = false
    }
  }

  private setAuthHeader(config: any, token: string) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
    return config
  }
}

export const authManager = new AuthManager()