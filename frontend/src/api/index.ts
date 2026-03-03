import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/auth/user' 
import { ElMessage, ElMessageBox } from 'element-plus'
import { authManager } from './auth/auth-manager'


// 定义后端返回的通用结构 (根据你的 ApiResponse 定义)
export interface ApiResponse<T=any> {
  code: number
  message: string
  data: T
}

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API, // 使用 vite.config.ts 中配置的代理 /api
  timeout: 10000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})


// --- 请求拦截器 ---
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    // 如果有 token，则添加到 headers 中
    config.headers['X-Device-Id'] = userStore.deviceId
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// --- 响应拦截器 ---
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 1. 特殊处理：如果是二进制流 (Blob)，直接返回
    if (response.config.responseType === 'blob' || response.data instanceof Blob) {
      return response.data;
    }
    // 后端返回的完整数据结构 { code, message, data, ... }
    const res = response.data as ApiResponse
    const { code, message, data } = res

    // 200 代表业务成功
    if (code === 200) {
      return data // 剥离外层，只返回 data
    } 

    if (code === 40001) {
      ElMessageBox.confirm('数据校验不通过，请下载错误报告修改', '导入失败', {
        confirmButtonText: '下载报告',
        cancelButtonText: '取消',
        type: 'error'
      }).then(() => {
        window.open(data) // 打开后端返回的 MinIO 链接
      }).catch(() => {
        // 用户点击取消，不做任何操作
      })
      return Promise.reject(new Error(message || 'Error'))
    }
    
    // 处理特定的业务错误（如 Token 过期 code === 401）
    // 这里简单处理，统一弹窗提示错误
    ElMessage.error(message || '系统错误')
    return Promise.reject(new Error(message || 'Error'))
  },
 async (error: any) => {
    // 处理 HTTP 网络错误 (如 404, 500)
    let msg = '网络异常'
    if (error.response){
      switch (error.response.status) {
        case 401:
          return authManager.handle401(error.config, service)
        case 403:
          msg = '没有权限访问该资源'
          break
        case 404:
          msg = '请求资源不存在'
          break
        case 500:
          msg = '服务器内部错误'
          break
        default:
          msg = error.response.statusText || '网络连接异常'
          break
      }
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service