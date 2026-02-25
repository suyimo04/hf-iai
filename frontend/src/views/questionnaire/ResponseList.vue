<template>
  <div class="response-list">
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
      <h2>问卷回复列表</h2>
      <div class="header-info" v-if="questionnaire">
        <span class="questionnaire-title">{{ questionnaire.title }}</span>
        <el-tag :type="getStatusType(questionnaire.status)" size="small">
          {{ getStatusLabel(questionnaire.status) }}
        </el-tag>
      </div>
      <el-button type="primary" :icon="Download" @click="handleExport" :loading="exporting">
        导出数据
      </el-button>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">总回复数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ todayCount }}</div>
          <div class="stat-label">今日回复</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ questionnaire?.fields?.length || 0 }}</div>
          <div class="stat-label">字段数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 回复列表 -->
    <el-card>
      <el-table :data="responses" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column
          v-for="field in displayFields"
          :key="field.fieldKey"
          :label="field.label"
          min-width="150"
        >
          <template #default="{ row }">
            <span class="answer-text">{{ formatAnswer(row.answers[field.fieldKey], field) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchResponses"
          @current-change="fetchResponses"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="回复详情" width="600px">
      <el-descriptions :column="1" border v-if="currentResponse">
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(currentResponse.submittedAt) }}
        </el-descriptions-item>
        <el-descriptions-item
          v-for="field in questionnaire?.fields"
          :key="field.fieldKey"
          :label="field.label"
        >
          {{ formatAnswer(currentResponse.answers[field.fieldKey], field) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getQuestionnaire,
  getQuestionnaireResponses,
  exportQuestionnaireResponses,
  type Questionnaire,
  type QuestionnaireResponse,
  type QuestionnaireField,
  type QuestionnaireStatus
} from '@/api/questionnaire'

defineOptions({ name: 'ResponseList' })

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const exporting = ref(false)
const detailVisible = ref(false)

const questionnaire = ref<Questionnaire | null>(null)
const responses = ref<QuestionnaireResponse[]>([])
const currentResponse = ref<QuestionnaireResponse | null>(null)
const total = ref(0)
const todayCount = ref(0)

const queryParams = ref({
  page: 1,
  size: 10
})

const displayFields = computed(() => {
  // 只显示前5个字段在表格中
  return (questionnaire.value?.fields || []).slice(0, 5)
})

function getStatusType(status: QuestionnaireStatus): '' | 'success' | 'warning' | 'info' {
  const map: Record<QuestionnaireStatus, '' | 'success' | 'warning' | 'info'> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning'
  }
  return map[status] || 'info'
}

function getStatusLabel(status: QuestionnaireStatus): string {
  const map: Record<QuestionnaireStatus, string> = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    ARCHIVED: '已归档'
  }
  return map[status] || status
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

function formatAnswer(answer: any, field: QuestionnaireField): string {
  if (answer === undefined || answer === null || answer === '') return '-'

  switch (field.fieldType) {
    case 'MULTIPLE_CHOICE':
      return Array.isArray(answer) ? answer.join(', ') : String(answer)
    case 'DATE':
      return formatDateTime(answer)
    default:
      return String(answer)
  }
}

async function fetchQuestionnaire() {
  const id = Number(route.params.id)
  if (!id) return

  try {
    const res = await getQuestionnaire(id)
    questionnaire.value = res.data.data
  } catch (e) {
    console.error('获取问卷详情失败', e)
  }
}

async function fetchResponses() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getQuestionnaireResponses(id, queryParams.value)
    responses.value = res.data.data?.content || []
    total.value = res.data.data?.totalElements || 0

    // 计算今日回复数
    const today = new Date().toDateString()
    todayCount.value = responses.value.filter(r =>
      new Date(r.submittedAt || '').toDateString() === today
    ).length
  } catch (e) {
    console.error('获取回复列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleViewDetail(row: QuestionnaireResponse) {
  currentResponse.value = row
  detailVisible.value = true
}

async function handleExport() {
  const id = Number(route.params.id)
  if (!id) return

  exporting.value = true
  try {
    const res = await exportQuestionnaireResponses(id)
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `问卷回复_${questionnaire.value?.title || id}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

function handleBack() {
  router.back()
}

onMounted(() => {
  fetchQuestionnaire()
  fetchResponses()
})
</script>

<style scoped>
.response-list {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.questionnaire-title {
  color: #606266;
  font-size: 14px;
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
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.answer-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
