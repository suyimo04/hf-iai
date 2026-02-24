import { Client, IMessage, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getToken } from './auth'

// WebSocket 连接状态
export type ConnectionState = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'RECONNECTING'

// AI面试消息类型
export interface InterviewWebSocketMessage {
  type: 'AI_RESPONSE' | 'SESSION_END' | 'ERROR' | 'TYPING'
  sessionId: number
  content?: string
  data?: any
}

// 连接状态变化回调
type ConnectionStateCallback = (state: ConnectionState) => void

class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, StompSubscription> = new Map()
  private connectionState: ConnectionState = 'DISCONNECTED'
  private stateCallbacks: Set<ConnectionStateCallback> = new Set()
  private reconnectAttempts = 0
  private maxReconnectAttempts = 10

  /**
   * 连接 WebSocket
   */
  connect(token?: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.client?.connected) {
        resolve()
        return
      }

      const authToken = token || getToken()
      if (!authToken) {
        reject(new Error('未登录，无法建立WebSocket连接'))
        return
      }

      this.setConnectionState('CONNECTING')

      this.client = new Client({
        // 使用 SockJS 工厂
        webSocketFactory: () => {
          return new SockJS('/ws')
        },

        // SockJS 兼容性配置
        forceBinaryWSFrames: false,
        splitLargeFrames: true,
        maxWebSocketChunkSize: 8 * 1024,

        // 连接头 - 携带认证信息
        connectHeaders: {
          Authorization: `Bearer ${authToken}`
        },

        // 重连配置
        reconnectDelay: 3000,
        maxReconnectDelay: 30000,

        // 心跳配置
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,

        // 连接成功
        onConnect: () => {
          console.log('[WebSocket] 连接成功')
          this.reconnectAttempts = 0
          this.setConnectionState('CONNECTED')
          resolve()
        },

        // STOMP 错误
        onStompError: (frame) => {
          console.error('[WebSocket] STOMP错误:', frame.headers['message'])
          reject(new Error(frame.headers['message'] || 'STOMP连接错误'))
        },

        // WebSocket 关闭
        onWebSocketClose: (event) => {
          console.log('[WebSocket] 连接关闭, code:', event.code)
          if (this.connectionState !== 'DISCONNECTED') {
            this.setConnectionState('RECONNECTING')
          }
        },

        // WebSocket 错误
        onWebSocketError: (event) => {
          console.error('[WebSocket] 连接错误:', event)
        },

        // 心跳丢失
        onHeartbeatLost: () => {
          console.warn('[WebSocket] 心跳丢失')
        },

        // 调试日志（生产环境可关闭）
        debug: (msg) => {
          if (import.meta.env.DEV) {
            console.debug('[WebSocket]', msg)
          }
        }
      })

      this.client.activate()
    })
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    if (this.client) {
      // 清理所有订阅
      this.subscriptions.forEach((sub, id) => {
        try {
          sub.unsubscribe()
        } catch (e) {
          console.warn('[WebSocket] 取消订阅失败:', id)
        }
      })
      this.subscriptions.clear()

      // 断开连接
      this.client.deactivate()
      this.client = null
      this.setConnectionState('DISCONNECTED')
      console.log('[WebSocket] 已断开连接')
    }
  }

  /**
   * 订阅目标
   */
  subscribe(destination: string, callback: (message: any) => void): string {
    if (!this.client?.connected) {
      console.error('[WebSocket] 未连接，无法订阅')
      return ''
    }

    const subscriptionId = `sub-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`

    const subscription = this.client.subscribe(
      destination,
      (message: IMessage) => {
        try {
          const body = message.body ? JSON.parse(message.body) : null
          callback(body)
        } catch (e) {
          // 非 JSON 消息，直接传递原始内容
          callback(message.body)
        }
      },
      { id: subscriptionId }
    )

    this.subscriptions.set(subscriptionId, subscription)
    console.log('[WebSocket] 订阅成功:', destination, subscriptionId)

    return subscriptionId
  }

  /**
   * 取消订阅
   */
  unsubscribe(subscriptionId: string): void {
    const subscription = this.subscriptions.get(subscriptionId)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(subscriptionId)
      console.log('[WebSocket] 取消订阅:', subscriptionId)
    }
  }

  /**
   * 发送消息
   */
  send(destination: string, body: any): void {
    if (!this.client?.connected) {
      console.error('[WebSocket] 未连接，无法发送消息')
      return
    }

    this.client.publish({
      destination,
      body: typeof body === 'string' ? body : JSON.stringify(body),
      headers: { 'content-type': 'application/json' }
    })
  }

  // ==================== AI面试专用方法 ====================

  /**
   * 订阅AI面试会话消息
   */
  subscribeToInterview(
    sessionId: number,
    onMessage: (msg: InterviewWebSocketMessage) => void
  ): string {
    // 订阅用户专属队列
    const destination = `/user/queue/ai-interview/${sessionId}`
    return this.subscribe(destination, onMessage)
  }

  /**
   * 发送面试消息
   */
  sendInterviewMessage(sessionId: number, content: string): void {
    const destination = `/app/ai-interview/${sessionId}/message`
    this.send(destination, { content })
  }

  /**
   * 发送面试开始信号
   */
  startInterview(sessionId: number): void {
    const destination = `/app/ai-interview/${sessionId}/start`
    this.send(destination, {})
  }

  /**
   * 发送面试结束信号
   */
  endInterview(sessionId: number): void {
    const destination = `/app/ai-interview/${sessionId}/end`
    this.send(destination, {})
  }

  // ==================== 状态管理 ====================

  /**
   * 获取当前连接状态
   */
  getConnectionState(): ConnectionState {
    return this.connectionState
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.client?.connected ?? false
  }

  /**
   * 监听连接状态变化
   */
  onStateChange(callback: ConnectionStateCallback): () => void {
    this.stateCallbacks.add(callback)
    // 返回取消监听函数
    return () => {
      this.stateCallbacks.delete(callback)
    }
  }

  /**
   * 设置连接状态并通知监听者
   */
  private setConnectionState(state: ConnectionState): void {
    this.connectionState = state
    this.stateCallbacks.forEach((cb) => cb(state))
  }
}

// 导出单例
export const wsService = new WebSocketService()
