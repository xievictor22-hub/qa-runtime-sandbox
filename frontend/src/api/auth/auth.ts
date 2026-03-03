import request from '@/api/index'
import type { LoginData, LoginResult } from '@/types/auth/auth' // 下面定义类型

/**
 * 登录接口
 */
export function loginApi(data: LoginData) {
  // 泛型指定返回数据的类型
  return request<any, LoginResult>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

