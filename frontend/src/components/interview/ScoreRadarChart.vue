<template>
  <div class="score-radar-chart">
    <div ref="chartRef" class="chart-container"></div>
    <div class="score-details">
      <div class="total-score">
        <span class="label">总分</span>
        <span class="value" :class="scoreClass">{{ score.finalScore }}</span>
        <span class="max">/100</span>
      </div>
      <div class="dimensions">
        <div v-for="dim in dimensions" :key="dim.key" class="dimension-item">
          <span class="dim-label">{{ dim.label }}</span>
          <el-progress :percentage="dim.value * 4" :color="getColor(dim.value)" />
          <span class="dim-value">{{ dim.value }}/25</span>
        </div>
      </div>
    </div>
    <div class="evaluation" v-if="score.evaluation">
      <h4>AI评价</h4>
      <p>{{ score.evaluation }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'

interface Score {
  attitudeScore: number        // 处理态度 0-25
  ruleExecutionScore: number   // 执行群规能力 0-25
  emotionalControlScore: number // 情绪控制 0-25
  decisionRationalityScore: number // 决策合理性 0-25
  finalScore: number           // 总分 0-100
  evaluation: string           // AI评价
}

const props = defineProps<{ score: Score }>()

const chartRef = ref<HTMLDivElement>()
let chartInstance: ECharts | null = null

// 4个维度配置
const dimensions = computed(() => [
  { key: 'attitude', label: '处理态度', value: props.score.attitudeScore },
  { key: 'ruleExecution', label: '执行群规', value: props.score.ruleExecutionScore },
  { key: 'emotionalControl', label: '情绪控制', value: props.score.emotionalControlScore },
  { key: 'decisionRationality', label: '决策合理', value: props.score.decisionRationalityScore }
])

// 总分样式类
const scoreClass = computed(() => {
  const score = props.score.finalScore
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'average'
  return 'poor'
})

// 根据分数获取颜色
const getColor = (value: number): string => {
  if (value >= 20) return '#67c23a'  // 优秀 - 绿色
  if (value >= 15) return '#409eff'  // 良好 - 蓝色
  if (value >= 10) return '#e6a23c'  // 一般 - 橙色
  return '#f56c6c'                    // 较差 - 红色
}

// ECharts雷达图配置
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  chartInstance.setOption({
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: dimensions.value.map(d => ({ name: d.label, max: 25 })),
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#666',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: ['#e5e5e5']
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(64, 158, 255, 0.05)', 'rgba(64, 158, 255, 0.1)']
        }
      }
    },
    series: [{
      type: 'radar',
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        color: '#409eff',
        width: 2
      },
      itemStyle: {
        color: '#409eff'
      },
      data: [{
        value: dimensions.value.map(d => d.value),
        name: '评分',
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.3)'
        }
      }]
    }]
  })
}

// 处理窗口resize
const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})

// 监听score变化更新图表
watch(() => props.score, () => {
  updateChart()
}, { deep: true })
</script>

<style scoped>
.score-radar-chart {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.score-details {
  margin-top: 20px;
}

.total-score {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 20px;
}

.total-score .label {
  font-size: 14px;
  color: #666;
  margin-right: 8px;
}

.total-score .value {
  font-size: 36px;
  font-weight: bold;
}

.total-score .value.excellent {
  color: #67c23a;
}

.total-score .value.good {
  color: #409eff;
}

.total-score .value.average {
  color: #e6a23c;
}

.total-score .value.poor {
  color: #f56c6c;
}

.total-score .max {
  font-size: 14px;
  color: #999;
}

.dimensions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.dimension-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dim-label {
  flex-shrink: 0;
  width: 70px;
  font-size: 13px;
  color: #666;
}

.dimension-item :deep(.el-progress) {
  flex: 1;
}

.dim-value {
  flex-shrink: 0;
  width: 45px;
  font-size: 12px;
  color: #999;
  text-align: right;
}

.evaluation {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.evaluation h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.evaluation p {
  margin: 0;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
</style>
