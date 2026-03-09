import request from '@/api/index'

// 上传文件
export function uploadFile(data: FormData): Promise<string> {
  return request({
    url: '/system/ding-drive/upload',
    method: 'post',
    data: data,
    headers: {
      // Axios 通常会自动处理 multipart/form-data 的 boundary，
      // 但显式声明是个好习惯，或者留空让浏览器自动推断
      'Content-Type': 'multipart/form-data' 
    }
  })
}
