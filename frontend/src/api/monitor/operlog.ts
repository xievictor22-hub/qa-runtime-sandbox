import request from '@/api/index'

const BASE_URL = '/monitor/operlog'

export interface OperLogQuery {
  pageNum: number
  pageSize: number
  title?: string
  operName?: string
  businessType?: number
  status?: number
}

export function listOperLog(params: OperLogQuery) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

export function delOperLog(operId: number | number[]) {
  // 支持单个或批量 ID (如果是数组 join 成字符串)
  const ids = Array.isArray(operId) ? operId.join(',') : operId
  return request({
    url: `${BASE_URL}/${ids}`,
    method: 'delete'
  })
}

export function cleanOperLog() {
  return request({
    url: `${BASE_URL}/clean`,
    method: 'delete'
  })
}