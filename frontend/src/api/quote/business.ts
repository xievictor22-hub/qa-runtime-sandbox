import request from '@/api/index'

/** 获取业务调整明细 (带产品名) */
export function listBusinessItems(quoteId: string) {
  return request({
    url: `/quote/business/${quoteId}/items`,
    method: 'get'
  })
}

/** 批量保存业务价格 */
export function saveBusinessItems(data: any[]) {
  return request({
    url: '/quote/business/items/batch-save',
    method: 'put',
    data
  })
}

/** 业务确认完成 */
export function finishBusiness(data: { id: string; auditComment?: string }) {
  return request({
    url: '/quote/business/finish',
    method: 'post',
    data
  })
}

/** 重新调整 (重开) */
export function reAdjustBusiness(quoteId: string) {
  return request({
    url: `/quote/business/re-adjust/${quoteId}`,
    method: 'post'
  })
}