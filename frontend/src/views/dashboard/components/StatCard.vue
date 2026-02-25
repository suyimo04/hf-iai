<template>
  <div class="stat-card">
    <div class="stat-card__icon" :style="{ background: iconBg }">
      <el-icon :size="24" :color="iconColor">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="stat-card__content">
      <div class="stat-card__value">{{ formattedValue }}</div>
      <div class="stat-card__title">{{ title }}</div>
      <div v-if="subtitle" class="stat-card__subtitle">{{ subtitle }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'StatCard'
})

const props = withDefaults(defineProps<{
  title: string
  value: number
  icon: string
  subtitle?: string
  iconColor?: string
  iconBg?: string
}>(), {
  iconColor: '#409eff',
  iconBg: 'rgba(64, 158, 255, 0.1)'
})

const formattedValue = computed(() => {
  if (props.value >= 10000) {
    return (props.value / 10000).toFixed(1) + 'w'
  }
  return props.value.toLocaleString()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-card__content {
  flex: 1;
  min-width: 0;
}

.stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.stat-card__title {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.stat-card__subtitle {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}
</style>
