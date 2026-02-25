<template>
  <el-button
    :type="type"
    :size="size"
    :loading="loading"
    :disabled="disabled"
    :plain="plain"
    :round="round"
    class="base-button"
    :class="[`base-button--${type || 'default'}`]"
  >
    <slot></slot>
  </el-button>
</template>

<script setup lang="ts">
defineOptions({
  name: 'BaseButton'
})

withDefaults(defineProps<{
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'default' | ''
  size?: 'large' | 'default' | 'small'
  loading?: boolean
  disabled?: boolean
  plain?: boolean
  round?: boolean
}>(), {
  type: 'default',
  size: 'default',
  loading: false,
  disabled: false,
  plain: false,
  round: false
})
</script>

<style scoped>
.base-button {
  --el-button-border-radius: var(--border-radius-sm, 8px);
  font-weight: 500;
  transition: all 0.3s ease;
}

.base-button--primary {
  --el-button-bg-color: var(--color-primary, #10b981);
  --el-button-border-color: var(--color-primary, #10b981);
  --el-button-hover-bg-color: var(--color-primary-light, #34d399);
  --el-button-hover-border-color: var(--color-primary-light, #34d399);
  --el-button-active-bg-color: var(--color-primary-dark, #059669);
  --el-button-active-border-color: var(--color-primary-dark, #059669);
}

.base-button--primary:not(.is-disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.base-button--default {
  --el-button-bg-color: rgba(255, 255, 255, 0.8);
  --el-button-border-color: rgba(0, 0, 0, 0.08);
  --el-button-hover-bg-color: rgba(255, 255, 255, 0.95);
  --el-button-hover-border-color: var(--color-primary-light, #34d399);
  --el-button-hover-text-color: var(--color-primary, #10b981);
}

.base-button--success {
  --el-button-bg-color: var(--color-primary, #10b981);
  --el-button-border-color: var(--color-primary, #10b981);
  --el-button-hover-bg-color: var(--color-primary-light, #34d399);
  --el-button-hover-border-color: var(--color-primary-light, #34d399);
}

.base-button.is-plain.base-button--primary {
  --el-button-bg-color: rgba(16, 185, 129, 0.1);
  --el-button-text-color: var(--color-primary, #10b981);
  --el-button-hover-bg-color: rgba(16, 185, 129, 0.2);
  --el-button-hover-text-color: var(--color-primary, #10b981);
}
</style>
