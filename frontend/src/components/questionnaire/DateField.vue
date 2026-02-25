<template>
  <div class="date-field">
    <el-date-picker
      :model-value="modelValue"
      :type="dateType"
      :disabled="disabled"
      :placeholder="placeholder"
      :format="displayFormat"
      :value-format="valueFormat"
      :disabled-date="disabledDate"
      style="width: 100%"
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'DateField'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: any]
}>()

const dateType = computed(() => {
  return props.field.validationRules?.dateType || 'date'
})

const placeholder = computed(() => {
  return props.field.validationRules?.placeholder || '请选择日期'
})

const displayFormat = computed(() => {
  const type = dateType.value
  if (type === 'datetime') return 'YYYY-MM-DD HH:mm:ss'
  if (type === 'month') return 'YYYY-MM'
  if (type === 'year') return 'YYYY'
  return 'YYYY-MM-DD'
})

const valueFormat = computed(() => {
  return props.field.validationRules?.valueFormat || displayFormat.value
})

const disabledDate = (date: Date) => {
  const rules = props.field.validationRules
  if (!rules) return false

  const now = new Date()
  now.setHours(0, 0, 0, 0)

  if (rules.disablePast && date < now) return true
  if (rules.disableFuture && date > now) return true

  if (rules.minDate) {
    const min = new Date(rules.minDate)
    if (date < min) return true
  }

  if (rules.maxDate) {
    const max = new Date(rules.maxDate)
    if (date > max) return true
  }

  return false
}
</script>

<style scoped>
.date-field {
  width: 100%;
}
</style>
