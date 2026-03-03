import request from '@/api/index'

const BASE_URL = '/monitor/logininfor'

export interface LoginInforQuery {
  pageNum: number
  pageSize: number
  ipaddr?: string
  username?: string
  status?: string
}

export function listLoginInfor(params: LoginInforQuery) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

export function delLoginInfor(loginId: number | number[]) {
  // 支持单个或批量 ID (如果是数组 join 成字符串)
  const ids = Array.isArray(loginId) ? loginId.join(',') : loginId
  return request({
    url: `${BASE_URL}/${ids}`,
    method: 'delete'
  })
}

export function cleanLoginInfor() {
  return request({
    url: `${BASE_URL}/clean`,
    method: 'delete'
  })
}