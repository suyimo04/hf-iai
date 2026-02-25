<template>
  <el-table
    :data="data"
    v-loading="loading"
    style="width: 100%"
    :row-class-name="getRowClassName"
  >
    <el-table-column prop="username" label="用户名" width="120" fixed="left" />
    <el-table-column prop="nickname" label="昵称" width="120">
      <template #default="{ row }">
        {{ row.nickname || '-' }}
      </template>
    </el-table-column>
    <el-table-column label="基础积分" width="120">
      <template #default="{ row }">
        <EditableCell
          :model-value="getCellValue(row.id, 'basePoints', row.basePoints)"
          type="number"
          :original-value="row.basePoints"
          :min="0"
          :max="9999"
          @change="(val) => onCellChange(row.id, 'basePoints', val)"
        />
      </template>
    </el-table-column>
    <el-table-column label="奖励积分" width="120">
      <template #default="{ row }">
        <EditableCell
          :model-value="getCellValue(row.id, 'bonusPoints', row.bonusPoints)"
          type="number"
          :original-value="row.bonusPoints"
          :min="0"
          :max="9999"
          @change="(val) => onCellChange(row.id, 'bonusPoints', val)"
        />
      </template>
    </el-table-column>
    <el-table-column label="扣除" width="100">
      <template #default="{ row }">
        <EditableCell
          :model-value="getCellValue(row.id, 'deduction', row.deduction)"
          type="number"
          :original-value="row.deduction"
          :min="0"
          :max="9999"
          @change="(val) => onCellChange(row.id, 'deduction', val)"
        />
      </template>
    </el-table-column>
    <el-table-column prop="totalPoints" label="总积分" width="100">
      <template #default="{ row }">
        <span class="total-points">{{ calculateTotalPoints(row) }}</span>
      </template>
    </el-table-column>
    <el-table-column label="迷你币" width="100">
      <template #default="{ row }">
        <EditableCell
          :model-value="getCellValue(row.id, 'miniCoins', row.miniCoins)"
          type="number"
          :original-value="row.miniCoins"
          :min="0"
          :max="9999"
          @change="(val) => onCellChange(row.id, 'miniCoins', val)"
        />
      </template>
    </el-table-column>
    <el-table-column prop="salary" label="工资" width="100">
      <template #default="{ row }">
        <span :class="{ 'salary-error': isSalaryError(row.id) }">
          ¥{{ row.salary }}
        </span>
      </template>
    </el-table-column>
    <el-table-column label="备注" min-width="150">
      <template #default="{ row }">
        <EditableCell
          :model-value="getCellValue(row.id, 'remark', row.remark || '')"
          type="text"
          :original-value="row.remark || ''"
          @change="(val) => onCellChange(row.id, 'remark', val)"
        />
      </template>
    </el-table-column>
    <el-table-column label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Salary, SalaryEditRequest } from '@/api/salary'
import EditableCell from './EditableCell.vue'

const props = defineProps<{
  data: Salary[]
  loading: boolean
  editedRows: Map<number, SalaryEditRequest>
  errorRows: Set<number>
}>()

const emit = defineEmits<{
  'cell-change': [id: number, field: string, value: string | number]
}>()

function getCellValue(id: number, field: string, originalValue: any): any {
  const edited = props.editedRows.get(id)
  if (edited && field in edited) {
    return (edited as any)[field]
  }
  return originalValue
}

function onCellChange(id: number, field: string, value: string | number) {
  emit('cell-change', id, field, value)
}

function calculateTotalPoints(row: Salary): number {
  const basePoints = getCellValue(row.id, 'basePoints', row.basePoints) as number
  const bonusPoints = getCellValue(row.id, 'bonusPoints', row.bonusPoints) as number
  const deduction = getCellValue(row.id, 'deduction', row.deduction) as number
  return basePoints + bonusPoints - deduction
}

function isSalaryError(id: number): boolean {
  return props.errorRows.has(id)
}

function getRowClassName({ row }: { row: Salary }): string {
  if (props.errorRows.has(row.id)) return 'error-row'
  if (props.editedRows.has(row.id)) return 'modified-row'
  return ''
}

const statusMap: Record<string, string> = {
  DRAFT: '草稿',
  CONFIRMED: '已确认',
  PAID: '已发放'
}

function getStatusLabel(status: string): string {
  return statusMap[status] || status
}

function getStatusType(status: string): '' | 'success' | 'warning' | 'info' {
  const typeMap: Record<string, '' | 'success' | 'warning' | 'info'> = {
    DRAFT: 'info',
    CONFIRMED: 'warning',
    PAID: 'success'
  }
  return typeMap[status] || 'info'
}
</script>

<style scoped>
.total-points {
  font-weight: 600;
  color: var(--el-color-primary);
}

.salary-error {
  color: var(--el-color-danger);
  font-weight: 600;
}

:deep(.error-row) {
  background-color: var(--el-color-danger-light-9) !important;
}

:deep(.modified-row) {
  background-color: var(--el-color-warning-light-9) !important;
}
</style>
