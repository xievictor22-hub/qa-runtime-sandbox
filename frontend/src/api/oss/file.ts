import request from '@/api/index'

/**
 * 通用上传接口
 * @param data FormData 对象 (包含 file)
 */
export function uploadFile(data: FormData) {
  return request({
    url: '/oss/file/upload',
    method: 'post',
    headers: { 'Content-Type': 'multipart/form-data' }, // 必须指定
    data
  })
}