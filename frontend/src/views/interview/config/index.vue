<template>
  <div class="interview-config-page">
    <div class="page-header">
      <h2>题库配置</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增题目</el-button>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filterForm" inline>
        <el-form-item label="分类">
          <el-select v-model="filterForm.category" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="filterForm.questionType" placeholder="全部题型" clearable style="width: 140px">
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="问答题" value="TEXT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.enabled" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
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
        <el-table-column label="分类" prop="category" width="120" />
        <el-table-column label="题目内容" prop="content" min-width="250" show-overflow-tooltip />
        <el-table-column label="题型" prop="questionType" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getTypeTagType(row.questionType)">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分值" prop="score" width="80" align="center" />
        <el-table-column label="排序" prop="sort" width="80" align="center" />
        <el-table-column label="状态" prop="enabled" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleEnabled(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该题目吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 编辑弹窗 -->
    <QuestionDialog
      v-model="dialogVisible"
      :question="currentQuestion"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QuestionDialog from './QuestionDialog.vue'
import {
  getQuestions,
  updateQuestion,
  deleteQuestion,
  getQuestionCategories,
  type Question,
  type QuestionType
} from '@/api/interview'

const loading = ref(false)
const tableData = ref<Question[]>([])
const categories = ref<string[]>([])
const dialogVisible = ref(false)
const currentQuestion = ref<Question | null>(null)

const filterForm = reactive({
  category: '',
  questionType: '' as QuestionType | '',
  enabled: undefined as boolean | undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

function getQuestionTypeText(type: QuestionType) {
  const map: Record<QuestionType, string> = {
    SINGLE_CHOICE: '单选题',
    MULTIPLE_CHOICE: '多选题',
    TEXT: '问答题'
  }
  return map[type] || type
}

function getTypeTagType(type: QuestionType) {
  const map: Record<QuestionType, string> = {
    SINGLE_CHOICE: '',
    MULTIPLE_CHOICE: 'success',
    TEXT: 'warning'
  }
  return map[type] || ''
}

async function fetchCategories() {
  try {
    const res = await getQuestionCategories()
    categories.value = res.data.data
  } catch {
    categories.value = []
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      category: filterForm.category || undefined,
      questionType: filterForm.questionType || undefined,
      enabled: filterForm.enabled
    }
    const res = await getQuestions(params)
    tableData.value = res.data.data.content
    pagination.total = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  filterForm.category = ''
  filterForm.questionType = ''
  filterForm.enabled = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  currentQuestion.value = null
  dialogVisible.value = true
}

function handleEdit(row: Question) {
  currentQuestion.value = row
  dialogVisible.value = true
}

async function handleToggleEnabled(row: Question) {
  try {
    await updateQuestion(row.id, { enabled: row.enabled })
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch {
    row.enabled = !row.enabled
  }
}

async function handleDelete(row: Question) {
  try {
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // error handled by request interceptor
  }
}

onMounted(() => {
  fetchCategories()
  fetchData()
})
</script>

<style scoped>
.interview-config-page {
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

.filter-card {
  margin-bottom: 16px;
}

.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
