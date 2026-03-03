import request from '@/api/index'

// 定义基础路径
const BASE_URL = '/system/user'

// --- 类型定义 (也可以移到 src/types/system.d.ts) ---
export interface UserQuery {
  pageNum: number
  pageSize: number
  username?: string
  deptId?: number // 新增部门ID字段
  phone?: string
  status?: number
}

export interface UserForm {
  id?: number
  deptId?: number // 新增部门ID字段
  username: string
  nickname: string
  password?: string // 新增时必填，修改时选填
  phone?: string
  deptNamePath?: string // 新增部门路径字段
  email?: string
  status: number
  remark?: string
}

// --- 接口函数 ---

/** 获取用户分页列表 */
export function getUserList(params: UserQuery) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params
  })
}

/** 新增用户 */
export function addUser(data: UserForm) {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

/** 修改用户 */
export function updateUser(data: UserForm) {
  return request({
    url: BASE_URL,
    method: 'put',
    data
  })
}

/** 删除用户 */
export function deleteUser(id: number) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'delete'
  })
}

/** 批量删除 */
export function deleteBatchUser(ids: number[]) {
  return request({
    url: `${BASE_URL}/batch`,
    method: 'delete',
    data: ids
  })
}

/** 获取用户已分配的角色ID */
export function getUserRoleIds(userId: number) {
  return request({
    url: `${BASE_URL}/${userId}/role`,
    method: 'get'
  })
}

/** 提交角色分配 */
export function assignUserRoles(userId: number, roleIds: number[]) {
  return request({
    url: `${BASE_URL}/${userId}/role`,
    method: 'put',
    data: roleIds
  })
}

// 获取用户精简列表
export function listUserOption() {
  return request({
    url: `${BASE_URL}/listAll`, // 你的后端接口地址
    method: 'get'
  })
}

/**
 * 导出用户数据
 * 注意：必须指定 responseType 为 'blob'
 */
export function exportUserApi(params: any) {
  return request({
    url: '/system/user/export',
    method: 'get',
    params,
    responseType: 'blob' // 关键！
  });
}

/**
 * 导入用户数据
 * @param file 文件对象
 */
export function importUserApi(file: File) {
  const formData = new FormData();
  formData.append('file', file); // 'file' 必须与后端 @RequestPart("file") 名字一致

  return request({
    url: '/system/user/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data' // 浏览器会自动识别，但显式写出更清晰
    }
  });
}