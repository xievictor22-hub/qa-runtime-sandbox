// 登录请求参数
export interface LoginData {
  username: string
  password: string
}

// 登录响应结果 (对应后端 Map<String, Object> 中的字段)
export interface LoginResult {
  token: string
  tokenHead: string
  refreshToken: string // 新增：刷新令牌
  permissions?: string[]
  userInfo: {
    username: string
    nickname: string
    id: number // 新增：用户 ID
    avatar?: string
    roles?: string[]
  }
} 