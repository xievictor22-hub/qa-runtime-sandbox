// 认证事件模块
// 路径: src/api/auth/auth-events.ts
// 功能：认证事件模块，用于在登录、登出、token 更新时触发事件

// 事件类型
type Listener<T = any> = (payload?: T) => void

// 事件名称
const TOKEN_UPDATED = 'auth:token-updated'
const LOGOUT = 'auth:logout'

// 触发 token 更新事件
export function emitTokenUpdated(payload: { accessToken: string }) {
  window.dispatchEvent(new CustomEvent(TOKEN_UPDATED, { detail: payload }))
}

// 监听 token 更新事件
export function onTokenUpdated(fn: Listener<{ accessToken: string }>) {
  const handler = (e: any) => fn(e.detail)
  window.addEventListener(TOKEN_UPDATED, handler as any)
  return () => window.removeEventListener(TOKEN_UPDATED, handler as any)
}

// 触发登出事件
export function emitLogout() {
  window.dispatchEvent(new CustomEvent(LOGOUT))
}

// 监听登出事件
export function onLogout(fn: Listener) {
  const handler = () => fn()
  window.addEventListener(LOGOUT, handler as any)
  return () => window.removeEventListener(LOGOUT, handler as any)
}