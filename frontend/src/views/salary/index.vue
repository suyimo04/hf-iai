<template>
  <div class="salary-page">
    <div class="page-header">
      <h2>薪酬管理</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedPeriod"
          type="month"
          placeholder="选择月份"
          format="YYYY-MM"
          value-format="YYYY-MM"
          :clearable="false"
          @change="handlePeriodChange"
        />
        <el-button type="primary" @click="handleGenerate">
          生成月度薪酬
        </el-button>
        <el-badge :value="unsavedCount" :hidden="unsavedCount === 0" class="save-badge">
          <el-button
            type="success"
            :disabled="unsavedCount === 0"
            :loading="saving"
            @click="handleBatchSave"
          >
            批量保存
          </el-button>
        </el-badge>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="queryParams.status" placeholder="状态筛选" clearable @change="fetchData">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="已确认" value="CONFIRMED" />
        <el-option label="已发放" value="PAID" />
      </el-select>
      <span v-if="unsavedCount > 0" class="unsaved-tip">
        <el-icon><Warning /></el-icon>
        {{ unsavedCount }} 条记录未保存
      </span>
    </div>

    <SalaryTable
      :data="salaryList"
      :loading="loading"
      :edited-rows="editedRows"
      :error-rows="errorRows"
      @cell-change="onCellChange"
    />

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 校验结果对话框 -->
    <el-dialog v-model="validateDialogVisible" title="校验结果" width="500px">
      <div class="validate-result">
        <div class="result-item">
          <span>正式成员数：</span>
          <span :class="{ error: validateResult.memberCount < 5 }">
            {{ validateResult.memberCount }} / 5
          </span>
        </div>
        <div class="result-item">
          <span>薪酬总额：</span>
          <span :class="{ error: validateResult.totalSalary > 2000 }">
            ¥{{ validateResult.totalSalary }} / ¥2000
          </span>
        </div>
        <div v-if="validateResult.errors.length > 0" class="error-list">
          <p class="error-title">校验错误：</p>
          <ul>
            <li v-for="err in validateResult.errors" :key="`${err.id}-${err.field}`">
              {{ err.message }}
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button @click="validateDialogVisible = false">关闭</el-button>
        <el-button
          v-if="validateResult.valid"
          type="primary"
          :loading="saving"
          @click="confirmSave"
        >
          确认保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SalaryTable from './components/SalaryTable.vue'
import {
  getSalaries,
  validateSalaries,
  batchSaveSalaries,
  generateSalary,
  type Salary,
  type SalaryEditRequest,
  type SalaryValidateResponse
} from '@/api/salary'

// 当前选择的周期
const selectedPeriod = ref(getCurrentPeriod())

// 查询参数
const queryParams = reactive({
  status: '',
  page: 1,
  size: 20
})

// 数据状态
const salaryList = ref<Salary[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

// 修改缓存池
const editedRows = ref<Map<number, SalaryEditRequest>>(new Map())

// 错误行
const errorRows = ref<Set<number>>(new Set())

// 校验对话框
const validateDialogVisible = ref(false)
const validateResult = ref<SalaryValidateResponse>({
  valid: false,
  totalSalary: 0,
  memberCount: 0,
  errors: []
})

// 未保存数量
const unsavedCount = computed(() => editedRows.value.size)

// 获取当前周期 (YYYY-MM)
function getCurrentPeriod(): string {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getSalaries({
      period: selectedPeriod.value,
      status: queryParams.status || undefined,
      page: queryParams.page,
      size: queryParams.size
    })
    salaryList.value = res.data.data.content
    total.value = res.data.data.totalElements
  } catch (error) {
    console.error('获取薪酬列表失败', error)
  } finally {
    loading.value = false
  }
}

// 周期变更
function handlePeriodChange() {
  if (unsavedCount.value > 0) {
    ElMessageBox.confirm('切换周期将丢失未保存的修改，是否继续？', '提示', {
      type: 'warning'
    }).then(() => {
      editedRows.value.clear()
      errorRows.value.clear()
      queryParams.page = 1
      fetchData()
    }).catch(() => {})
  } else {
    queryParams.page = 1
    fetchData()
  }
}

// 单元格修改
function onCellChange(id: number, field: string, value: string | number) {
  const existing = editedRows.value.get(id) || { id }
  editedRows.value.set(id, { ...existing, [field]: value })
  // 清除该行的错误状态
  errorRows.value.delete(id)
}

// 生成月度薪酬
async function handleGenerate() {
  try {
    await ElMessageBox.confirm(
      `确定要生成 ${selectedPeriod.value} 的月度薪酬吗？`,
      '生成薪酬',
      { type: 'warning' }
    )
    loading.value = true
    await generateSalary(selectedPeriod.value)
    ElMessage.success('生成成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('生成薪酬失败', error)
    }
  } finally {
    loading.value = false
  }
}

// 批量保存（先校验）
async function handleBatchSave() {
  if (unsavedCount.value === 0) return

  saving.value = true
  errorRows.value.clear()

  try {
    const items = Array.from(editedRows.value.values())
    const res = await validateSalaries({
      period: selectedPeriod.value,
      items
    })
    validateResult.value = res.data.data

    // 标记错误行
    res.data.data.errors.forEach(err => {
      errorRows.value.add(err.id)
    })

    validateDialogVisible.value = true
  } catch (error) {
    console.error('校验失败', error)
  } finally {
    saving.value = false
  }
}

// 确认保存
async function confirmSave() {
  saving.value = true
  try {
    const items = Array.from(editedRows.value.values())
    await batchSaveSalaries({
      period: selectedPeriod.value,
      items
    })
    ElMessage.success('保存成功')
    editedRows.value.clear()
    errorRows.value.clear()
    validateDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.salary-page {
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
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.save-badge :deep(.el-badge__content) {
  top: 8px;
  right: 18px;
}

.filter-bar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.unsaved-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-warning);
  font-size: 14px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.validate-result {
  padding: 10px 0;
}

.result-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.result-item .error {
  color: var(--el-color-danger);
  font-weight: 600;
}

.error-list {
  margin-top: 16px;
}

.error-title {
  color: var(--el-color-danger);
  margin-bottom: 8px;
}

.error-list ul {
  padding-left: 20px;
  color: var(--el-color-danger);
}

.error-list li {
  margin-bottom: 4px;
}
</style>
