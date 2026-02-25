<template>
  <div class="editable-cell" :class="{ 'is-modified': isModified }" @click="startEdit">
    <template v-if="!editing">
      <span class="cell-value">{{ displayValue }}</span>
      <el-icon class="edit-icon"><Edit /></el-icon>
    </template>
    <template v-else>
      <el-input
        v-if="type === 'text'"
        ref="inputRef"
        v-model="localValue"
        size="small"
        @blur="finishEdit"
        @keyup.enter="finishEdit"
      />
      <el-input-number
        v-else
        ref="inputRef"
        v-model="localValue"
        size="small"
        :min="min"
        :max="max"
        :precision="precision"
        :controls="false"
        @blur="finishEdit"
        @keyup.enter="finishEdit"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { Edit } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue: string | number
  type?: 'text' | 'number'
  min?: number
  max?: number
  precision?: number
  originalValue?: string | number
}>(), {
  type: 'text',
  min: 0,
  max: 99999,
  precision: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  'change': [value: string | number]
}>()

const editing = ref(false)
const localValue = ref<string | number>(props.modelValue)
const inputRef = ref<HTMLInputElement | null>(null)

const displayValue = computed(() => {
  if (props.type === 'number') {
    return typeof props.modelValue === 'number' ? props.modelValue : '-'
  }
  return props.modelValue || '-'
})

const isModified = computed(() => {
  if (props.originalValue === undefined) return false
  return props.modelValue !== props.originalValue
})

watch(() => props.modelValue, (val) => {
  localValue.value = val
})

function startEdit() {
  editing.value = true
  nextTick(() => {
    const input = inputRef.value as any
    input?.focus?.()
  })
}

function finishEdit() {
  editing.value = false
  if (localValue.value !== props.modelValue) {
    emit('update:modelValue', localValue.value)
    emit('change', localValue.value)
  }
}
</script>

<style scoped>
.editable-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  min-height: 32px;
  transition: background-color 0.2s;
}

.editable-cell:hover {
  background-color: var(--el-fill-color-light);
}

.editable-cell.is-modified {
  background-color: var(--el-color-warning-light-9);
}

.editable-cell.is-modified:hover {
  background-color: var(--el-color-warning-light-8);
}

.cell-value {
  flex: 1;
}

.edit-icon {
  opacity: 0;
  transition: opacity 0.2s;
  color: var(--el-color-primary);
}

.editable-cell:hover .edit-icon {
  opacity: 1;
}

:deep(.el-input),
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>
