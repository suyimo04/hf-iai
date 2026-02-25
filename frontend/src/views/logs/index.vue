<template>
  <div class="logs-page">
    <BaseCard title="操作日志">
      <!-- 筛选区域 -->
      <div class="filter-area">
        <el-select
          v-model="queryParams.category"
          placeholder="日志分类"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in categoryOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-select
          v-model="queryParams.action"
          placeholder="操作类型"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in actionOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-select
          v-model="queryParams.targetType"
          placeholder="目标类型"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in targetTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-input
          v-model="queryParams.username"
          placeholder="操作人"
          clearable
          style="width: 140px"
          @keyup.enter="handleSearch"
        />

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

        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 日志表格 -->
      <el-table :data="logs" stripe v-loading="loading">
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)" size="small">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getActionTagType(row.action)" size="small">
              {{ getActionLabel(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="目标类型" width="100">
          <template #default="{ row }">
            {{ getTargetTypeLabel(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
      </el-table>

      <!-- 分页 -->
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
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import BaseCard from '@/components/base/BaseCard.vue'
import { getLogs, type OperationLog } from '@/api/log'

const loading = ref(false)
const logs = ref<OperationLog[]>([])
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)

const queryParams = reactive({
  category: '',
  action: '',
  targetType: '',
  username: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 10
})

const categoryOptions = [
  { value: 'AUTH', label: '认证' },
  { value: 'USER', label: '用户' },
  { value: 'BUSINESS', label: '业务' },
  { value: 'SYSTEM', label: '系统' },
  { value: 'SECURITY', label: '安全' }
]

const actionOptions = [
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'CREATE', label: '创建' },
  { value: 'UPDATE', label: '更新' },
  { value: 'DELETE', label: '删除' },
  { value: 'REVIEW', label: '审核' },
  { value: 'EXPORT', label: '导出' },
  { value: 'IMPORT', label: '导入' }
]

const targetTypeOptions = [
  { value: 'USER', label: '用户' },
  { value: 'APPLICATION', label: '报名' },
  { value: 'INTERVIEW', label: '面试' },
  { value: 'ACTIVITY', label: '活动' },
  { value: 'SALARY', label: '薪酬' },
  { value: 'QUESTIONNAIRE', label: '问卷' },
  { value: 'CONFIG', label: '配置' },
  { value: 'MENU', label: '菜单' },
  { value: 'PERMISSION', label: '权限' }
]

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

function getCategoryLabel(category: string) {
  const option = categoryOptions.find(item => item.value === category)
  return option?.label || category || '-'
}

function getCategoryTagType(category: string) {
  const typeMap: Record<string, string> = {
    AUTH: 'info',
    USER: 'primary',
    BUSINESS: 'success',
    SYSTEM: 'warning',
    SECURITY: 'danger'
  }
  return typeMap[category] || 'info'
}

function getActionLabel(action: string) {
  const option = actionOptions.find(item => item.value === action)
  return option?.label || action
}

function getActionTagType(action: string) {
  const typeMap: Record<string, string> = {
    LOGIN: 'info',
    LOGOUT: 'info',
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    REVIEW: 'primary',
    EXPORT: '',
    IMPORT: ''
  }
  return typeMap[action] || 'info'
}

function getTargetTypeLabel(targetType: string) {
  const option = targetTypeOptions.find(item => item.value === targetType)
  return option?.label || targetType
}

function handleDateChange(val: [string, string] | null) {
  if (val) {
    queryParams.startDate = val[0]
    queryParams.endDate = val[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  handleSearch()
}

async function fetchLogs() {
  loading.value = true
  try {
    const res = await getLogs(queryParams)
    logs.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchLogs()
}

function handleReset() {
  queryParams.category = ''
  queryParams.action = ''
  queryParams.targetType = ''
  queryParams.username = ''
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
.logs-page {
  padding: 20px;
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
</style>
