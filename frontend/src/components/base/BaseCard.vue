<template>
  <div class="base-card" :class="{ 'base-card--hoverable': hoverable }">
    <div v-if="title || $slots.extra" class="base-card__header">
      <h3 v-if="title" class="base-card__title">{{ title }}</h3>
      <div v-if="$slots.extra" class="base-card__extra">
        <slot name="extra"></slot>
      </div>
    </div>
    <div class="base-card__body" :class="{ 'base-card__body--no-header': !title && !$slots.extra }">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({
  name: 'BaseCard'
})

withDefaults(defineProps<{
  title?: string
  hoverable?: boolean
}>(), {
  hoverable: false
})
</script>

<style scoped>
.base-card {
  background: var(--color-card-bg, rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--color-card-border, rgba(255, 255, 255, 0.6));
  border-radius: var(--border-radius, 12px);
  box-shadow: var(--shadow-card, 0 4px 20px rgba(0, 0, 0, 0.05));
  transition: all 0.3s ease;
  overflow: hidden;
}

.base-card--hoverable:hover {
  box-shadow: var(--shadow-card-hover, 0 8px 30px rgba(0, 0, 0, 0.1));
  transform: translateY(-2px);
}

.base-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md, 16px) var(--spacing-lg, 24px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.base-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.base-card__extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.base-card__body {
  padding: var(--spacing-lg, 24px);
}

.base-card__body--no-header {
  padding: var(--spacing-lg, 24px);
}
</style>
