import request from '@/api/index'
import type { LoginData, LoginResult } from '@/types/auth/auth' // 下面定义类型

/**
 * 登录接口
 */
export function loginApi(data: LoginData): Promise<LoginResult> {
  return request<LoginResult>({
    url: '/auth/login',
    method: 'post',
    data
  })
}
