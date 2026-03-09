import request from '@/api/index'

// --- 类型定义 ---
export interface QuoteOrderQuery {
  pageNum?: number
  pageSize?: number
  projectName?: string
  status?: string
  queryType?: 'todo' | 'all'
}

export interface QuoteOrderSearchCondition {
  projectName?: string;
  customerName?: string;
  projectType?: number;
}

/**
 * 报价单接口
 */
export interface QuoteOrder {
  id: string;
  projectName: string;
  customerName: string;
  status: string;
  currentQuoteVersion: number;
  currentBusinessVersion: number;
  createBy: number;
  createTime: string;
  updateBy: number | null;
  updateTime: string;
  auditComment?: string;
  deleteFlag?: number;
  deptId?: number | null;
  finalTotalPrice?: number;
  nextHandlerId?: number | null;
  otherFeesJson?: string | null;
  params?: Record<string, unknown>;
  projectCode?: string | null;
  projectType?: string | null;
  remark?: string | null;
  taxRate?: number | null;
  totalInstallPrice?: number;
  totalMaterialPrice?: number;
}
export interface QuoteSummary {
  category: string;//费用类别
  salesTotal: number;//销售总价
  productDiscount: number;//产品折扣率
  otherCostTotal: number;//其他费用
  systemTotal: number;//系统项目总价
  contractPrice: number;//客户合同总价
}

// 报价单履历日志
export interface QuoteLog {
  id: string;
  quoteId: string;
  action: string; // 例如: "CREATE", "SUBMIT_AUDIT" 等
  operatorId: number | null;
  operatorName: string | null;
  receiverId: number | null;
  receiverName: string | null;
  comment: string;
  createTime: string; // ISO 格式的时间字符串
}

// --- 接口方法 ---

/** 获取报价单摘要信息 */
export function getQuoteSummary(id: string): Promise<QuoteSummary> {
  return request({
    url: `/quote/order/summary/${id}`,
    method: 'get'
  })
}


/** 获取报价单列表 */
export interface QuoteOrderListResult {
  records: QuoteOrder[]
  total?: number
  pageNum?: number
  pageSize?: number
}

export function listQuoteOrder(params: QuoteOrderQuery): Promise<QuoteOrderListResult> {
  return request({
    url: '/quote/order/list',
    method: 'get',
    params
  })
}

/** 获取报价单列表 */
export function queryQuoteOrder(id:string):Promise<QuoteOrder>  {
  return request({
    url: `/quote/order/query/${id}`,
    method: 'get',
  })
}

/** 获取报价单详情 */
export function getQuoteOrder(id: string): Promise<QuoteOrder> {
  return request({
    url: `/quote/order/${id}`,
    method: 'get'
  })
}



/** 创建报价单 (草稿) */
export function createQuoteOrder(data: QuoteOrderSearchCondition): Promise<QuoteOrder> {
  return request({
    url: '/quote/order/create',
    method: 'post',
    data
  })
}

/** 修改报价单基础信息 */
export function updateQuoteOrder(data: QuoteOrder): Promise<void> {
  return request({
    url: '/quote/order/update',
    method: 'put',
    data
  })
}

/** 删除报价单 (仅草稿) */
export function deleteQuoteOrder(id: string): Promise<void> {
  return request({
    url: `/quote/order/${id}`,
    method: 'delete'
  })
}


/** 获取履历日志 */
export function getQuoteLogs(id: string): Promise<QuoteLog[]> {
  return request({
    url: `/quote/order/${id}/logs`,
    method: 'get'
  })
}

/** 导出报价单 */
export function exportQuoteOrder(id: string, columns: string[]): Promise<Blob> {
  return request({
    url: `/quote/order/export/${id}`,
    method: 'post',
    data: columns,
    responseType: 'blob' // 关键：二进制流
  })
}
