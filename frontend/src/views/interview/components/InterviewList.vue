<template>
  <div class="interview-list">
    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="待面试" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="应聘者" min-width="150">
          <template #default="{ row }">
            <div class="applicant-info">
              <el-avatar :size="32" :src="row.applicantAvatar">
                {{ row.applicantName?.charAt(0) }}
              </el-avatar>
              <span class="name">{{ row.applicantName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分数" prop="score" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.score !== null && row.score !== undefined" :class="getScoreClass(row.score)">
              {{ row.score }}
            </span>
            <span v-else class="no-score">--</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面试时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.startTime || row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
            <el-button
              v-if="row.status === 'COMPLETED'"
              type="success"
              link
              @click="handleViewReport(row)"
            >
              查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getInterviews, type Interview, type InterviewStatus } from '@/api/interview'

const emit = defineEmits<{
  (e: 'view-detail', interview: Interview): void
  (e: 'view-report', interview: Interview): void
}>()

const loading = ref(false)
const tableData = ref<Interview[]>([])
const dateRange = ref<[string, string] | null>(null)

const filterForm = reactive({
  status: '' as InterviewStatus | ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 获取状态标签类型
function getStatusType(status: InterviewStatus) {
  const map: Record<InterviewStatus, string> = {
    PENDING: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
function getStatusText(status: InterviewStatus) {
  const map: Record<InterviewStatus, string> = {
    PENDING: '待面试',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

// 获取分数样式
function getScoreClass(score: number) {
  if (score >= 80) return 'score-high'
  if (score >= 60) return 'score-medium'
  return 'score-low'
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      status: filterForm.status || undefined,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1]
    }
    const res = await getInterviews(params)
    tableData.value = res.data.data.content
    pagination.total = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchData()
}

// 重置
function handleReset() {
  filterForm.status = ''
  dateRange.value = null
  pagination.page = 1
  fetchData()
}

// 查看详情
function handleViewDetail(row: Interview) {
  emit('view-detail', row)
}

// 查看报告
function handleViewReport(row: Interview) {
  emit('view-report', row)
}

onMounted(() => {
  fetchData()
})

defineExpose({ fetchData })
</script>

<style scoped>
.interview-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.applicant-info .name {
  font-weight: 500;
}

.score-high {
  color: #67c23a;
  font-weight: bold;
}

.score-medium {
  color: #e6a23c;
  font-weight: bold;
}

.score-low {
  color: #f56c6c;
  font-weight: bold;
}

.no-score {
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
