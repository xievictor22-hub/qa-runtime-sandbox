import request from '@/api/index'

export interface UserProfile {
  userId: number
  username: string
  nickname: string
  avatar?: string
  email?: string
  phone?: string
  deptId?: number
  deptName?: string
  roleIds?: number[]
}

export interface UserProfileUpdatePayload {
  nickname: string
  email?: string
  phone?: string
  sex?: string
}

// 获取个人信息
export function getUserProfile(): Promise<UserProfile> {
  return request({
    url: '/system/user/profile',
    method: 'get'
  })
}

// 修改基本信息
export function updateUserProfile(data: UserProfileUpdatePayload): Promise<void> {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}



// 修改密码
export function updateUserPwd(oldPassword: string, newPassword: string): Promise<void> {
  return request({
    url: '/system/user/profile/password',
    method: 'put',
    params: { oldPassword, newPassword }
  })
}

// 修改头像 (保存 URL 到用户表)
export function updateUserAvatar(avatar: string): Promise<void> {
  return request({
    url: '/system/user/profile/avatar',
    method: 'put',
    params: { avatar }
  })
}
