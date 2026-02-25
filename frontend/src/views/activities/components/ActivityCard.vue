<template>
  <el-card class="activity-card" shadow="hover">
    <div class="activity-card__header">
      <h3 class="activity-card__title">{{ activity.title }}</h3>
      <el-tag :type="statusTagType" size="small">{{ statusText }}</el-tag>
    </div>

    <div class="activity-card__info">
      <div class="activity-card__item">
        <el-icon><Calendar /></el-icon>
        <span>{{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
      </div>
      <div class="activity-card__item">
        <el-icon><Location /></el-icon>
        <span>{{ activity.location }}</span>
      </div>
      <div class="activity-card__item">
        <el-icon><User /></el-icon>
        <span>{{ activity.currentParticipants }} / {{ activity.maxParticipants }} 人</span>
      </div>
      <div class="activity-card__item">
        <el-icon><Medal /></el-icon>
        <span>签到奖励 {{ activity.signinPoints }} 积分</span>
      </div>
    </div>

    <div class="activity-card__actions">
      <el-button type="primary" link @click="$emit('edit', activity)">
        <el-icon><Edit /></el-icon>编辑
      </el-button>
      <el-button type="primary" link @click="$emit('signups', activity)">
        <el-icon><List /></el-icon>报名管理
      </el-button>
      <el-popconfirm title="确定删除该活动吗？" @confirm="$emit('delete', activity)">
        <template #reference>
          <el-button type="danger" link>
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </template>
      </el-popconfirm>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Calendar, Location, User, Medal, Edit, List, Delete } from '@element-plus/icons-vue'
import type { Activity, ActivityStatus } from '@/api/activity'

const props = defineProps<{
  activity: Activity
}>()

defineEmits<{
  edit: [activity: Activity]
  delete: [activity: Activity]
  signups: [activity: Activity]
}>()

const statusMap: Record<ActivityStatus, { text: string; type: 'info' | 'warning' | 'success' | 'danger' }> = {
  DRAFT: { text: '草稿', type: 'info' },
  PUBLISHED: { text: '已发布', type: 'warning' },
  ONGOING: { text: '进行中', type: 'success' },
  ENDED: { text: '已结束', type: 'danger' }
}

const statusText = computed(() => statusMap[props.activity.status]?.text || '未知')
const statusTagType = computed(() => statusMap[props.activity.status]?.type || 'info')

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.activity-card {
  margin-bottom: 16px;
}

.activity-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.activity-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.activity-card__info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.activity-card__item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.activity-card__item .el-icon {
  color: #9ca3af;
}

.activity-card__actions {
  display: flex;
  gap: 8px;
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}
</style>
