<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-6 mb-6">
      <StatCard
        title="用户总数"
        :value="stats.userCount"
        icon="User"
        icon-color="#409eff"
        icon-bg="rgba(64, 158, 255, 0.1)"
      />
      <StatCard
        title="待审核报名"
        :value="stats.pendingApplicationCount"
        icon="Document"
        icon-color="#e6a23c"
        icon-bg="rgba(230, 162, 60, 0.1)"
      />
      <StatCard
        title="进行中活动"
        :value="stats.activeActivityCount"
        icon="Calendar"
        icon-color="#67c23a"
        icon-bg="rgba(103, 194, 58, 0.1)"
      />
      <StatCard
        title="本月积分"
        :value="stats.monthlyPoints"
        icon="Medal"
        icon-color="#f56c6c"
        icon-bg="rgba(245, 108, 108, 0.1)"
      />
    </div>

    <!-- 图表 -->
    <div class="grid grid-cols-2 gap-6">
      <BaseCard title="用户增长趋势">
        <ChartPanel type="line" :data="userTrend" color="#409eff" />
      </BaseCard>
      <BaseCard title="积分发放统计">
        <ChartPanel type="bar" :data="pointsTrend" color="#67c23a" />
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Document, Calendar, Medal } from '@element-plus/icons-vue'
import StatCard from './components/StatCard.vue'
import ChartPanel from './components/ChartPanel.vue'
import { getDashboardStats, getUserTrend, getPointsTrend } from '@/api/dashboard'
import type { DashboardStats, TrendData } from '@/api/dashboard'

const stats = ref<DashboardStats>({
  userCount: 0,
  memberCount: 0,
  applicationCount: 0,
  pendingApplicationCount: 0,
  activityCount: 0,
  activeActivityCount: 0,
  totalPoints: 0,
  monthlyPoints: 0
})

const userTrend = ref<TrendData[]>([])
const pointsTrend = ref<TrendData[]>([])

const fetchStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchTrends = async () => {
  try {
    const [userRes, pointsRes] = await Promise.all([
      getUserTrend(7),
      getPointsTrend(7)
    ])
    userTrend.value = userRes.data.data
    pointsTrend.value = pointsRes.data.data
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

onMounted(() => {
  fetchStats()
  fetchTrends()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}
</style>
