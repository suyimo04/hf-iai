<template>
  <div class="interview-chat">
    <!-- 头部信息 -->
    <div class="chat-header">
      <div class="session-info">
        <span class="progress">面试进度: {{ session?.roundCount || 0 }} / {{ session?.maxRounds || 15 }} 轮</span>
        <el-tag
          v-for="type in session?.violationTypes"
          :key="type"
          size="small"
          class="violation-tag"
        >
          {{ getViolationLabel(type) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-tag :type="connectionStatusType" size="small">
          {{ connectionStatusText }}
        </el-tag>
        <el-button
          v-if="session?.status === 'IN_PROGRESS'"
          type="danger"
          size="small"
          @click="handleEndInterview"
        >
          结束面试
        </el-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <div v-if="messages.length === 0 && !loading" class="empty-tip">
        暂无消息，等待面试开始...
      </div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message', msg.role.toLowerCase()]"
      >
        <div class="avatar">{{ msg.role === 'USER' ? '管理' : 'AI' }}</div>
        <div class="content">
          <div class="text">{{ msg.content }}</div>
          <div class="time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>
      <!-- 流式消息 -->
      <div v-if="streamingContent" class="message ai">
        <div class="avatar">AI</div>
        <div class="content">
          <div class="text">{{ streamingContent }}<span class="cursor">|</span></div>
        </div>
      </div>
      <!-- 加载中 -->
      <div v-if="loading" class="loading-tip">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入消息... (Ctrl+Enter 发送)"
        :disabled="!canSend"
        @keyup.enter.ctrl="handleSendMessage"
        resize="none"
      />
      <el-button
        type="primary"
        :disabled="!canSend || !inputMessage.trim()"
        :loading="sending"
        @click="handleSendMessage"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  wsService,
  type ConnectionState,
  type InterviewWebSocketMessage
} from '@/utils/websocket'
import {
  getSessionMessages,
  getAIInterviewSession,
  endAIInterview,
  getViolationLabel,
  type AIInterviewSession,
  type AIInterviewMessage
} from '@/api/aiInterview'

// Props
const props = defineProps<{
  sessionId: number
}>()

// Emits
const emit = defineEmits<{
  (e: 'session-end', session: AIInterviewSession): void
  (e: 'error', error: Error): void
}>()

// Refs
const messageListRef = ref<HTMLElement | null>(null)
const inputMessage = ref('')
const messages = ref<AIInterviewMessage[]>([])
const session = ref<AIInterviewSession | null>(null)
const streamingContent = ref('')
const loading = ref(false)
const sending = ref(false)
const connectionState = ref<ConnectionState>('DISCONNECTED')

// WebSocket 订阅 ID
let subscriptionId = ''
let stateUnsubscribe: (() => void) | null = null

// Computed
const connectionStatusType = computed(() => {
  const map: Record<ConnectionState, 'success' | 'warning' | 'danger' | 'info'> = {
    CONNECTED: 'success',
    CONNECTING: 'warning',
    RECONNECTING: 'warning',
    DISCONNECTED: 'danger'
  }
  return map[connectionState.value] || 'info'
})

const connectionStatusText = computed(() => {
  const map: Record<ConnectionState, string> = {
    CONNECTED: '已连接',
    CONNECTING: '连接中',
    RECONNECTING: '重连中',
    DISCONNECTED: '未连接'
  }
  return map[connectionState.value] || '未知'
})

const canSend = computed(() => {
  return (
    session.value?.status === 'IN_PROGRESS' &&
    connectionState.value === 'CONNECTED' &&
    !sending.value &&
    !streamingContent.value
  )
})

// Methods
function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

async function fetchSessionData() {
  if (!props.sessionId) return

  loading.value = true
  try {
    const [sessionRes, messagesRes] = await Promise.all([
      getAIInterviewSession(props.sessionId),
      getSessionMessages(props.sessionId)
    ])
    session.value = sessionRes.data.data
    messages.value = messagesRes.data.data || []
    scrollToBottom()
  } catch (error) {
    console.error('获取会话数据失败:', error)
    emit('error', error as Error)
  } finally {
    loading.value = false
  }
}

async function connectWebSocket() {
  try {
    // 连接 WebSocket
    if (!wsService.isConnected()) {
      await wsService.connect()
    }

    // 订阅面试消息
    subscriptionId = wsService.subscribeToInterview(
      props.sessionId,
      handleWebSocketMessage
    )

    connectionState.value = wsService.getConnectionState()
  } catch (error) {
    console.error('WebSocket 连接失败:', error)
    ElMessage.error('实时连接失败，请刷新页面重试')
  }
}

function handleWebSocketMessage(msg: InterviewWebSocketMessage) {
  console.log('[InterviewChat] 收到消息:', msg)

  switch (msg.type) {
    case 'AI_RESPONSE':
      // AI 完整回复
      if (msg.content) {
        streamingContent.value = ''
        const aiMessage: AIInterviewMessage = {
          id: Date.now(),
          sessionId: props.sessionId,
          role: 'AI',
          content: msg.content,
          sequenceNumber: messages.value.length + 1,
          createdAt: new Date().toISOString()
        }
        messages.value.push(aiMessage)
        scrollToBottom()

        // 更新轮次
        if (session.value) {
          session.value.roundCount = (session.value.roundCount || 0) + 1
        }
      }
      break

    case 'TYPING':
      // 流式输出
      if (msg.content) {
        streamingContent.value += msg.content
        scrollToBottom()
      }
      break

    case 'SESSION_END':
      // 会话结束
      if (session.value) {
        session.value.status = 'COMPLETED'
      }
      ElMessage.success('面试已结束')
      if (msg.data) {
        emit('session-end', msg.data as AIInterviewSession)
      }
      break

    case 'ERROR':
      // 错误消息
      ElMessage.error(msg.content || '发生错误')
      break
  }
}

async function handleSendMessage() {
  const content = inputMessage.value.trim()
  if (!content || !canSend.value) return

  sending.value = true
  try {
    // 添加用户消息到列表
    const userMessage: AIInterviewMessage = {
      id: Date.now(),
      sessionId: props.sessionId,
      role: 'USER',
      content,
      sequenceNumber: messages.value.length + 1,
      createdAt: new Date().toISOString()
    }
    messages.value.push(userMessage)
    inputMessage.value = ''
    scrollToBottom()

    // 通过 WebSocket 发送
    wsService.sendInterviewMessage(props.sessionId, content)
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
    // 移除失败的消息
    messages.value.pop()
    inputMessage.value = content
  } finally {
    sending.value = false
  }
}

async function handleEndInterview() {
  try {
    await ElMessageBox.confirm(
      '确定要结束本次面试吗？结束后将无法继续对话。',
      '结束面试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await endAIInterview(props.sessionId)
    session.value = res.data.data
    ElMessage.success('面试已结束')
    emit('session-end', res.data.data)
  } catch (error) {
    if ((error as any) !== 'cancel') {
      console.error('结束面试失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

function cleanup() {
  // 取消订阅
  if (subscriptionId) {
    wsService.unsubscribe(subscriptionId)
    subscriptionId = ''
  }

  // 取消状态监听
  if (stateUnsubscribe) {
    stateUnsubscribe()
    stateUnsubscribe = null
  }
}

// Lifecycle
onMounted(async () => {
  // 监听连接状态
  stateUnsubscribe = wsService.onStateChange((state) => {
    connectionState.value = state
  })
  connectionState.value = wsService.getConnectionState()

  // 获取会话数据
  await fetchSessionData()

  // 连接 WebSocket
  if (session.value?.status === 'IN_PROGRESS') {
    await connectWebSocket()
  }
})

onUnmounted(() => {
  cleanup()
})

// Watch sessionId changes
watch(() => props.sessionId, async (newId, oldId) => {
  if (newId !== oldId && newId) {
    cleanup()
    messages.value = []
    streamingContent.value = ''
    await fetchSessionData()
    if (session.value?.status === 'IN_PROGRESS') {
      await connectWebSocket()
    }
  }
})
</script>

<style scoped>
.interview-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 400px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.session-info .progress {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.violation-tag {
  margin-left: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.empty-tip,
.loading-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.ai .avatar {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
}

.message.user .avatar {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: #fff;
}

.message.system .avatar {
  background: #909399;
  color: #fff;
}

.message .content {
  max-width: 70%;
}

.message .text {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message.ai .text {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px 8px 8px 0;
}

.message.user .text {
  background: #409eff;
  color: #fff;
  border-radius: 8px 8px 0 8px;
}

.message.system .text {
  background: #f4f4f5;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

.message .time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message.user .time {
  text-align: right;
}

.cursor {
  animation: blink 1s infinite;
  color: #409eff;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

.input-area :deep(.el-textarea__inner) {
  resize: none;
}

.input-area .el-button {
  align-self: flex-end;
  height: 54px;
}
</style>
