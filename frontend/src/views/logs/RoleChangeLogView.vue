<template>
  <div class="role-change-log">
    <div class="page-header">
      <h2>角色变更日志</h2>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-area">
      <el-input
        v-model="queryParams.username"
        placeholder="用户名"
        clearable
        style="width: 150px"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="queryParams.fromRole"
        placeholder="原角色"
        clearable
        style="width: 140px"
        @change="handleSearch"
      >
        <el-option
          v-for="role in roleOptions"
          :key="role.value"
          :label="role.label"
          :value="role.value"
        />
      </el-select>
      <el-select
        v-model="queryParams.toRole"
        placeholder="新角色"
        clearable
        style="width: 140px"
        @change="handleSearch"
      >
        <el-option
          v-for="role in roleOptions"
          :key="role.value"
          :label="role.label"
          :value="role.value"
        />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 260px"
        @change="handleDateChange"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 日志表格 -->
    <el-card>
      <el-table :data="logs" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="变更时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column label="原角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.fromRole)" size="small">
              {{ getRoleLabel(row.fromRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="" width="50" align="center">
          <template #default>
            <el-icon><Right /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="新角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.toRole)" size="small">
              {{ getRoleLabel(row.toRole) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="变更原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="changedByName" label="操作人" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </el-card>

    <!-- 角色说明卡片 -->
    <el-card class="role-info-card">
      <template #header>
        <span>角色说明</span>
      </template>
      <div class="role-info-list">
        <div v-for="role in roleOptions" :key="role.value" class="role-info-item">
          <el-tag :type="getRoleTagType(role.value)" size="small">{{ role.label }}</el-tag>
          <span class="role-desc">{{ role.description }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Right } from '@element-plus/icons-vue'
import { getRoleChangeLogs, type RoleChangeLog } from '@/api/log'

defineOptions({ name: 'RoleChangeLogView' })

const loading = ref(false)
const logs = ref<RoleChangeLog[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const queryParams = reactive({
  username: '',
  fromRole: '',
  toRole: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 10
})

const roleOptions = [
  { value: 'ADMIN', label: '超级管理员', description: '拥有系统所有权限' },
  { value: 'LEADER', label: '群主', description: '管理群组和成员' },
  { value: 'VICE_LEADER', label: '副群主', description: '协助群主管理' },
  { value: 'MEMBER', label: '正式成员', description: '通过试用期的成员' },
  { value: 'PROBATION', label: '试用成员', description: '新加入的试用成员' },
  { value: 'INTERN', label: '实习成员', description: '实习期成员' }
]

function getRoleLabel(role: string): string {
  const option = roleOptions.find(r => r.value === role)
  return option?.label || role
}

function getRoleTagType(role: string): '' | 'success' | 'warning' | 'danger' | 'info' {
  const typeMap: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    ADMIN: 'danger',
    LEADER: 'warning',
    VICE_LEADER: 'warning',
    MEMBER: 'success',
    PROBATION: 'info',
    INTERN: ''
  }
  return typeMap[role] || 'info'
}

function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function handleDateChange(val: [string, string] | null) {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

async function fetchLogs() {
  loading.value = true
  try {
    const res = await getRoleChangeLogs(queryParams)
    logs.value = res.data.data?.content || []
    total.value = res.data.data?.totalElements || 0
  } catch (e) {
    console.error('获取角色变更日志失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchLogs()
}

function handleReset() {
  queryParams.username = ''
  queryParams.fromRole = ''
  queryParams.toRole = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  dateRange.value = null
  queryParams.page = 1
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.role-change-log {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-area {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.role-info-card {
  margin-top: 20px;
}

.role-info-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.role-info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-desc {
  font-size: 13px;
  color: #606266;
}
</style>
