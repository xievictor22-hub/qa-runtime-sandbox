import request from '@/api/index'
/**
 * 导入参数接口
 */
export interface ImportQuoteParams {
  version: string
  description?: string
}

/**
 * 上传 Excel
 */
export function uploadQuoteExcel(file: File, params: ImportQuoteParams) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('version', params.version)
  if (params.description) {
    formData.append('description', params.description)
  }

  return request({
    url: '/api/quote/import/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data' // 必须显式指定
    }
  })
}