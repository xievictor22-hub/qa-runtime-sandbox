import request from '@/api/index'

// 获取个人信息
export function getUserProfile() {
  return request({
    url: '/system/user/profile',
    method: 'get'
  })
}

// 修改基本信息
export function updateUserProfile(data: any) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data
  })
}



// 修改密码
export function updateUserPwd(oldPassword: string, newPassword: string) {
  return request({
    url: '/system/user/profile/password',
    method: 'put',
    params: { oldPassword, newPassword }
  })
}

// 修改头像 (保存 URL 到用户表)
export function updateUserAvatar(avatar: string) {
  return request({
    url: '/system/user/profile/avatar',
    method: 'put',
    params: { avatar }
  })
}