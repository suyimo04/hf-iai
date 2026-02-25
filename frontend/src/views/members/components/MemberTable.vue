<template>
  <el-table :data="data" v-loading="loading" style="width: 100%">
    <el-table-column label="头像" width="80">
      <template #default="{ row }">
        <el-avatar :size="40" :src="row.avatar">
          {{ row.nickname?.charAt(0) || row.username?.charAt(0) }}
        </el-avatar>
      </template>
    </el-table-column>
    <el-table-column prop="username" label="用户名" min-width="120" />
    <el-table-column prop="nickname" label="昵称" min-width="120">
      <template #default="{ row }">
        {{ row.nickname || '-' }}
      </template>
    </el-table-column>
    <el-table-column label="角色" width="120">
      <template #default="{ row }">
        <el-tag :type="getRoleTagType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="注册时间" width="180">
      <template #default="{ row }">
        {{ formatDate(row.createdAt) }}
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" fixed="right">
      <template #default="{ row }">
        <el-button
          v-if="canEditRole"
          type="primary"
          link
          size="small"
          @click="emit('edit-role', row)"
        >
          修改角色
        </el-button>
        <el-button
          v-if="canEditStatus"
          type="warning"
          link
          size="small"
          @click="emit('edit-status', row)"
        >
          修改状态
        </el-button>
        <el-popconfirm
          v-if="canDelete"
          title="确定要删除该用户吗？"
          @confirm="emit('delete', row)"
        >
          <template #reference>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-popconfirm>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import type { User, Role } from '@/types'

defineProps<{
  data: User[]
  loading: boolean
  canEditRole: boolean
  canEditStatus: boolean
  canDelete: boolean
}>()

const emit = defineEmits<{
  'edit-role': [user: User]
  'edit-status': [user: User]
  'delete': [user: User]
}>()

const roleMap: Record<Role, string> = {
  ADMIN: '管理员',
  LEADER: '组长',
  VICE_LEADER: '副组长',
  MEMBER: '正式成员',
  INTERN: '实习成员',
  APPLICANT: '应聘者'
}

const statusMap: Record<string, string> = {
  ACTIVE: '正常',
  INACTIVE: '禁用',
  PENDING: '待审核'
}

function getRoleLabel(role: Role): string {
  return roleMap[role] || role
}

function getRoleTagType(role: Role): '' | 'success' | 'warning' | 'info' | 'danger' {
  const typeMap: Record<Role, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    ADMIN: 'danger',
    LEADER: '',
    VICE_LEADER: 'warning',
    MEMBER: 'success',
    INTERN: 'info',
    APPLICANT: 'info'
  }
  return typeMap[role] || 'info'
}

function getStatusLabel(status: string): string {
  return statusMap[status] || status
}

function getStatusTagType(status: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  const typeMap: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    ACTIVE: 'success',
    INACTIVE: 'danger',
    PENDING: 'warning'
  }
  return typeMap[status] || 'info'
}

function formatDate(dateStr: string): string {
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
