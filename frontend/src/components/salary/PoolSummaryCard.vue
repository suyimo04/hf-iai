<template>
  <el-card class="pool-summary-card">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <span class="coin-icon">💰</span>
          <span class="header-title">薪酬池分配</span>
        </div>
        <el-tag :type="isOverLimit ? 'danger' : 'success'" effect="dark" round>
          {{ isOverLimit ? '超出限额' : '正常' }}
        </el-tag>
      </div>
    </template>

    <!-- 主进度环 -->
    <div class="pool-progress-section">
      <div class="progress-ring-container">
        <el-progress
          type="dashboard"
          :percentage="percentage"
          :color="progressColors"
          :stroke-width="12"
          :width="160"
        >
          <template #default>
            <div class="progress-inner">
              <div class="progress-value">{{ usedCoins }}</div>
              <div class="progress-label">/ {{ totalPool }}</div>
            </div>
          </template>
        </el-progress>
      </div>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="8">
        <div class="stat-item">
          <div class="stat-icon total">📊</div>
          <div class="stat-content">
            <div class="stat-value">{{ totalPool }}</div>
            <div class="stat-label">总池(迷你币)</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-item">
          <div class="stat-icon used">📤</div>
          <div class="stat-content">
            <div class="stat-value" :class="{ warning: usedCoins > totalPool }">{{ usedCoins }}</div>
            <div class="stat-label">已分配</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-item">
          <div class="stat-icon remaining">💎</div>
          <div class="stat-content">
            <div class="stat-value" :class="{ negative: remainingCoins < 0, positive: remainingCoins > 0 }">
              {{ remainingCoins }}
            </div>
            <div class="stat-label">剩余</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 成员分配列表 -->
    <div class="member-allocation" v-if="members.length">
      <el-divider>
        <span class="divider-text">成员分配 ({{ members.length }}/5人)</span>
      </el-divider>
      <div class="member-list">
        <div v-for="m in members" :key="m.id" class="member-row">
          <div class="member-info">
            <span class="member-avatar">{{ getInitial(m.nickname || m.username) }}</span>
            <span class="member-name">{{ m.nickname || m.username }}</span>
          </div>
          <div class="member-coins-wrapper">
            <div class="member-bar-bg">
              <div
                class="member-bar"
                :class="{ invalid: !isValidRange(m.coins) }"
                :style="{ width: getMemberBarWidth(m.coins) }"
              ></div>
            </div>
            <span class="member-coins" :class="{ invalid: !isValidRange(m.coins) }">
              {{ m.coins }}
              <el-tag v-if="!isValidRange(m.coins)" type="danger" size="small" effect="plain">
                超出范围
              </el-tag>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 规则提示 -->
    <div class="rules-tip">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          <span class="rule-title">分配规则</span>
        </template>
        <div class="rule-content">
          总池 <b>2000</b> 迷你币 · 仅 <b>5</b> 名正式成员 · 单人 <b>200-400</b> 迷你币
        </div>
      </el-alert>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'PoolSummaryCard'
})

// 成员类型
interface PoolMember {
  id: number
  username: string
  nickname?: string
  coins: number
}

// Props
const props = withDefaults(defineProps<{
  members: PoolMember[]
  totalPool?: number
}>(), {
  members: () => [],
  totalPool: 2000
})

// 计算属性
const totalPool = computed(() => props.totalPool)
const usedCoins = computed(() => props.members.reduce((sum, m) => sum + m.coins, 0))
const remainingCoins = computed(() => totalPool.value - usedCoins.value)
const percentage = computed(() => Math.min((usedCoins.value / totalPool.value) * 100, 100))
const isOverLimit = computed(() => usedCoins.value > totalPool.value)

// 进度条颜色
const progressColors = [
  { color: '#10b981', percentage: 60 },
  { color: '#f59e0b', percentage: 80 },
  { color: '#ef4444', percentage: 100 }
]

// 验证单人范围
const isValidRange = (coins: number): boolean => coins >= 200 && coins <= 400

// 获取成员名首字母
const getInitial = (name: string): string => {
  return name ? name.charAt(0).toUpperCase() : '?'
}

// 获取成员条形图宽度
const getMemberBarWidth = (coins: number): string => {
  const maxCoins = 400
  const width = Math.min((coins / maxCoins) * 100, 100)
  return `${width}%`
}
</script>

<style scoped>
.pool-summary-card {
  --el-card-padding: 20px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(240, 253, 244, 0.9) 100%);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 16px;
  overflow: hidden;
}

.pool-summary-card :deep(.el-card__header) {
  padding: 16px 20px;
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.08) 0%, rgba(16, 185, 129, 0.02) 100%);
  border-bottom: 1px solid rgba(16, 185, 129, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coin-icon {
  font-size: 20px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1f2937);
}

/* 进度环区域 */
.pool-progress-section {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.progress-ring-container {
  position: relative;
}

.progress-inner {
  text-align: center;
}

.progress-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary, #10b981);
  line-height: 1.2;
}

.progress-label {
  font-size: 14px;
  color: var(--el-text-color-secondary, #6b7280);
}

/* 统计信息 */
.stats-row {
  margin: 0 0 20px 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  font-size: 18px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.stat-icon.used {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.stat-icon.remaining {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary, #1f2937);
  line-height: 1.2;
}

.stat-value.warning {
  color: var(--el-color-warning, #f59e0b);
}

.stat-value.negative {
  color: var(--el-color-danger, #ef4444);
}

.stat-value.positive {
  color: var(--el-color-success, #10b981);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary, #6b7280);
  margin-top: 2px;
}

/* 成员分配 */
.member-allocation {
  margin-top: 8px;
}

.member-allocation :deep(.el-divider) {
  margin: 16px 0;
}

.divider-text {
  font-size: 13px;
  color: var(--el-text-color-secondary, #6b7280);
  font-weight: 500;
}

.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.member-row:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(16, 185, 129, 0.2);
}

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 100px;
}

.member-avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--el-color-primary-light-7) 0%, var(--el-color-primary-light-5) 100%);
  color: var(--el-color-primary-dark-2, #059669);
  border-radius: 50%;
  font-size: 14px;
  font-weight: 600;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary, #1f2937);
}

.member-coins-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 200px;
}

.member-bar-bg {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 4px;
  overflow: hidden;
}

.member-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--el-color-primary-light-3) 0%, var(--el-color-primary) 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.member-bar.invalid {
  background: linear-gradient(90deg, #fca5a5 0%, #ef4444 100%);
}

.member-coins {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary, #1f2937);
  min-width: 80px;
  justify-content: flex-end;
}

.member-coins.invalid {
  color: var(--el-color-danger, #ef4444);
}

/* 规则提示 */
.rules-tip {
  margin-top: 20px;
}

.rules-tip :deep(.el-alert) {
  background: rgba(16, 185, 129, 0.06);
  border: 1px solid rgba(16, 185, 129, 0.15);
  border-radius: 10px;
}

.rules-tip :deep(.el-alert__title) {
  font-size: 13px;
}

.rule-title {
  font-weight: 600;
  color: var(--el-color-primary-dark-2, #059669);
}

.rule-content {
  font-size: 13px;
  color: var(--el-text-color-regular, #4b5563);
  margin-top: 4px;
}

.rule-content b {
  color: var(--el-color-primary, #10b981);
  font-weight: 600;
}
</style>
