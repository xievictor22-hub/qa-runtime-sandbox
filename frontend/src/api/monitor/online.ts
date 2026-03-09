import request from '@/api/index'

export interface OnlineUserQuery {
  ipaddr?: string
  userName?: string
}

export interface OnlineUserItem {
  tokenId: string
  userName: string
  ipaddr: string
  loginLocation?: string
  browser?: string
  os?: string
  loginTime?: string
}

export function listOnline(query: OnlineUserQuery): Promise<OnlineUserItem[]> {
  return request({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  })
}

export function forceLogout(tokenId: string): Promise<void> {
  return request({
    url: `/monitor/online/${tokenId}`,
    method: 'delete'
  })
}
