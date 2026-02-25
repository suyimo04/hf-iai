<template>
  <div class="interview-replay" v-loading="loading">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
      <h2>面试回放</h2>
      <el-tag v-if="session" :type="getStatusType(session.status)">
        {{ getStatusLabel(session.status) }}
      </el-tag>
    </div>

    <div class="replay-container" v-if="session">
      <el-row :gutter="20">
        <el-col :span="16">
          <!-- 消息历史 -->
          <el-card class="message-card">
            <template #header>
              <div class="card-header">
                <span>对话记录</span>
                <span class="round-info">共 {{ messages.length }} 条消息</span>
              </div>
            </template>
            <div class="message-list">
              <div v-if="messages.length === 0" class="empty-tip">
                暂无对话记录
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
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <!-- 会话信息 -->
          <el-card class="info-card">
            <template #header>
              <span>会话信息</span>
            </template>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="会话ID">{{ session.id }}</el-descriptions-item>
              <el-descriptions-item label="用户">{{ session.username }}</el-descriptions-item>
              <el-descriptions-item label="对话轮次">
                {{ session.roundCount }} / {{ session.maxRounds }}
              </el-descriptions-item>
              <el-descriptions-item label="AI模型">
                {{ session.aiProvider || '-' }} / {{ session.aiModel || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="开始时间">
                {{ formatDateTime(session.startedAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="结束时间">
                {{ formatDateTime(session.endedAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 评分结果 -->
          <el-card v-if="score" class="score-card">
            <template #header>
              <span>评分结果</span>
            </template>
            <ScoreRadarChart :score="scoreData" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" description="面试记录不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  getAIInterviewSession,
  getSessionMessages,
  getAIInterviewScore,
  AI_INTERVIEW_STATUS_MAP,
  type AIInterviewSession,
  type AIInterviewMessage,
  type AIInterviewScore,
  type AIInterviewStatus
} from '@/api/aiInterview'
import ScoreRadarChart from '@/components/interview/ScoreRadarChart.vue'

defineOptions({ name: 'InterviewReplay' })

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const session = ref<AIInterviewSession | null>(null)
const messages = ref<AIInterviewMessage[]>([])
const score = ref<AIInterviewScore | null>(null)

const scoreData = computed(() => {
  if (!score.value) {
    return {
      attitudeScore: 0,
      ruleExecutionScore: 0,
      emotionalControlScore: 0,
      decisionRationalityScore: 0,
      finalScore: 0,
      evaluation: ''
    }
  }
  return {
    attitudeScore: score.value.attitudeScore,
    ruleExecutionScore: score.value.ruleExecutionScore,
    emotionalControlScore: score.value.emotionalControlScore,
    decisionRationalityScore: score.value.decisionRationalityScore,
    finalScore: score.value.finalScore,
    evaluation: score.value.evaluation || ''
  }
})

function getStatusType(status: AIInterviewStatus) {
  return AI_INTERVIEW_STATUS_MAP[status]?.type || 'info'
}

function getStatusLabel(status: AIInterviewStatus) {
  return AI_INTERVIEW_STATUS_MAP[status]?.label || status
}

function formatTime(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function fetchData() {
  const sessionId = Number(route.params.id)
  if (!sessionId) return

  loading.value = true
  try {
    const [sessionRes, messagesRes] = await Promise.all([
      getAIInterviewSession(sessionId),
      getSessionMessages(sessionId)
    ])
    session.value = sessionRes.data.data
    messages.value = messagesRes.data.data || []

    // 如果已完成，获取评分
    if (session.value?.status === 'COMPLETED') {
      try {
        const scoreRes = await getAIInterviewScore(sessionId)
        score.value = scoreRes.data.data
      } catch (e) {
        console.warn('获取评分失败', e)
      }
    }
  } catch (e) {
    console.error('获取面试详情失败', e)
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.interview-replay {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.replay-container {
  max-width: 1400px;
}

.message-card {
  height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
}

.message-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.round-info {
  font-size: 12px;
  color: #909399;
}

.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 16px;
}

.empty-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
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

.message .time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message.user .time {
  text-align: right;
}

.info-card {
  margin-bottom: 20px;
}

.score-card :deep(.el-card__body) {
  padding: 16px;
}
</style>
