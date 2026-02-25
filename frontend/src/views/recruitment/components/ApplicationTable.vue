<template>
  <el-table :data="data" v-loading="loading" stripe>
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column label="姓名" min-width="100">
      <template #default="{ row }">
        {{ row.formData?.name || row.user?.nickname || '-' }}
      </template>
    </el-table-column>
    <el-table-column label="手机" min-width="120">
      <template #default="{ row }">
        {{ row.formData?.phone || row.user?.phone || '-' }}
      </template>
    </el-table-column>
    <el-table-column label="邮箱" min-width="160">
      <template #default="{ row }">
        {{ row.formData?.email || row.user?.email || '-' }}
      </template>
    </el-table-column>
    <el-table-column label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="getStatusType(row.status)" size="small">
          {{ getStatusLabel(row.status) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column label="提交时间" width="170">
      <template #default="{ row }">
        {{ formatDateTime(row.createdAt) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" fixed="right">
      <template #default="{ row }">
        <el-button type="primary" link size="small" @click="$emit('view', row)">
          查看详情
        </el-button>
        <el-button
          v-if="canReview(row.status)"
          type="warning"
          link
          size="small"
          @click="$emit('review', row)"
        >
          审核
        </el-button>
        <el-button
          v-if="canInterview(row.status)"
          type="success"
          link
          size="small"
          @click="$emit('interview', row)"
        >
          发起面试
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { APPLICATION_STATUS_MAP, type Application, type ApplicationStatus } from '@/api/application'

defineProps<{
  data: Application[]
  loading?: boolean
}>()

defineEmits<{
  (e: 'view', row: Application): void
  (e: 'review', row: Application): void
  (e: 'interview', row: Application): void
}>()

function getStatusLabel(status: ApplicationStatus): string {
  return APPLICATION_STATUS_MAP[status]?.label || status
}

function getStatusType(status: ApplicationStatus) {
  return APPLICATION_STATUS_MAP[status]?.type || 'info'
}

function canReview(status: ApplicationStatus): boolean {
  return status === 'PENDING' || status === 'REVIEWING'
}

function canInterview(status: ApplicationStatus): boolean {
  return status === 'REVIEWING' || status === 'PASSED'
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
