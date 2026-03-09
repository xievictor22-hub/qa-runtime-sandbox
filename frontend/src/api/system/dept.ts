import request from '@/api/index'

export interface DeptItem {
  deptId: number
  parentId: number
  deptName: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: string
  children?: DeptItem[]
}

export interface DeptForm {
  deptId?: number
  parentId: number
  deptName: string
  orderNum: number
  leader?: string
  phone?: string
  email?: string
  status: string
}

export interface DeptQuery {
  deptName?: string
  status?: string
}

export function listDept(query?: DeptQuery): Promise<DeptItem[]> {
  return request({
    url: '/system/dept/list',
    method: 'get',
    params: query
  })
}

export function addDept(data: DeptForm): Promise<void> {
  return request({
    url: '/system/dept',
    method: 'post',
    data
  })
}

export function updateDept(data: DeptForm): Promise<void> {
  return request({
    url: '/system/dept',
    method: 'put',
    data
  })
}

export function delDept(deptId: number): Promise<void> {
  return request({
    url: `/system/dept/${deptId}`,
    method: 'delete'
  })
}
