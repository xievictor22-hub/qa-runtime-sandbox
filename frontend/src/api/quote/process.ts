import request from '@/api/index'

function createIdempotencyKey(action: string, quoteId: string) {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${action}:${quoteId}:${random}`
}

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


/** 提交审核 */
export function submitAudit(data: ProcessDto) {
  return request({
    url: '/quote/process/submit',
    method: 'post',
    data,
    headers: {
      'X-Idempotency-Key': createIdempotencyKey('submit', data.id)
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
      'X-Idempotency-Key': createIdempotencyKey('audit-pass', data.id)
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
      'X-Idempotency-Key': createIdempotencyKey('audit-reject', data.id)
    }
  })
}

/** 创建新报价版本 (被驳回后) */
export function createNewVersion(quoteId: string) {
  return request({
    url: `/quote/process/${quoteId}/new-version`,
    method: 'post',
    headers: {
      'X-Idempotency-Key': createIdempotencyKey('new-version', quoteId)
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
