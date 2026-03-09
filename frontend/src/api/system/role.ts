import request from '@/api/index'
import type { PageResult } from '@/api/types'

// 基础路径
const BASE_URL = '/system/role'

// --- 类型定义 ---
export interface RoleQuery {
  pageNum: number
  pageSize: number
  roleName?: string
  roleKey?: string
  status?: number
}

export interface RoleForm {
  id?: number
  roleName: string
  roleKey: string
  sort: number
  status: number
  remark?: string
  dataScope?: string // 新增
  deptIds?: number[] // 新增
}

export interface RoleOption {
  id: number
  roleName: string
  roleKey: string
}

// --- 接口函数 ---

/** 获取角色分页列表 */
export function getRoleList(params: RoleQuery): Promise<PageResult<RoleForm>> {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

/** 获取所有角色 (用于下拉列表) */
export function getAllRoles(): Promise<RoleOption[]> {
  return request({
    url: `${BASE_URL}/all`,
    method: 'get'
  })
}

/** 新增角色 */
export function addRole(data: RoleForm): Promise<void> {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

/** 修改角色 */
export function updateRole(data: RoleForm): Promise<void> {
  return request({
    url: BASE_URL,
    method: 'put',
    data
  })
}

/** 删除角色 */
export function deleteRole(id: number): Promise<void> {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'delete'
  })
}

/** 获取角色已分配的菜单ID */
export function getRoleMenuIds(roleId: number): Promise<number[]> {
  return request({
    url: `${BASE_URL}/${roleId}/menu`,
    method: 'get'
  })
}

/** 提交角色菜单权限 */
export function assignRoleMenus(roleId: number, menuIds: number[]): Promise<void> {
  return request({
    url: `${BASE_URL}/${roleId}/menu`,
    method: 'put',
    data: menuIds
  })
}

/**
 * 获取角色绑定的部门ID列表 (用于自定义权限回显)
 * 对应后端: @GetMapping("/deptIds/{roleId}")
 */
export function getRoleDeptIds(roleId: number): Promise<number[]> {
  return request({
    url: '/system/role/deptIds/' + roleId,
    method: 'get'
  })
}
