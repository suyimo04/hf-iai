<template>
  <div v-if="isVisible" class="field-renderer">
    <el-form-item
      :label="field.label"
      :required="field.required"
      :prop="field.fieldKey"
      :rules="validationRules"
    >
      <component
        :is="fieldComponent"
        :field="field"
        :model-value="modelValue"
        :disabled="disabled"
        :all-fields="allFields"
        :all-values="allValues"
        @update:model-value="$emit('update:modelValue', $event)"
      />
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import type { QuestionnaireField } from '@/api/questionnaire'
import SingleChoiceField from './SingleChoiceField.vue'
import MultipleChoiceField from './MultipleChoiceField.vue'
import TextField from './TextField.vue'
import DateField from './DateField.vue'
import NumberField from './NumberField.vue'
import DropdownField from './DropdownField.vue'
import GroupField from './GroupField.vue'

defineOptions({
  name: 'FieldRenderer'
})

const props = defineProps<{
  field: QuestionnaireField
  modelValue: any
  disabled?: boolean
  allFields?: QuestionnaireField[]
  allValues?: Record<string, any>
}>()

defineEmits<{
  'update:modelValue': [value: any]
}>()

// 字段类型到组件的映射
const componentMap: Record<string, Component> = {
  SINGLE_CHOICE: SingleChoiceField,
  MULTIPLE_CHOICE: MultipleChoiceField,
  TEXT: TextField,
  DATE: DateField,
  NUMBER: NumberField,
  DROPDOWN: DropdownField,
  GROUP: GroupField
}

// 根据字段类型获取对应组件
const fieldComponent = computed(() => {
  return componentMap[props.field.fieldType] || TextField
})

// 条件逻辑判断 - 决定字段是否显示
const isVisible = computed(() => {
  if (!props.field.conditionLogic || !props.allValues) return true
  return evaluateCondition(props.field.conditionLogic, props.allValues)
})

// 生成校验规则
const validationRules = computed(() => {
  const rules: any[] = []

  // 必填校验
  if (props.field.required) {
    rules.push({
      required: true,
      message: `${props.field.label}不能为空`,
      trigger: props.field.fieldType === 'TEXT' ? 'blur' : 'change'
    })
  }

  // 自定义校验规则
  const customRules = props.field.validationRules
  if (customRules) {
    // 最小长度
    if (customRules.minLength) {
      rules.push({
        min: customRules.minLength,
        message: `${props.field.label}至少${customRules.minLength}个字符`,
        trigger: 'blur'
      })
    }

    // 最大长度
    if (customRules.maxLength) {
      rules.push({
        max: customRules.maxLength,
        message: `${props.field.label}最多${customRules.maxLength}个字符`,
        trigger: 'blur'
      })
    }

    // 正则校验
    if (customRules.pattern) {
      rules.push({
        pattern: new RegExp(customRules.pattern),
        message: customRules.patternMessage || `${props.field.label}格式不正确`,
        trigger: 'blur'
      })
    }

    // 邮箱校验
    if (customRules.email) {
      rules.push({
        type: 'email',
        message: '请输入有效的邮箱地址',
        trigger: 'blur'
      })
    }

    // 手机号校验
    if (customRules.phone) {
      rules.push({
        pattern: /^1[3-9]\d{9}$/,
        message: '请输入有效的手机号',
        trigger: 'blur'
      })
    }
  }

  return rules
})

// 条件逻辑评估函数
function evaluateCondition(logic: Record<string, any>, values: Record<string, any>): boolean {
  const { field, operator, value, conditions, logicType } = logic

  // 复合条件 (AND/OR)
  if (conditions && Array.isArray(conditions)) {
    if (logicType === 'OR') {
      return conditions.some(cond => evaluateCondition(cond, values))
    }
    // 默认 AND
    return conditions.every(cond => evaluateCondition(cond, values))
  }

  // 单一条件
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
    case 'greaterThan':
      return typeof fieldValue === 'number' && fieldValue > value
    case 'lessThan':
      return typeof fieldValue === 'number' && fieldValue < value
    default:
      return true
  }
}
</script>

<style scoped>
.field-renderer {
  width: 100%;
}

.field-renderer :deep(.el-form-item) {
  margin-bottom: 0;
}

.field-renderer :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}
</style>
