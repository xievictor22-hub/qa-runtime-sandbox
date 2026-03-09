import request from '@/api/index'
import type { PageResult } from '@/api/types'

const BASE_URL = '/monitor/operlog'

export interface OperLogQuery {
  pageNum: number
  pageSize: number
  title?: string
  operName?: string
  businessType?: number
  status?: number
}

export interface OperLogItem {
  operId: number
  title?: string
  businessType?: number
  method?: string
  requestMethod?: string
  operName?: string
  status?: number
  operTime?: string
}

export function listOperLog(params: OperLogQuery): Promise<PageResult<OperLogItem>> {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

export function delOperLog(operId: number | number[]): Promise<void> {
  // 支持单个或批量 ID (如果是数组 join 成字符串)
  const ids = Array.isArray(operId) ? operId.join(',') : operId
  return request({
    url: `${BASE_URL}/${ids}`,
    method: 'delete'
  })
}

export function cleanOperLog(): Promise<void> {
  return request({
    url: `${BASE_URL}/clean`,
    method: 'delete'
  })
}
