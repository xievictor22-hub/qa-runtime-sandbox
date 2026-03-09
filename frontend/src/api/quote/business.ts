import request from '@/api/index'

export interface QuoteProcessPayload {
  id: string
  auditComment?: string
}

export interface QuoteBusinessItem {
  id: string
  quoteId: string
  detailId: string
  businessVersion: number
  productName?: string
  spec?: string
  quantity?: number
  rowNum?: number
  factoryTotal?: number
  installTotal?: number
  salesUnitPrice?: number
  factoryProfitRate?: number
  customerFactoryTotal?: number
  installProfitRate?: number
  customerInstallTotal?: number
  salesPoint?: number
  customerUnitPrice?: number
  customerTotalPrice?: number
  salesUnitAdjustAmount?: number
  remark?: string
}

/** 获取业务调整明细 (带产品名) */
export function listBusinessItems(quoteId: string): Promise<QuoteBusinessItem[]> {
  return request({
    url: `/quote/business/${quoteId}/items`,
    method: 'get'
  })
}

/** 批量保存业务价格 */
export function saveBusinessItems(data: QuoteBusinessItem[]): Promise<void> {
  return request({
    url: '/quote/business/items/batch-save',
    method: 'put',
    data
  })
}

/** 业务确认完成 */
export function finishBusiness(data: QuoteProcessPayload): Promise<void> {
  return request({
    url: '/quote/business/finish',
    method: 'post',
    data
  })
}

/** 重新调整 (重开) */
export function reAdjustBusiness(quoteId: string): Promise<void> {
  return request({
    url: `/quote/business/re-adjust/${quoteId}`,
    method: 'post'
  })
}
