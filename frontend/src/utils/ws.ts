export function getWsUrl() {
  // 开发：走 Vite dev server 的 ws 代理（相对路径）
  if (import.meta.env.DEV) {
    const path = import.meta.env.VITE_APP_WS_PATH || '/api/ws'
    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${proto}//${location.host}${path}`
  }

  // 生产：必须配置完整地址
  const url = import.meta.env.VITE_APP_WS_URL
  if (!url) {
    throw new Error('Missing VITE_APP_WS_URL in production env')
  }
  return url
}