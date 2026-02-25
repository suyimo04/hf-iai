<template>
  <div class="dropdown-field">
    <el-select
      :model-value="modelValue"
      :disabled="disabled"
      :placeholder="placeholder"
      :multiple="isMultiple"
      :clearable="clearable"
      :filterable="filterable"
      style="width: 100%"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <el-option
        v-for="option in normalizedOptions"
        :key="option.value"
        :label="option.label"
        :value="option.value"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'DropdownField'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: any]
}>()

const normalizedOptions = computed(() => {
  if (!props.field.options) return []
  return props.field.options.map(opt => {
    if (typeof opt === 'string') {
      return { label: opt, value: opt }
    }
    return { label: opt.label || opt.value, value: opt.value }
  })
})

const placeholder = computed(() => {
  return props.field.validationRules?.placeholder || `请选择${props.field.label}`
})

const isMultiple = computed(() => {
  return props.field.validationRules?.multiple === true
})

const clearable = computed(() => {
  return props.field.validationRules?.clearable !== false
})

const filterable = computed(() => {
  return props.field.validationRules?.filterable === true
})
</script>

<style scoped>
.dropdown-field {
  width: 100%;
}
</style>
