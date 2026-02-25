<template>
  <el-dialog
    v-model="visible"
    title="报名详情"
    width="600px"
    destroy-on-close
    @close="$emit('update:modelValue', false)"
  >
    <div v-if="application" class="detail-content">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">
          {{ application.formData?.name || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机">
          {{ application.formData?.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ application.formData?.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="年龄">
          {{ application.formData?.age || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(application.status)" size="small">
            {{ getStatusLabel(application.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(application.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">
          {{ application.formData?.introduction || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          {{ application.formData?.reason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="技能特长" :span="2">
          {{ application.formData?.skills || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="application.reviewComment || application.reviewer" class="review-history">
        <h4>审核记录</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="审核人">
            {{ application.reviewer?.nickname || application.reviewer?.username || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见">
            {{ application.reviewComment || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(application.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { APPLICATION_STATUS_MAP, type Application, type ApplicationStatus } from '@/api/application'

const props = defineProps<{
  modelValue: boolean
  application: Application | null
}>()

defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => {}
})

function getStatusLabel(status: ApplicationStatus): string {
  return APPLICATION_STATUS_MAP[status]?.label || status
}

function getStatusType(status: ApplicationStatus) {
  return APPLICATION_STATUS_MAP[status]?.type || 'info'
}

function formatDateTime(dateStr: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.review-history {
  margin-top: 20px;
}

.review-history h4 {
  margin-bottom: 12px;
  color: #303133;
  font-size: 14px;
}
</style>
