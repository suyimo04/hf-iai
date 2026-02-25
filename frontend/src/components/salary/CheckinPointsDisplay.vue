<template>
  <el-card class="checkin-points-card">
    <template #header>
      <span>签到积分规则</span>
    </template>

    <!-- 规则表格 -->
    <el-table :data="rules" border size="small">
      <el-table-column prop="range" label="月签到次数" align="center" />
      <el-table-column prop="points" label="积分" align="center">
        <template #default="{ row }">
          <span :class="getValueClass(row.points)">
            {{ formatValue(row.points) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="coins" label="迷你币" align="center">
        <template #default="{ row }">
          <span :class="getValueClass(row.coins)">
            {{ formatValue(row.coins) }}
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 当前签到情况 -->
    <div v-if="currentCheckin" class="current-status">
      <el-divider />
      <div class="status-row">
        <span class="status-item">
          本月签到: <b>{{ currentCheckin.count }}</b> 次
        </span>
        <span class="status-item">
          预计积分: <b :class="getValueClass(currentCheckin.reward.points)">
            {{ formatValue(currentCheckin.reward.points) }}
          </b>
        </span>
        <span class="status-item">
          预计迷你币: <b :class="getValueClass(currentCheckin.reward.coins)">
            {{ formatValue(currentCheckin.reward.coins) }}
          </b>
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineOptions({
  name: 'CheckinPointsDisplay'
})

// 签到奖励类型
interface CheckinReward {
  points: number
  coins: number
}

// 当前签到情况类型
interface CurrentCheckin {
  count: number
  reward: CheckinReward
}

// Props
withDefaults(defineProps<{
  currentCheckin?: CurrentCheckin
}>(), {
  currentCheckin: undefined
})

// 签到积分规则
const rules = [
  { range: '< 20', points: -20, coins: -40 },
  { range: '20-29', points: -10, coins: -20 },
  { range: '30-39', points: 0, coins: 0 },
  { range: '40-49', points: 30, coins: 60 },
  { range: '≥ 50', points: 50, coins: 100 }
]

// 获取数值样式类
const getValueClass = (value: number): string => {
  if (value > 0) return 'positive'
  if (value < 0) return 'negative'
  return 'neutral'
}

// 格式化数值显示
const formatValue = (value: number): string => {
  if (value > 0) return `+${value}`
  return String(value)
}
</script>

<style scoped>
.checkin-points-card {
  --el-card-padding: 16px;
}

.checkin-points-card :deep(.el-card__header) {
  padding: 12px 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1f2937);
}

.positive {
  color: var(--el-color-success, #10b981);
  font-weight: 600;
}

.negative {
  color: var(--el-color-danger, #ef4444);
  font-weight: 600;
}

.neutral {
  color: var(--el-text-color-secondary, #6b7280);
}

.current-status {
  margin-top: 8px;
}

.current-status :deep(.el-divider) {
  margin: 16px 0;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  justify-content: center;
  padding: 8px 0;
}

.status-item {
  color: var(--el-text-color-regular, #4b5563);
  font-size: 14px;
}

.status-item b {
  margin-left: 4px;
}
</style>
