<template>
  <div class="application-status">
    <div class="status-header">
      <div class="status-badge" :class="`status-badge--${statusInfo.type}`">
        <el-icon :size="20"><component :is="statusIcon" /></el-icon>
      </div>
      <div class="status-info">
        <h3 class="status-title">{{ statusInfo.label }}</h3>
        <p class="status-time">提交于 {{ formatDate(application.createdAt) }}</p>
      </div>
    </div>

    <div class="status-timeline">
      <div
        v-for="(step, index) in timelineSteps"
        :key="step.status"
        class="timeline-item"
        :class="{
          'is-active': step.isActive,
          'is-completed': step.isCompleted,
          'is-rejected': step.status === 'REJECTED' && application.status === 'REJECTED'
        }"
      >
        <div class="timeline-node">
          <el-icon v-if="step.isCompleted" :size="14"><Check /></el-icon>
          <el-icon v-else-if="step.status === 'REJECTED' && application.status === 'REJECTED'" :size="14"><Close /></el-icon>
          <span v-else class="node-index">{{ index + 1 }}</span>
        </div>
        <div class="timeline-content">
          <span class="timeline-label">{{ step.label }}</span>
          <span v-if="step.time" class="timeline-time">{{ step.time }}</span>
        </div>
        <div v-if="index < timelineSteps.length - 1" class="timeline-line" />
      </div>
    </div>

    <div v-if="application.reviewComment" class="status-comment">
      <div class="comment-header">
        <el-icon :size="16"><ChatDotRound /></el-icon>
        <span>审核意见</span>
      </div>
      <p class="comment-content">{{ application.reviewComment }}</p>
    </div>

    <div class="status-details">
      <h4 class="details-title">报名信息</h4>
      <div class="details-grid">
        <div class="detail-item">
          <span class="detail-label">姓名</span>
          <span class="detail-value">{{ application.formData.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">手机号</span>
          <span class="detail-value">{{ application.formData.phone }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">邮箱</span>
          <span class="detail-value">{{ application.formData.email }}</span>
        </div>
        <div v-if="application.formData.age" class="detail-item">
          <span class="detail-label">年龄</span>
          <span class="detail-value">{{ application.formData.age }}</span>
        </div>
      </div>
      <div class="detail-block">
        <span class="detail-label">自我介绍</span>
        <p class="detail-text">{{ application.formData.introduction }}</p>
      </div>
      <div class="detail-block">
        <span class="detail-label">加入原因</span>
        <p class="detail-text">{{ application.formData.reason }}</p>
      </div>
      <div v-if="application.formData.skills" class="detail-block">
        <span class="detail-label">特长技能</span>
        <p class="detail-text">{{ application.formData.skills }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check, Close, ChatDotRound, Clock, Loading, CircleCheck, CircleClose, VideoCamera, Trophy, Star } from '@element-plus/icons-vue'
import type { Application, ApplicationStatus } from '@/api/application'
import { APPLICATION_STATUS_MAP } from '@/api/application'

const props = defineProps<{
  application: Application
}>()

const statusInfo = computed(() => APPLICATION_STATUS_MAP[props.application.status])

const statusIcon = computed(() => {
  const iconMap: Record<ApplicationStatus, any> = {
    PENDING: Clock,
    REVIEWING: Loading,
    INTERVIEW: VideoCamera,
    PASSED: CircleCheck,
    REJECTED: CircleClose,
    INTERN: Star,
    CONVERTED: Trophy
  }
  return iconMap[props.application.status]
})

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const timelineSteps = computed(() => {
  const statusOrder: ApplicationStatus[] = ['PENDING', 'REVIEWING', 'INTERVIEW', 'PASSED', 'INTERN', 'CONVERTED']
  const currentIndex = statusOrder.indexOf(props.application.status)
  const isRejected = props.application.status === 'REJECTED'

  if (isRejected) {
    return [
      { status: 'PENDING' as const, label: '提交申请', isCompleted: true, isActive: false, time: formatDate(props.application.createdAt) },
      { status: 'REVIEWING' as const, label: '审核中', isCompleted: true, isActive: false, time: null },
      { status: 'REJECTED' as const, label: '已拒绝', isCompleted: false, isActive: true, time: props.application.updatedAt ? formatDate(props.application.updatedAt) : null }
    ]
  }

  return [
    { status: 'PENDING' as const, label: '提交申请', isCompleted: true, isActive: currentIndex === 0, time: formatDate(props.application.createdAt) },
    { status: 'REVIEWING' as const, label: '审核中', isCompleted: currentIndex > 1, isActive: currentIndex === 1, time: null },
    { status: 'INTERVIEW' as const, label: '面试', isCompleted: currentIndex > 2, isActive: currentIndex === 2, time: null },
    { status: 'PASSED' as const, label: '通过', isCompleted: currentIndex > 3, isActive: currentIndex === 3, time: null }
  ]
})
</script>

<style scoped>
.application-status {
  padding: 24px;
}

.status-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.status-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.status-badge--warning { background: #e6a23c; }
.status-badge--info { background: #909399; }
.status-badge--primary { background: #409eff; }
.status-badge--success { background: #67c23a; }
.status-badge--danger { background: #f56c6c; }

.status-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.status-time {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}

.status-timeline {
  display: flex;
  justify-content: space-between;
  margin-bottom: 32px;
  padding: 0 8px;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
}

.timeline-node {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 600;
  z-index: 1;
  transition: all 0.3s ease;
}

.timeline-item.is-completed .timeline-node {
  background: var(--color-primary, #10b981);
  color: white;
}

.timeline-item.is-active .timeline-node {
  background: var(--color-primary, #10b981);
  color: white;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
}

.timeline-item.is-rejected .timeline-node {
  background: #f56c6c;
  color: white;
  box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.2);
}

.timeline-content {
  margin-top: 8px;
  text-align: center;
}

.timeline-label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.timeline-item.is-active .timeline-label,
.timeline-item.is-completed .timeline-label {
  color: #1f2937;
}

.timeline-time {
  display: block;
  font-size: 11px;
  color: #9ca3af;
  margin-top: 2px;
}

.timeline-line {
  position: absolute;
  top: 16px;
  left: calc(50% + 20px);
  width: calc(100% - 40px);
  height: 2px;
  background: #e5e7eb;
}

.timeline-item.is-completed .timeline-line {
  background: var(--color-primary, #10b981);
}

.status-comment {
  background: #fef3c7;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #92400e;
  font-weight: 500;
  margin-bottom: 8px;
}

.comment-content {
  margin: 0;
  color: #78350f;
  font-size: 14px;
  line-height: 1.6;
}

.status-details {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.details-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #1f2937;
}

.detail-block {
  margin-bottom: 12px;
}

.detail-block:last-child {
  margin-bottom: 0;
}

.detail-text {
  margin: 4px 0 0;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
