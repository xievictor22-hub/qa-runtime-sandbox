import { useUserStore } from '@/stores/auth/user'
import { Client } from '@stomp/stompjs'
import { onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getWsUrl } from '@/utils/ws';

/**
 * WebSocket 报价单更新消息接口
 */
export interface QuoteUpdateMessage {
  quoteId: number;
  action: string;
  // 可能包含其他更新的字段
  status?: string;
  currentBusinessVersion?: number;
  currentQuoteVersion?: number;
  id?: number; // 兼容可能使用 id 字段的情况
}


/**
 * WebSocket 钩子函数
 * @description 提供 STOMP WebSocket 连接、订阅和断开功能
 */
export function useWebSocket() {
  const client = ref<Client | null>(null)
  const isConnected = ref(false)
  const userStore = useUserStore()

  /**
   * 初始化并连接 WebSocket
   * @param onConnect 连接成功后的回调
   * @param onError 连接错误后的回调
   * @returns void
   */
  const connect = (onConnect?: () => void, onError?: (error: any) => void) => {
    // 防止重复连接
    if (client.value?.active) return



    // 构建完整的 WebSocket URL
    const wsUrl = getWsUrl()
    
    console.log('[WebSocket] 连接地址:', wsUrl)

    // 检查是否有 token
    if (!userStore.token) {
      const errorMsg = 'WebSocket 连接失败：缺少认证 token'
      console.error(errorMsg)
      ElMessage.error(errorMsg)
      onError?.(new Error(errorMsg))
      return
    }

    client.value = new Client({
      brokerURL: wsUrl, // Spring Boot 的 endpoint
      
      // 关键：携带 Token 鉴权
      beforeConnect: async () => {
        client.value!.connectHeaders = {
        'Authorization': `Bearer ${userStore.token}`,
        'X-Device-Id': userStore.deviceId
        }
      },
      
      // 调试模式（生产环境可关闭）
      debug:  (str) => {
        console.log('[WS Debug]:', str)
      },
      
      // 自动重连配置 (企业级关键配置)
      reconnectDelay: 5000, // 断开后5秒重连
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      // 连接超时设置
      connectionTimeout: 10000,
    })

    // 连接成功回调
    client.value.onConnect = (frame) => {
      isConnected.value = true
      // ✅ 接收服务端系统消息（踢下线）
  client.value?.subscribe('/user/queue/system', (msg) => {
    try {
      const payload = JSON.parse(msg.body || '{}')
      if (payload.type === 'KICK') {
        // 1) 断开WS
        disconnect()

        // 2) 清理登录态（你项目里 logout 怎么写就用那个）
        userStore.logout?.()  // 或者 userStore.token = ''; userStore.refreshToken='';

        // 3) 提示 + 跳转
        // 你项目里如果有 element-plus message，就弹一下
        // ElMessage.warning('该账号已在本设备重新登录，你已被下线')
        // router.push('/login')
      }
    } catch (e) {
      console.warn('system msg parse error', e)
    }
  })

      console.log('WebSocket 连接成功')
      if (onConnect) onConnect()
    }

    // 错误处理
    client.value.onStompError = (frame) => {
      const errorMsg = frame.headers['message'] || '未知错误'
      const errorDetail = frame.body || ''
      console.error('WebSocket 错误:', errorMsg)
      console.error('错误详情:', errorDetail)
      ElMessage.error(`WebSocket 错误: ${errorMsg}`)
      onError?.(new Error(errorMsg))
    }

    // 连接断开处理
    client.value.onDisconnect = () => {
      isConnected.value = false
      console.log('WebSocket 连接已断开')
    }

    // 连接失败处理
    client.value.onWebSocketError = (error) => {
      console.error('WebSocket 连接失败:', error)
      ElMessage.error('WebSocket 连接失败，请检查网络')
      onError?.(error)
    }

    // 启动连接
    try {
      client.value.activate()
    } catch (error) {
      console.error('WebSocket 启动失败:', error)
      ElMessage.error('WebSocket 启动失败')
      onError?.(error)
    }
  }

/**
 * 订阅频道
 * @param destination 订阅地址 (如 /topic/quote/*)
 * @param callback 收到消息的回调，接收完整的消息对象
 * @returns 订阅对象，可用于取消订阅
 */
const subscribe = (destination: string, callback: (message: { body: QuoteUpdateMessage, destination: string }) => void) => {
  if (!client.value || !isConnected.value) {
    console.warn('WebSocket 未连接，无法订阅:', destination)
    return null
  }
  try {
    const subscription = client.value.subscribe(destination, (message) => {
      if (message.body) {
        try {
          // 传递完整的消息信息，包括 destination
          callback({
            body: JSON.parse(message.body),
            destination: message.headers.destination || ''
          })
        } catch (error) {
          console.error('WebSocket 消息处理失败:', error)
          ElMessage.error('WebSocket 消息处理失败')
        }
      }
    })
    
    console.log('WebSocket 订阅成功:', destination)
    return subscription
  } catch (error) {
    console.error('WebSocket 订阅失败:', error)
    ElMessage.error('WebSocket 订阅失败')
    return null
  }
}

  /**
   * 发送消息
   * @param destination 目标地址
   * @param body 消息体
   * @returns boolean 是否发送成功
   */
  const send = (destination: string, body: QuoteUpdateMessage): boolean => {
    if (!client.value || !isConnected.value) {
      console.warn('WebSocket 未连接，无法发送消息')
      ElMessage.warning('WebSocket 未连接，无法发送消息')
      return false
    }

    try {
      const messageBody = typeof body === 'string' ? body : JSON.stringify(body)
      client.value.publish({
        destination,
        body: messageBody
      })
      console.log('WebSocket 消息发送成功:', destination)
      return true
    } catch (error) {
      console.error('WebSocket 消息发送失败:', error)
      ElMessage.error('WebSocket 消息发送失败')
      return false
    }
  }

  /**
   * 断开连接 (通常组件销毁时调用)
   */
  const disconnect = async () => {
    if (client.value) {
      try {
        await client.value.deactivate()
        isConnected.value = false
        client.value = null
        console.log('WebSocket 连接已断开')
      } catch (error) {
        console.error('WebSocket 断开失败:', error)
      }
    }
  }

  // 组件卸载时自动断开，防止内存泄漏
  onUnmounted(() => {
    disconnect()
  })

  return {
    client,
    isConnected,
    connect,
    subscribe,
    send,
    disconnect
  }
}