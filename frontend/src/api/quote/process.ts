import request from '@/api/index'

export interface ProcessDto {
  id: string // quoteId
  nextHandlerId?: number
  auditComment?: string
}
export interface ProcessUser {
  id: string
  nickname: string
  roleNames: string
  username: string
}
// 生成幂等键
function createIdempotencyKey(action: string, quoteId: string) {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${action}:${quoteId}:${random}`
}



/** 提交审核 */
export function submitAudit(data: ProcessDto) {
  return request({
    url: '/quote/process/submit',
    method: 'post',
    data,
    headers: {
      'Idempotency-Key': createIdempotencyKey('submitAudit', data.id)
    }
  })
}

/** 审核通过 */
export function auditPass(data: ProcessDto) {
  return request({
    url: '/quote/process/audit-pass',
    method: 'post',
    data,
    headers: {
      'Idempotency-Key': createIdempotencyKey('auditPass', data.id)
    }
  })
}

/** 审核驳回 */
export function auditReject(data: ProcessDto) {
  return request({
    url: '/quote/process/audit-reject',
    method: 'post',
    data,
    headers: {
      'Idempotency-Key': createIdempotencyKey('auditReject', data.id)
    }
  })
}

/** 创建新报价版本 (被驳回后) */
export function createNewVersion(quoteId: string) {
  return request({
    url: `/quote/process/${quoteId}/new-version`,
    method: 'post',
    headers: {
      'Idempotency-Key': createIdempotencyKey('createNewVersion', quoteId)
    }
  })
}

/** 根据角色获取候选人列表 */
export function getProcessUsers(roleKey: string): Promise<ProcessUser[]> {
  return request({
    url: `/quote/process/users/${roleKey}`,
    method: 'get'
  })
}