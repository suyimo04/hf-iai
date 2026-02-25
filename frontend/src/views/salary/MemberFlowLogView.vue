<template>
  <div class="member-flow-log-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成员流转记录</span>
          <div class="filters">
            <el-select v-model="filters.flowType" placeholder="流转类型" clearable @change="fetchLogs">
              <el-option label="转正" value="PROMOTION" />
              <el-option label="降级" value="DEMOTION" />
              <el-option label="开除" value="REMOVAL" />
            </el-select>
            <el-select v-model="filters.triggerType" placeholder="触发方式" clearable @change="fetchLogs">
              <el-option label="自动" value="AUTO" />
              <el-option label="手动" value="MANUAL" />
            </el-select>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="fetchLogs"
            />
          </div>
        </div>
      </template>

      <el-table :data="logs" v-loading="loading">
        <el-table-column prop="userName" label="成员" width="120" />
        <el-table-column prop="fromRole" label="原角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.fromRole)">{{ getRoleLabel(row.fromRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="" width="50" align="center">
          <template #default>
            <el-icon><Right /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="toRole" label="新角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.toRole)">{{ getRoleLabel(row.toRole) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="flowType" label="流转类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getFlowTypeTagType(row.flowType)">
              {{ getFlowTypeLabel(row.flowType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="triggerType" label="触发方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.triggerType === 'AUTO' ? 'warning' : 'primary'">
              {{ row.triggerType === 'AUTO' ? '自动' : '手动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchLogs"
          @current-change="fetchLogs"
        />
      </div>
    </el-card>

    <!-- 流转规则说明 -->
    <el-card class="rules-card">
      <template #header>流转规则</template>
      <div class="rules">
        <p><strong>转正条件：</strong>实习成员积分>=100分，经管理组审批</p>
        <p><strong>降级条件：</strong>正式成员连续2个月积分&lt;150分</p>
        <p><strong>开除条件：</strong>实习成员连续2个月积分&lt;100分（自动执行）</p>
        <p><strong>名额限制：</strong>始终保持5名正式成员</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Right } from '@element-plus/icons-vue'
import { getMemberFlowLogs, type MemberFlowLog } from '@/api/user'

const loading = ref(false)
const logs = ref<MemberFlowLog[]>([])

const filters = reactive({
  flowType: '',
  triggerType: '',
  dateRange: null as [string, string] | null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取角色标签类型
function getRoleTagType(role: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    ADMIN: 'danger',
    LEADER: 'danger',
    VICE_LEADER: 'warning',
    MEMBER: 'success',
    INTERN: 'info',
    REMOVED: ''
  }
  return map[role] || 'info'
}

// 获取角色显示名称
function getRoleLabel(role: string): string {
  const map: Record<string, string> = {
    ADMIN: '管理员',
    LEADER: '组长',
    VICE_LEADER: '副组长',
    MEMBER: '正式成员',
    INTERN: '实习成员',
    REMOVED: '已移除'
  }
  return map[role] || role
}

// 获取流转类型标签类型
function getFlowTypeTagType(flowType: string): '' | 'success' | 'warning' | 'danger' {
  const map: Record<string, '' | 'success' | 'warning' | 'danger'> = {
    PROMOTION: 'success',
    DEMOTION: 'warning',
    REMOVAL: 'danger'
  }
  return map[flowType] || ''
}

// 获取流转类型显示名称
function getFlowTypeLabel(flowType: string): string {
  const map: Record<string, string> = {
    PROMOTION: '转正',
    DEMOTION: '降级',
    REMOVAL: '开除'
  }
  return map[flowType] || flowType
}

// 获取流转日志
async function fetchLogs() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    }
    if (filters.flowType) params.flowType = filters.flowType
    if (filters.triggerType) params.triggerType = filters.triggerType
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }

    const res = await getMemberFlowLogs(params)
    logs.value = res.data.data.content
    pagination.total = res.data.data.totalElements
  } catch (error) {
    console.error('获取流转日志失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.member-flow-log-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
}

.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.rules-card {
  margin-top: 20px;
}

.rules p {
  margin: 8px 0;
  line-height: 1.6;
  color: var(--el-text-color-regular);
}

.rules strong {
  color: var(--el-text-color-primary);
}
</style>
