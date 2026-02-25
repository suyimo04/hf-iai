<template>
  <div class="questionnaire-list">
    <div class="page-header">
      <h2>问卷管理</h2>
      <el-button
        v-permission="'questionnaire:create'"
        type="primary"
        :icon="Plus"
        @click="handleCreate"
      >
        新建问卷
      </el-button>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="queryParams.status"
        placeholder="状态筛选"
        clearable
        style="width: 150px"
        @change="handleSearch"
      >
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="已归档" value="ARCHIVED" />
      </el-select>
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索问卷标题"
        clearable
        style="width: 250px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" style="width: 100%">
      <el-table-column prop="title" label="问卷标题" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="handlePreview(row)">{{ row.title }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" align="center">
        <template #default="{ row }">
          v{{ row.version }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'questionnaire:edit'"
            type="primary"
            link
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button type="info" link size="small" @click="handlePreview(row)">
            预览
          </el-button>
          <el-button
            v-if="row.status === 'DRAFT'"
            v-permission="'questionnaire:publish'"
            type="success"
            link
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="row.status === 'PUBLISHED'"
            v-permission="'questionnaire:archive'"
            type="warning"
            link
            size="small"
            @click="handleArchive(row)"
          >
            归档
          </el-button>
          <el-button
            v-if="row.status === 'PUBLISHED' && row.publicToken"
            type="primary"
            link
            size="small"
            @click="handleCopyLink(row)"
          >
            复制链接
          </el-button>
          <el-popconfirm
            title="确定要删除该问卷吗？"
            @confirm="handleDelete(row)"
          >
            <template #reference>
              <el-button
                v-permission="'questionnaire:delete'"
                type="danger"
                link
                size="small"
              >
                删除
              </el-button>
            </template>
          </el-popconfirm>
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
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getQuestionnaires,
  deleteQuestionnaire,
  publishQuestionnaire,
  archiveQuestionnaire,
  type Questionnaire,
  type QuestionnaireStatus,
  type QuestionnaireQuery
} from '@/api/questionnaire'

defineOptions({ name: 'QuestionnaireList' })

const router = useRouter()
const loading = ref(false)
const tableData = ref<Questionnaire[]>([])
const total = ref(0)

const queryParams = ref<QuestionnaireQuery>({
  page: 1,
  size: 10,
  status: undefined,
  keyword: ''
})

const statusMap: Record<QuestionnaireStatus, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  ARCHIVED: '已归档'
}

function getStatusLabel(status: QuestionnaireStatus): string {
  return statusMap[status] || status
}

function getStatusTagType(status: QuestionnaireStatus): '' | 'success' | 'warning' | 'info' {
  const typeMap: Record<QuestionnaireStatus, '' | 'success' | 'warning' | 'info'> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning'
  }
  return typeMap[status] || 'info'
}

function formatDate(dateStr?: string): string {
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
    const res = await getQuestionnaires(queryParams.value)
    tableData.value = res.data.data?.content || []
    total.value = res.data.data?.totalElements || 0
  } catch (e) {
    console.error('获取问卷列表失败', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.value.page = 1
  fetchData()
}

function handleCreate() {
  router.push('/questionnaire/create')
}

function handleEdit(row: Questionnaire) {
  router.push(`/questionnaire/edit/${row.id}`)
}

function handlePreview(row: Questionnaire) {
  router.push(`/questionnaire/preview/${row.id}`)
}

async function handlePublish(row: Questionnaire) {
  try {
    await publishQuestionnaire(row.id!)
    ElMessage.success('发布成功')
    fetchData()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

async function handleArchive(row: Questionnaire) {
  try {
    await archiveQuestionnaire(row.id!)
    ElMessage.success('归档成功')
    fetchData()
  } catch (e) {
    ElMessage.error('归档失败')
  }
}

async function handleDelete(row: Questionnaire) {
  try {
    await deleteQuestionnaire(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function handleCopyLink(row: Questionnaire) {
  const link = `${window.location.origin}/public/questionnaire/${row.publicToken}`
  navigator.clipboard.writeText(link).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.questionnaire-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
