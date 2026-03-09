import request from '@/api/index'
import type { PageResult } from '@/api/types'

const BASE_URL = '/monitor/logininfor'

export interface LoginInforQuery {
  pageNum: number
  pageSize: number
  ipaddr?: string
  username?: string
  status?: string
}

export interface LoginInforItem {
  infoId: number
  userName?: string
  ipaddr?: string
  loginLocation?: string
  browser?: string
  os?: string
  status?: string
  msg?: string
  loginTime?: string
}

export function listLoginInfor(params: LoginInforQuery): Promise<PageResult<LoginInforItem>> {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

export function delLoginInfor(loginId: number | number[]): Promise<void> {
  // 支持单个或批量 ID (如果是数组 join 成字符串)
  const ids = Array.isArray(loginId) ? loginId.join(',') : loginId
  return request({
    url: `${BASE_URL}/${ids}`,
    method: 'delete'
  })
}

export function cleanLoginInfor(): Promise<void> {
  return request({
    url: `${BASE_URL}/clean`,
    method: 'delete'
  })
}
