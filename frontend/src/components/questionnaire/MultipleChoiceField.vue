<template>
  <div class="multiple-choice-field">
    <el-checkbox-group
      :model-value="modelValue || []"
      :disabled="disabled"
      @update:model-value="$emit('update:modelValue', $event)"
    >
      <el-checkbox
        v-for="option in normalizedOptions"
        :key="option.value"
        :value="option.value"
      >
        {{ option.label }}
      </el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'MultipleChoiceField'
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
.multiple-choice-field {
  width: 100%;
}

.multiple-choice-field :deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.multiple-choice-field :deep(.el-checkbox) {
  height: auto;
  line-height: 1.5;
}
</style>
