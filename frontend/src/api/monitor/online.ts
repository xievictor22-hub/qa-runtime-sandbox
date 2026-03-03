import request from '@/api/index'

export function listOnline(query: any) {
  return request({
    url: '/monitor/online/list',
    method: 'get',
    params: query
  })
}

export function forceLogout(tokenId: string) {
  return request({
    url: `/monitor/online/${tokenId}`,
    method: 'delete'
  })
}