<template>
  <div v-if="isVisible" class="group-field">
    <div class="group-field__header">
      <h4 class="group-field__title">{{ field.label }}</h4>
      <span v-if="field.validationRules?.description" class="group-field__desc">
        {{ field.validationRules.description }}
      </span>
    </div>
    <div class="group-field__content">
      <div
        v-for="childField in childFields"
        :key="childField.fieldKey"
        class="group-field__item"
      >
        <FieldRenderer
          :field="childField"
          :model-value="getChildValue(childField.fieldKey)"
          :disabled="disabled"
          :all-values="allValues"
          @update:model-value="updateChildValue(childField.fieldKey, $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'
import FieldRenderer from './FieldRenderer.vue'

defineOptions({
  name: 'GroupField'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
  allFields?: QuestionnaireField[]
  allValues?: Record<string, any>
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()

// 获取该分组下的子字段
const childFields = computed(() => {
  if (!props.allFields) return []
  return props.allFields
    .filter(f => f.groupId === props.field.id)
    .sort((a, b) => a.sortOrder - b.sortOrder)
})

// 条件逻辑判断
const isVisible = computed(() => {
  if (!props.field.conditionLogic || !props.allValues) return true
  return evaluateCondition(props.field.conditionLogic, props.allValues)
})

// 获取子字段值
const getChildValue = (fieldKey: string) => {
  if (!props.modelValue || typeof props.modelValue !== 'object') return undefined
  return props.modelValue[fieldKey]
}

// 更新子字段值
const updateChildValue = (fieldKey: string, value: any) => {
  const newValue = { ...(props.modelValue || {}), [fieldKey]: value }
  emit('update:modelValue', newValue)
}

// 条件逻辑评估
function evaluateCondition(logic: Record<string, any>, values: Record<string, any>): boolean {
  const { field, operator, value } = logic
  if (!field || !operator) return true

  const fieldValue = values[field]

  switch (operator) {
    case 'equals':
      return fieldValue === value
    case 'notEquals':
      return fieldValue !== value
    case 'contains':
      return Array.isArray(fieldValue) ? fieldValue.includes(value) : false
    case 'notContains':
      return Array.isArray(fieldValue) ? !fieldValue.includes(value) : true
    case 'isEmpty':
      return !fieldValue || (Array.isArray(fieldValue) && fieldValue.length === 0)
    case 'isNotEmpty':
      return !!fieldValue && (!Array.isArray(fieldValue) || fieldValue.length > 0)
    default:
      return true
  }
}
</script>

<style scoped>
.group-field {
  width: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.group-field__header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.group-field__title {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.group-field__desc {
  font-size: 13px;
  color: #909399;
}

.group-field__content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-field__item {
  width: 100%;
}
</style>
