<template>
  <div class="interview-list-page">
    <div class="page-header">
      <h2>AI面试管理</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row" v-if="statistics">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statistics.totalSessions }}</div>
          <div class="stat-label">总面试数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ statistics.completedSessions }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value highlight">{{ statistics.averageScore.toFixed(1) }}</div>
          <div class="stat-label">平均分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value success">{{ (statistics.passRate * 100).toFixed(1) }}%</div>
          <div class="stat-label">通过率</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="queryParams.status"
        placeholder="状态筛选"
        clearable
        style="width: 150px"
        @change="handleSearch"
      >
        <el-option
          v-for="(item, key) in AI_INTERVIEW_STATUS_MAP"
          :key="key"
          :label="item.label"
          :value="key"
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

    <!-- 面试列表 -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="对话轮次" width="100" align="center">
        <template #default="{ row }">
          {{ row.roundCount }} / {{ row.maxRounds }}
        </template>
      </el-table-column>
      <el-table-column label="评分" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.score" :class="getScoreClass(row.score.finalScore)">
            {{ row.score.finalScore }}
          </span>
          <span v-else class="no-score">-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI模型" width="150">
        <template #default="{ row }">
          {{ row.aiProvider || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="开始时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.startedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="结束时间" width="170">
        <template #default="{ row }">
          {{ formatDateTime(row.endedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleView(row)">
            查看回放
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getAIInterviewSessions,
  getAIInterviewStatistics,
  AI_INTERVIEW_STATUS_MAP,
  type AIInterviewSession,
  type AIInterviewStatistics,
  type AIInterviewStatus,
  type AIInterviewQueryParams
} from '@/api/aiInterview'

defineOptions({ name: 'InterviewListPage' })

const router = useRouter()
const loading = ref(false)
const tableData = ref<AIInterviewSession[]>([])
const total = ref(0)
const statistics = ref<AIInterviewStatistics | null>(null)
const dateRange = ref<[string, string] | null>(null)

const queryParams = ref<AIInterviewQueryParams>({
  page: 1,
  size: 10,
  status: undefined,
  startDate: undefined,
  endDate: undefined
})

function getStatusType(status: AIInterviewStatus) {
  return AI_INTERVIEW_STATUS_MAP[status]?.type || 'info'
}

function getStatusLabel(status: AIInterviewStatus) {
  return AI_INTERVIEW_STATUS_MAP[status]?.label || status
}

function getScoreClass(score: number): string {
  if (score >= 80) return 'score-excellent'
  if (score >= 60) return 'score-good'
  if (score >= 40) return 'score-average'
  return 'score-poor'
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
  loading.value = true
  try {
    const res = await getAIInterviewSessions(queryParams.value)
    tableData.value = res.data.data?.content || []
    total.value = res.data.data?.totalElements || 0
  } catch (e) {
    console.error('获取面试列表失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchStatistics() {
  try {
    const res = await getAIInterviewStatistics()
    statistics.value = res.data.data
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

function handleSearch() {
  queryParams.value.page = 1
  fetchData()
}

function handleReset() {
  queryParams.value = {
    page: 1,
    size: 10,
    status: undefined,
    startDate: undefined,
    endDate: undefined
  }
  dateRange.value = null
  fetchData()
}

function handleDateChange(val: [string, string] | null) {
  if (val) {
    queryParams.value.startDate = val[0]
    queryParams.value.endDate = val[1]
  } else {
    queryParams.value.startDate = undefined
    queryParams.value.endDate = undefined
  }
}

function handleView(row: AIInterviewSession) {
  router.push(`/interview/replay/${row.id}`)
}

onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped>
.interview-list-page {
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-value.highlight {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.score-excellent {
  color: #67c23a;
  font-weight: 600;
}

.score-good {
  color: #409eff;
  font-weight: 600;
}

.score-average {
  color: #e6a23c;
  font-weight: 600;
}

.score-poor {
  color: #f56c6c;
  font-weight: 600;
}

.no-score {
  color: #c0c4cc;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
