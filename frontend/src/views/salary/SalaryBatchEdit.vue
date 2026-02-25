<template>
  <div class="salary-batch-edit">
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
        <h2>薪酬批量编辑</h2>
      </div>
      <div class="header-right">
        <el-date-picker
          v-model="period"
          type="month"
          placeholder="选择月份"
          value-format="YYYY-MM"
          :clearable="false"
          @change="handlePeriodChange"
        />
        <el-button type="primary" @click="handleGenerate" :loading="generating">
          生成月度薪酬
        </el-button>
      </div>
    </div>

    <!-- 汇总信息 -->
    <el-row :gutter="20" class="summary-row">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value">{{ summaryData.memberCount }}</div>
          <div class="summary-label">成员数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value">{{ summaryData.totalPoints }}</div>
          <div class="summary-label">总积分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value">{{ summaryData.totalSalary }}</div>
          <div class="summary-label">总薪酬</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :class="['summary-card', validationStatus.class]">
          <div class="summary-value">{{ validationStatus.text }}</div>
          <div class="summary-label">校验状态</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>薪酬明细</span>
          <div class="header-actions">
            <el-button type="warning" @click="handleValidate" :loading="validating">
              校验数据
            </el-button>
            <el-button type="primary" @click="handleSave" :loading="saving" :disabled="!canSave">
              保存全部
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" max-height="500">
        <el-table-column prop="username" label="用户名" width="120" fixed />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column label="基础积分" width="130">
          <template #default="{ row }">
            <el-input-number
              v-model="row.basePoints"
              :min="0"
              :max="1000"
              size="small"
              controls-position="right"
              @change="handleCellChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="奖励积分" width="130">
          <template #default="{ row }">
            <el-input-number
              v-model="row.bonusPoints"
              :min="0"
              :max="500"
              size="small"
              controls-position="right"
              @change="handleCellChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="扣除" width="130">
          <template #default="{ row }">
            <el-input-number
              v-model="row.deduction"
              :min="0"
              :max="500"
              size="small"
              controls-position="right"
              @change="handleCellChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="总积分" width="100" align="center">
          <template #default="{ row }">
            <span class="total-points">{{ calcTotalPoints(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="迷你币" width="130">
          <template #default="{ row }">
            <el-input-number
              v-model="row.miniCoins"
              :min="0"
              :max="1000"
              size="small"
              controls-position="right"
              @change="handleCellChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="薪酬" width="120" align="center">
          <template #default="{ row }">
            <span :class="getSalaryClass(row.salary)">{{ row.salary }}</span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <el-input
              v-model="row.remark"
              size="small"
              placeholder="备注"
              @change="handleCellChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 校验错误提示 -->
    <el-card v-if="validationErrors.length > 0" class="error-card">
      <template #header>
        <span class="error-title">校验错误 ({{ validationErrors.length }})</span>
      </template>
      <div class="error-list">
        <div v-for="(err, idx) in validationErrors" :key="idx" class="error-item">
          <el-icon class="error-icon"><WarningFilled /></el-icon>
          <span>ID {{ err.id }}: {{ err.field }} - {{ err.message }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSalaries,
  validateSalaries,
  batchSaveSalaries,
  generateSalary,
  type Salary,
  type SalaryQueryParams
} from '@/api/salary'

defineOptions({ name: 'SalaryBatchEdit' })

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const validating = ref(false)
const generating = ref(false)

const period = ref(getCurrentPeriod())
const tableData = ref<Salary[]>([])
const modifiedIds = ref<Set<number>>(new Set())
const validationErrors = ref<{ id: number; field: string; message: string }[]>([])
const isValidated = ref(false)

function getCurrentPeriod(): string {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const summaryData = computed(() => {
  const memberCount = tableData.value.length
  const totalPoints = tableData.value.reduce((sum, row) => sum + calcTotalPoints(row), 0)
  const totalSalary = tableData.value.reduce((sum, row) => sum + (row.salary || 0), 0)
  return { memberCount, totalPoints, totalSalary }
})

const validationStatus = computed(() => {
  if (validationErrors.value.length > 0) {
    return { text: '有错误', class: 'error' }
  }
  if (isValidated.value) {
    return { text: '已通过', class: 'success' }
  }
  return { text: '待校验', class: '' }
})

const canSave = computed(() => {
  return isValidated.value && validationErrors.value.length === 0 && modifiedIds.value.size > 0
})

function calcTotalPoints(row: Salary): number {
  return (row.basePoints || 0) + (row.bonusPoints || 0) - (row.deduction || 0)
}

function getSalaryClass(salary: number): string {
  if (salary >= 400) return 'salary-high'
  if (salary >= 200) return 'salary-medium'
  return 'salary-low'
}

function getStatusType(status: string): '' | 'success' | 'warning' | 'info' {
  const map: Record<string, '' | 'success' | 'warning' | 'info'> = {
    DRAFT: 'info',
    CONFIRMED: 'warning',
    PAID: 'success'
  }
  return map[status] || 'info'
}

function getStatusLabel(status: string): string {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    CONFIRMED: '已确认',
    PAID: '已发放'
  }
  return map[status] || status
}

async function fetchData() {
  loading.value = true
  try {
    const params: SalaryQueryParams = {
      period: period.value,
      page: 1,
      size: 100
    }
    const res = await getSalaries(params)
    tableData.value = res.data.data?.content || []
    modifiedIds.value.clear()
    validationErrors.value = []
    isValidated.value = false
  } catch (e) {
    console.error('获取薪酬数据失败', e)
  } finally {
    loading.value = false
  }
}

function handleCellChange(row: Salary) {
  modifiedIds.value.add(row.id)
  isValidated.value = false
}

function handlePeriodChange() {
  fetchData()
}

async function handleValidate() {
  if (modifiedIds.value.size === 0) {
    ElMessage.warning('没有修改的数据')
    return
  }

  validating.value = true
  try {
    const items = tableData.value
      .filter(row => modifiedIds.value.has(row.id))
      .map(row => ({
        id: row.id,
        basePoints: row.basePoints,
        bonusPoints: row.bonusPoints,
        deduction: row.deduction,
        miniCoins: row.miniCoins,
        remark: row.remark
      }))

    const res = await validateSalaries({ period: period.value, items })
    const result = res.data.data

    if (result?.valid) {
      validationErrors.value = []
      isValidated.value = true
      ElMessage.success('校验通过')
    } else {
      validationErrors.value = result?.errors || []
      isValidated.value = false
      ElMessage.error(`校验失败，共 ${validationErrors.value.length} 个错误`)
    }
  } catch (e) {
    console.error('校验失败', e)
    ElMessage.error('校验请求失败')
  } finally {
    validating.value = false
  }
}

async function handleSave() {
  if (!canSave.value) return

  try {
    await ElMessageBox.confirm('确定要保存所有修改吗？', '确认保存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    saving.value = true
    const items = tableData.value
      .filter(row => modifiedIds.value.has(row.id))
      .map(row => ({
        id: row.id,
        basePoints: row.basePoints,
        bonusPoints: row.bonusPoints,
        deduction: row.deduction,
        miniCoins: row.miniCoins,
        remark: row.remark
      }))

    await batchSaveSalaries({ period: period.value, items })
    ElMessage.success('保存成功')
    modifiedIds.value.clear()
    fetchData()
  } catch (e) {
    if ((e as any) !== 'cancel') {
      console.error('保存失败', e)
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

async function handleGenerate() {
  try {
    await ElMessageBox.confirm(
      `确定要生成 ${period.value} 的月度薪酬吗？这将覆盖现有草稿数据。`,
      '生成月度薪酬',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    generating.value = true
    await generateSalary(period.value)
    ElMessage.success('生成成功')
    fetchData()
  } catch (e) {
    if ((e as any) !== 'cancel') {
      console.error('生成失败', e)
      ElMessage.error('生成失败')
    }
  } finally {
    generating.value = false
  }
}

function handleBack() {
  router.back()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.salary-batch-edit {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 12px;
}

.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  text-align: center;
  padding: 10px 0;
}

.summary-card.success {
  border-color: #67c23a;
}

.summary-card.error {
  border-color: #f56c6c;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.summary-card.success .summary-value {
  color: #67c23a;
}

.summary-card.error .summary-value {
  color: #f56c6c;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.total-points {
  font-weight: 600;
  color: #409eff;
}

.salary-high {
  color: #67c23a;
  font-weight: 600;
}

.salary-medium {
  color: #e6a23c;
  font-weight: 600;
}

.salary-low {
  color: #909399;
}

.error-card {
  border-color: #f56c6c;
}

.error-title {
  color: #f56c6c;
  font-weight: 600;
}

.error-list {
  max-height: 200px;
  overflow-y: auto;
}

.error-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  color: #606266;
}

.error-item:last-child {
  border-bottom: none;
}

.error-icon {
  color: #f56c6c;
}
</style>
