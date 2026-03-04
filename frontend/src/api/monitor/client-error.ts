import request from '@/api'
export function reportClientError(data: any) {
  return request({
    url: '/monitor/client-error',
    method: 'post',
    data
  })
}