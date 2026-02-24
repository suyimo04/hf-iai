<template>
  <div class="single-choice-field">
    <el-radio-group
      :model-value="modelValue"
      :disabled="disabled"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <el-radio
        v-for="option in normalizedOptions"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </el-radio>
    </el-radio-group>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'SingleChoiceField'
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
</script>

<style scoped>
.single-choice-field {
  width: 100%;
}

.single-choice-field :deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.single-choice-field :deep(.el-radio) {
  height: auto;
  line-height: 1.5;
}
</style>
