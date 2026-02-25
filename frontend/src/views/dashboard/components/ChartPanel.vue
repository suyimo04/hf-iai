<template>
  <div ref="chartRef" class="chart-panel"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, shallowRef } from 'vue'
import * as echarts from 'echarts'
import type { TrendData } from '@/api/dashboard'

defineOptions({
  name: 'ChartPanel'
})

const props = withDefaults(defineProps<{
  type: 'line' | 'bar'
  data: TrendData[]
  color?: string
}>(), {
  color: '#409eff'
})

const chartRef = ref<HTMLElement>()
const chartInstance = shallowRef<echarts.ECharts>()

const getOption = (): echarts.EChartsOption => {
  const xData = props.data.map(item => item.date)
  const yData = props.data.map(item => item.value)

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280', fontSize: 12 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#6b7280', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } }
    },
    series: [{
      type: props.type,
      data: yData,
      smooth: props.type === 'line',
      itemStyle: { color: props.color },
      areaStyle: props.type === 'line' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: props.color + '40' },
          { offset: 1, color: props.color + '05' }
        ])
      } : undefined,
      barWidth: '40%',
      emphasis: { itemStyle: { color: props.color } }
    }]
  }
}

const initChart = () => {
  if (!chartRef.value) return
  chartInstance.value = echarts.init(chartRef.value)
  chartInstance.value.setOption(getOption())
}

const resizeChart = () => {
  chartInstance.value?.resize()
}

watch(() => props.data, () => {
  chartInstance.value?.setOption(getOption())
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance.value?.dispose()
})
</script>

<style scoped>
.chart-panel {
  width: 100%;
  height: 300px;
}
</style>
