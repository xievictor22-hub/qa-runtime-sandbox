import axios from 'axios'

type ClientErrorInput = {
  type: string
  message: string
  stack?: string
  info?: string
  level?: 'error' | 'warn' | 'info'
  extra?: Record<string, unknown> | string
}

type ClientErrorReportDto = {
  appName: string
  env: string
  level: string
  errorType: string
  message: string
  stack?: string
  pageUrl?: string
  routePath?: string
  deviceId?: string
  traceId?: string
  extra?: string
  occurTime: number
  appKey: string
  timestamp: number
  sign: string
}

const REPORT_PATH = '/monitor/client-error/report'
const DEVICE_ID_KEY = 'client-error-device-id'

const reportClient = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' }
})

let isReporting = false
let warnedMissingConfig = false

function safeStringify(value: unknown): string {
  try {
    return typeof value === 'string' ? value : JSON.stringify(value)
  } catch {
    return '[Unserializable value]'
  }
}

function getDeviceId() {
  const existing = localStorage.getItem(DEVICE_ID_KEY)
  if (existing) return existing

  const next = typeof crypto !== 'undefined' && 'randomUUID' in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`

  localStorage.setItem(DEVICE_ID_KEY, next)
  return next
}

async function hmacSha256Hex(payload: string, secret: string): Promise<string> {
  if (!crypto?.subtle) {
    throw new Error('crypto.subtle unavailable')
  }
  const enc = new TextEncoder()
  const key = await crypto.subtle.importKey(
    'raw',
    enc.encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  )
  const sig = await crypto.subtle.sign('HMAC', key, enc.encode(payload))
  return Array.from(new Uint8Array(sig))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

async function buildPayload(input: ClientErrorInput): Promise<ClientErrorReportDto | null> {
  const appKey = (import.meta.env.VITE_CLIENT_ERROR_APP_KEY || '').trim()
  const appSecret = (import.meta.env.VITE_CLIENT_ERROR_APP_SECRET || '').trim()

  if (!appKey || !appSecret) {
    if (!warnedMissingConfig) {
      console.warn('[client-error] missing VITE_CLIENT_ERROR_APP_KEY / VITE_CLIENT_ERROR_APP_SECRET, skip reporting')
      warnedMissingConfig = true
    }
    return null
  }

  const now = Date.now()
  const timestamp = Math.floor(now / 1000)

  const dto: Omit<ClientErrorReportDto, 'sign'> = {
    appName: (import.meta.env.VITE_APP_NAME || 'mogo-frontend').slice(0, 64),
    env: (import.meta.env.MODE || 'unknown').slice(0, 32),
    level: (input.level || 'error').slice(0, 16),
    errorType: (input.type || 'unknown').slice(0, 64),
    message: String(input.message || 'unknown error').slice(0, 1000),
    stack: input.stack?.slice(0, 5000),
    pageUrl: location.href.slice(0, 1000),
    routePath: (location.pathname + location.search + location.hash).slice(0, 255),
    deviceId: getDeviceId().slice(0, 128),
    traceId: undefined,
    extra: input.extra ? safeStringify(input.extra) : (input.info ? safeStringify({ info: input.info }) : undefined),
    occurTime: now,
    appKey,
    timestamp
  }

  const signPayload = [
    dto.appKey,
    String(dto.timestamp),
    dto.appName,
    dto.env,
    dto.level,
    dto.errorType,
    dto.message
  ].join('|')

  const sign = await hmacSha256Hex(signPayload, appSecret)
  return { ...dto, sign }
}

/**
 * 上报前端错误（内部吞错，防止因为上报失败引发二次错误）
 */
export async function reportClientError(input: ClientErrorInput): Promise<void> {
  if (isReporting) return
  if (!input?.message) return

  isReporting = true
  try {
    const payload = await buildPayload(input)
    if (!payload) return

    await reportClient.post(REPORT_PATH, payload)
  } catch (error) {
    console.warn('[client-error] report failed:', error)
  } finally {
    isReporting = false
  }
}
