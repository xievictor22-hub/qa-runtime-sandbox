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


/** 提交审核 */
export function submitAudit(data: ProcessDto) {
  return request({
    url: '/quote/process/submit',
    method: 'post',
    data
  })
}

/** 审核通过 */
export function auditPass(data: ProcessDto) {
  return request({
    url: '/quote/process/audit-pass',
    method: 'post',
    data
  })
}

/** 审核驳回 */
export function auditReject(data: ProcessDto) {
  return request({
    url: '/quote/process/audit-reject',
    method: 'post',
    data
  })
}

/** 创建新报价版本 (被驳回后) */
export function createNewVersion(quoteId: string) {
  return request({
    url: `/quote/process/${quoteId}/new-version`,
    method: 'post'
  })
}

/** 根据角色获取候选人列表 */
export function getProcessUsers(roleKey: string): Promise<ProcessUser[]> {
  return request({
    url: `/quote/process/users/${roleKey}`,
    method: 'get'
  })
}