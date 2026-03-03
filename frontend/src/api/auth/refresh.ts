import axios from "axios"
import { ApiResponse } from ".."

export type RefreshTokenResponse = {
  accessToken: string
  refreshToken: string
}


const refreshClient = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

export async function refreshTokenApi(refreshToken: string): Promise<RefreshTokenResponse> {
  // 刷新 token 
  const response = await refreshClient({
    url: '/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
  const res = response.data as ApiResponse<RefreshTokenResponse>
  if(res.code === 200)return res.data
  
  throw new Error(res.message || '刷新 token 失败')
}

