<template>
  <div class="field-property-editor">
    <h3 class="editor-title">属性编辑</h3>

    <el-form label-position="top" size="default">
      <!-- 基础属性 -->
      <el-form-item label="字段标识">
        <el-input v-model="localField.fieldKey" placeholder="唯一标识" />
      </el-form-item>

      <el-form-item label="字段标题">
        <el-input v-model="localField.label" placeholder="显示标题" />
      </el-form-item>

      <el-form-item label="字段类型">
        <el-select v-model="localField.fieldType" disabled style="width: 100%">
          <el-option label="单行文本" value="TEXT" />
          <el-option label="单选" value="SINGLE_CHOICE" />
          <el-option label="多选" value="MULTIPLE_CHOICE" />
          <el-option label="下拉选择" value="DROPDOWN" />
          <el-option label="日期" value="DATE" />
          <el-option label="数字" value="NUMBER" />
          <el-option label="分组" value="GROUP" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-checkbox v-model="localField.required">必填</el-checkbox>
      </el-form-item>

      <!-- 选项配置（单选、多选、下拉） -->
      <template v-if="hasOptions">
        <el-divider>选项配置</el-divider>
        <div class="options-editor">
          <div v-for="(opt, idx) in localOptions" :key="idx" class="option-item">
            <el-input v-model="localOptions[idx]" placeholder="选项内容" />
            <el-button type="danger" :icon="Delete" circle size="small" @click="removeOption(idx)" />
          </div>
          <el-button type="primary" :icon="Plus" @click="addOption">添加选项</el-button>
        </div>
      </template>

      <!-- 文本字段配置 -->
      <template v-if="localField.fieldType === 'TEXT'">
        <el-divider>文本配置</el-divider>
        <el-form-item>
          <el-checkbox v-model="validationRules.multiline">多行文本</el-checkbox>
        </el-form-item>
        <el-form-item label="占位提示">
          <el-input v-model="validationRules.placeholder" placeholder="请输入..." />
        </el-form-item>
        <el-form-item label="最大长度">
          <el-input-number v-model="validationRules.maxLength" :min="1" :max="2000" />
        </el-form-item>
        <el-form-item label="最小长度">
          <el-input-number v-model="validationRules.minLength" :min="0" :max="2000" />
        </el-form-item>
      </template>

      <!-- 数字字段配置 -->
      <template v-if="localField.fieldType === 'NUMBER'">
        <el-divider>数字配置</el-divider>
        <el-form-item label="最小值">
          <el-input-number v-model="validationRules.min" />
        </el-form-item>
        <el-form-item label="最大值">
          <el-input-number v-model="validationRules.max" />
        </el-form-item>
        <el-form-item label="精度">
          <el-input-number v-model="validationRules.precision" :min="0" :max="10" />
        </el-form-item>
      </template>

      <!-- 日期字段配置 -->
      <template v-if="localField.fieldType === 'DATE'">
        <el-divider>日期配置</el-divider>
        <el-form-item label="日期格式">
          <el-select v-model="validationRules.format" style="width: 100%">
            <el-option label="年-月-日" value="YYYY-MM-DD" />
            <el-option label="年-月-日 时:分" value="YYYY-MM-DD HH:mm" />
            <el-option label="年-月" value="YYYY-MM" />
          </el-select>
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import type { QuestionnaireField } from '@/api/questionnaire'

defineOptions({
  name: 'FieldPropertyEditor'
})

const props = defineProps<{
  field: QuestionnaireField
}>()

const emit = defineEmits<{
  update: [field: QuestionnaireField]
}>()

// 本地副本
const localField = ref<QuestionnaireField>({ ...props.field })
const validationRules = ref<Record<string, any>>({ ...props.field.validationRules })
const localOptions = ref<string[]>([])

// 是否有选项配置
const hasOptions = computed(() => {
  return ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'DROPDOWN'].includes(localField.value.fieldType)
})

// 初始化选项
watch(() => props.field, (newField) => {
  localField.value = { ...newField }
  validationRules.value = { ...newField.validationRules }
  if (newField.options) {
    localOptions.value = newField.options.map((o: any) =>
      typeof o === 'string' ? o : o.label || o.value
    )
  } else {
    localOptions.value = []
  }
}, { immediate: true, deep: true })

// 监听变化并触发更新
watch([localField, validationRules, localOptions], () => {
  const updated: QuestionnaireField = {
    ...localField.value,
    validationRules: { ...validationRules.value },
    options: hasOptions.value ? localOptions.value.map(opt => ({ label: opt, value: opt })) : undefined
  }
  emit('update', updated)
}, { deep: true })

// 添加选项
function addOption() {
  localOptions.value.push(`选项${localOptions.value.length + 1}`)
}

// 删除选项
function removeOption(index: number) {
  localOptions.value.splice(index, 1)
}
</script>

<style scoped>
.field-property-editor {
  padding: 16px;
  height: 100%;
  overflow-y: auto;
}

.editor-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.options-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  gap: 8px;
  align-items: center;
}

.option-item .el-input {
  flex: 1;
}

:deep(.el-form-item) {
  margin-bottom: 12px;
}

:deep(.el-divider) {
  margin: 16px 0;
}
</style>
