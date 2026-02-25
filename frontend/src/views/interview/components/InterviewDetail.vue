<template>
  <el-dialog
    v-model="visible"
    title="面试详情"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-loading="loading" class="interview-detail">
      <!-- 应聘者信息 -->
      <el-card shadow="never" class="info-card">
        <template #header>
          <span class="card-title">应聘者信息</span>
        </template>
        <div class="applicant-header">
          <el-avatar :size="64" :src="interview?.applicantAvatar">
            {{ interview?.applicantName?.charAt(0) }}
          </el-avatar>
          <div class="applicant-meta">
            <h3>{{ interview?.applicantName }}</h3>
            <div class="meta-row">
              <el-tag :type="getStatusType(interview?.status)">
                {{ getStatusText(interview?.status) }}
              </el-tag>
              <span v-if="interview?.startTime" class="time">
                面试时间：{{ interview.startTime }}
              </span>
            </div>
          </div>
          <div v-if="interview?.score !== null && interview?.score !== undefined" class="score-display">
            <div class="score-value" :class="getScoreClass(interview.score)">
              {{ interview.score }}
            </div>
            <div class="score-label">综合得分</div>
          </div>
        </div>
      </el-card>

      <!-- 答题详情 -->
      <el-card shadow="never" class="questions-card">
        <template #header>
          <span class="card-title">答题详情</span>
        </template>
        <div v-if="questions.length === 0" class="empty-tip">暂无答题记录</div>
        <div v-else class="questions-list">
          <div v-for="(q, index) in questions" :key="q.id" class="question-item">
            <div class="question-header">
              <span class="question-index">{{ index + 1 }}.</span>
              <span class="question-content">{{ q.question }}</span>
              <el-tag size="small" type="info">{{ getQuestionTypeText(q.questionType) }}</el-tag>
            </div>
            <div v-if="q.options && q.options.length > 0" class="question-options">
              <div v-for="(opt, i) in q.options" :key="i" class="option-item">
                {{ String.fromCharCode(65 + i) }}. {{ opt }}
              </div>
            </div>
            <div class="answer-section">
              <div class="answer-row">
                <span class="label">回答：</span>
                <span class="value">{{ q.answer || '未作答' }}</span>
              </div>
              <div class="score-row">
                <span class="label">得分：</span>
                <span class="value" :class="getAnswerScoreClass(q.score, q.maxScore)">
                  {{ q.score ?? '--' }} / {{ q.maxScore }}
                </span>
              </div>
            </div>
            <div v-if="q.aiComment" class="ai-comment">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ q.aiComment }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- AI评分报告 -->
      <el-card v-if="showReport && interview?.aiReport" shadow="never" class="report-card">
        <template #header>
          <span class="card-title">AI评分报告</span>
        </template>
        <div class="report-content" v-html="formatReport(interview.aiReport)"></div>
      </el-card>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import DOMPurify from 'dompurify'
import {
  getInterviewById,
  getInterviewQuestions,
  type Interview,
  type InterviewQuestion,
  type InterviewStatus,
  type QuestionType
} from '@/api/interview'

const props = defineProps<{
  modelValue: boolean
  interviewId: number | null
  showReport?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = ref(false)
const loading = ref(false)
const interview = ref<Interview | null>(null)
const questions = ref<InterviewQuestion[]>([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.interviewId) {
    fetchDetail()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function fetchDetail() {
  if (!props.interviewId) return
  loading.value = true
  try {
    const [interviewRes, questionsRes] = await Promise.all([
      getInterviewById(props.interviewId),
      getInterviewQuestions(props.interviewId)
    ])
    interview.value = interviewRes.data.data
    questions.value = questionsRes.data.data
  } finally {
    loading.value = false
  }
}

function getStatusType(status?: InterviewStatus) {
  if (!status) return 'info'
  const map: Record<InterviewStatus, string> = {
    PENDING: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

function getStatusText(status?: InterviewStatus) {
  if (!status) return ''
  const map: Record<InterviewStatus, string> = {
    PENDING: '待面试',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

function getQuestionTypeText(type: QuestionType) {
  const map: Record<QuestionType, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TEXT: '问答题'
  }
  return map[type] || type
}

function getScoreClass(score: number) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

function getAnswerScoreClass(score: number | undefined, maxScore: number) {
  if (score === undefined || score === null) return ''
  const ratio = score / maxScore
  if (ratio >= 0.8) return 'score-high'
  if (ratio >= 0.6) return 'score-medium'
  return 'score-low'
}

function formatReport(report: string): string {
  if (!report) return ''
  // 使用 DOMPurify 净化输出，防止XSS攻击
  const escaped = report.replace(/\n/g, '<br>')
  return DOMPurify.sanitize(escaped, {
    ALLOWED_TAGS: ['br'],
    ALLOWED_ATTR: []
  })
}

function handleClose() {
  visible.value = false
  interview.value = null
  questions.value = []
}
</script>

<style scoped>
.interview-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.card-title {
  font-weight: 600;
}

.applicant-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.applicant-meta h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-row .time {
  color: #909399;
  font-size: 14px;
}

.score-display {
  margin-left: auto;
  text-align: center;
}

.score-value {
  font-size: 36px;
  font-weight: bold;
}

.score-label {
  font-size: 12px;
  color: #909399;
}

.score-high { color: #67c23a; }
.score-medium { color: #e6a23c; }
.score-low { color: #f56c6c; }

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.question-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.question-index {
  font-weight: 600;
  color: #409eff;
}

.question-content {
  flex: 1;
  line-height: 1.5;
}

.question-options {
  margin: 8px 0 8px 20px;
  color: #606266;
}

.option-item {
  padding: 4px 0;
}

.answer-section {
  display: flex;
  gap: 24px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #dcdfe6;
}

.answer-row, .score-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.answer-row .label, .score-row .label {
  color: #909399;
}

.ai-comment {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 8px;
  padding: 8px;
  background: #ecf5ff;
  border-radius: 4px;
  color: #409eff;
  font-size: 13px;
}

.report-content {
  line-height: 1.8;
  color: #606266;
}
</style>
