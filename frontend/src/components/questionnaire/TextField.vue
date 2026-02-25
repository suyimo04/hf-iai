<template>
  <div class="text-field">
    <el-input
      v-if="!isTextarea"
      :model-value="modelValue"
      :disabled="disabled"
      :placeholder="placeholder"
      :maxlength="maxLength"
      show-word-limit
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <el-input
      v-else
      type="textarea"
      :model-value="modelValue"
      :disabled="disabled"
      :placeholder="placeholder"
      :maxlength="maxLength"
      :rows="rows"
      show-word-limit
      @update:model-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'TextField'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: any]
}>()

const isTextarea = computed(() => {
  return props.field.validationRules?.multiline === true
})

const placeholder = computed(() => {
  return props.field.validationRules?.placeholder || `请输入${props.field.label}`
})

const maxLength = computed(() => {
  return props.field.validationRules?.maxLength || (isTextarea.value ? 500 : 100)
})

const rows = computed(() => {
  return props.field.validationRules?.rows || 4
})
</script>

<style scoped>
.text-field {
  width: 100%;
}

.text-field :deep(.el-input),
.text-field :deep(.el-textarea) {
  width: 100%;
}
</style>
