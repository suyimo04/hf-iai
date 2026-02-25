<template>
  <div :class="['message-bubble', role.toLowerCase(), { streaming }]">
    <!-- 头像 -->
    <div class="avatar">
      <el-avatar :size="36" :style="avatarStyle">
        {{ role === 'USER' ? '管' : 'AI' }}
      </el-avatar>
    </div>

    <!-- 消息内容 -->
    <div class="bubble-content">
      <div class="bubble-header">
        <span class="role-name">{{ roleName }}</span>
        <span class="time">{{ formatTime(createdAt) }}</span>
      </div>
      <div class="bubble-body">
        <div class="text" v-html="formattedContent"></div>
        <span v-if="streaming" class="cursor">|</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

interface Props {
  role: 'USER' | 'AI' | 'SYSTEM'
  content: string
  createdAt?: string
  streaming?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  streaming: false
})

const roleName = computed(() => {
  return props.role === 'USER' ? '管理员' : props.role === 'AI' ? '群成员' : '系统'
})

const avatarStyle = computed(() => ({
  backgroundColor: props.role === 'USER' ? '#10b981' : '#6366f1'
}))

const formattedContent = computed(() => {
  // 使用 DOMPurify 净化 marked 输出，防止XSS攻击
  const rawHtml = marked.parseInline(props.content || '') as string
  return DOMPurify.sanitize(rawHtml, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'code', 'pre', 'br', 'a'],
    ALLOWED_ATTR: ['href', 'target', 'rel']
  })
})

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.message-bubble {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-bubble.user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.bubble-content {
  max-width: 70%;
}

.bubble-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
}

.message-bubble.user .bubble-header {
  flex-direction: row-reverse;
}

.role-name {
  font-weight: 500;
  color: #606266;
}

.time {
  color: #c0c4cc;
}

.bubble-body {
  display: inline-flex;
  align-items: flex-end;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f3f4f6;
  line-height: 1.6;
  word-break: break-word;
}

.message-bubble.user .bubble-body {
  background: #10b981;
  color: white;
  border-radius: 12px 12px 0 12px;
}

.message-bubble.ai .bubble-body {
  background: #e0e7ff;
  border-radius: 12px 12px 12px 0;
}

.message-bubble.system .bubble-body {
  background: #f4f4f5;
  color: #909399;
  font-size: 12px;
}

.text {
  white-space: pre-wrap;
}

.text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.message-bubble.user .text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.text :deep(strong) {
  font-weight: 600;
}

.text :deep(em) {
  font-style: italic;
}

.text :deep(a) {
  color: #409eff;
  text-decoration: underline;
}

.message-bubble.user .text :deep(a) {
  color: #fff;
}

.cursor {
  margin-left: 2px;
  font-weight: bold;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

/* 流式消息特殊样式 */
.message-bubble.streaming .bubble-body {
  min-height: 20px;
}
</style>
