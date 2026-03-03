import request from '@/api/index'

export function listDept(query?: any) {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params: query
  })
}

export function addDept(data: any) {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

export function updateDept(data: any) {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

export function delDept(deptId: number) {
  return request({
    url: `/system/dept/${deptId}`,
    method: 'delete'
  })
}