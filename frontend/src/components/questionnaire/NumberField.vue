<template>
  <div class="number-field">
    <el-input-number
      :model-value="modelValue"
      :disabled="disabled"
      :min="minValue"
      :max="maxValue"
      :step="step"
      :precision="precision"
      :placeholder="placeholder"
      :controls="showControls"
      style="width: 100%"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'NumberField'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: any]
}>()

const minValue = computed(() => {
  return props.field.validationRules?.min ?? -Infinity
})

const maxValue = computed(() => {
  return props.field.validationRules?.max ?? Infinity
})

const step = computed(() => {
  return props.field.validationRules?.step || 1
})

const precision = computed(() => {
  return props.field.validationRules?.precision
})

const placeholder = computed(() => {
  return props.field.validationRules?.placeholder || `请输入${props.field.label}`
})

const showControls = computed(() => {
  return props.field.validationRules?.controls !== false
})
</script>

<style scoped>
.number-field {
  width: 100%;
}

.number-field :deep(.el-input-number) {
  width: 100%;
}

.number-field :deep(.el-input-number .el-input__wrapper) {
  width: 100%;
}
</style>
