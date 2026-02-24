<template>
  <div class="designer-container">
    <!-- 左侧组件库 -->
    <div class="component-library">
      <h3 class="panel-title">组件库</h3>
      <draggable
        :list="componentTypes"
        :group="{ name: 'fields', pull: 'clone', put: false }"
        :clone="cloneField"
        item-key="type"
        :sort="false"
      >
        <template #item="{ element }">
          <div class="component-item">
            <el-icon><component :is="element.icon" /></el-icon>
            <span>{{ element.label }}</span>
          </div>
        </template>
      </draggable>
    </div>

    <!-- 中间设计区 -->
    <div class="design-area">
      <div class="questionnaire-header">
        <el-input
          v-model="questionnaire.title"
          placeholder="请输入问卷标题"
          class="title-input"
        />
        <el-input
          v-model="questionnaire.description"
          type="textarea"
          placeholder="请输入问卷描述（可选）"
          :rows="2"
        />
      </div>

      <div class="fields-container">
        <draggable
          v-model="questionnaire.fields"
          group="fields"
          item-key="fieldKey"
          :animation="200"
          ghost-class="ghost-field"
          @change="onFieldChange"
        >
          <template #item="{ element, index }">
            <div
              class="field-item"
              :class="{ active: selectedIndex === index }"
              @click="selectField(index)"
            >
              <div class="field-header">
                <span class="field-type-tag">{{ getFieldTypeLabel(element.fieldType) }}</span>
                <span v-if="element.required" class="required-tag">必填</span>
              </div>
              <FieldRenderer :field="element" :model-value="null" disabled />
              <div class="field-actions">
                <el-button size="small" :icon="CopyDocument" @click.stop="copyField(index)" />
                <el-button size="small" type="danger" :icon="Delete" @click.stop="removeField(index)" />
              </div>
            </div>
          </template>
        </draggable>

        <!-- 空状态提示 -->
        <div v-if="questionnaire.fields.length === 0" class="empty-placeholder">
          <el-icon :size="48"><DocumentAdd /></el-icon>
          <p>从左侧拖拽组件到此处</p>
        </div>
      </div>
    </div>

    <!-- 右侧属性编辑器 -->
    <div class="property-editor">
      <FieldPropertyEditor
        v-if="selectedField"
        :field="selectedField"
        @update="updateField"
      />
      <div v-else class="no-selection">
        <el-icon :size="48"><Setting /></el-icon>
        <p>选择一个字段以编辑属性</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import draggable from 'vuedraggable'
import {
  Delete,
  CopyDocument,
  DocumentAdd,
  Setting,
  EditPen,
  Select,
  List,
  Calendar,
  Histogram,
  ArrowDown,
  Folder
} from '@element-plus/icons-vue'
import type { QuestionnaireField, FieldType } from '@/api/questionnaire'
import { FieldRenderer } from '@/components/questionnaire'
import FieldPropertyEditor from '@/components/questionnaire/FieldPropertyEditor.vue'

defineOptions({
  name: 'QuestionnaireDesigner'
})

// 组件类型定义
interface ComponentType {
  type: FieldType
  label: string
  icon: any
}

const componentTypes: ComponentType[] = [
  { type: 'TEXT', label: '单行文本', icon: EditPen },
  { type: 'SINGLE_CHOICE', label: '单选题', icon: Select },
  { type: 'MULTIPLE_CHOICE', label: '多选题', icon: List },
  { type: 'DROPDOWN', label: '下拉选择', icon: ArrowDown },
  { type: 'DATE', label: '日期', icon: Calendar },
  { type: 'NUMBER', label: '数字', icon: Histogram },
  { type: 'GROUP', label: '分组', icon: Folder }
]

// 问卷数据
const questionnaire = ref<{
  title: string
  description: string
  fields: QuestionnaireField[]
}>({
  title: '',
  description: '',
  fields: []
})

// 选中的字段索引
const selectedIndex = ref<number | null>(null)

// 选中的字段
const selectedField = computed(() => {
  if (selectedIndex.value === null) return null
  return questionnaire.value.fields[selectedIndex.value]
})

// 生成唯一 key
function generateFieldKey(): string {
  return `field_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

// 克隆字段（从组件库拖入时）
function cloneField(componentType: ComponentType): QuestionnaireField {
  const baseField: QuestionnaireField = {
    fieldKey: generateFieldKey(),
    label: componentType.label,
    fieldType: componentType.type,
    required: false,
    sortOrder: questionnaire.value.fields.length
  }

  // 为选择类型添加默认选项
  if (['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'DROPDOWN'].includes(componentType.type)) {
    baseField.options = [
      { label: '选项1', value: '选项1' },
      { label: '选项2', value: '选项2' }
    ]
  }

  return baseField
}

// 获取字段类型标签
function getFieldTypeLabel(type: FieldType): string {
  const found = componentTypes.find(c => c.type === type)
  return found ? found.label : type
}

// 选中字段
function selectField(index: number) {
  selectedIndex.value = index
}

// 删除字段
function removeField(index: number) {
  questionnaire.value.fields.splice(index, 1)
  if (selectedIndex.value === index) {
    selectedIndex.value = null
  } else if (selectedIndex.value !== null && selectedIndex.value > index) {
    selectedIndex.value--
  }
}

// 复制字段
function copyField(index: number) {
  const original = questionnaire.value.fields[index]
  const copied: QuestionnaireField = {
    ...original,
    fieldKey: generateFieldKey(),
    label: `${original.label}（副本）`
  }
  questionnaire.value.fields.splice(index + 1, 0, copied)
}

// 更新字段属性
function updateField(updated: QuestionnaireField) {
  if (selectedIndex.value !== null) {
    questionnaire.value.fields[selectedIndex.value] = updated
  }
}

// 字段变化回调
function onFieldChange() {
  // 更新排序
  questionnaire.value.fields.forEach((field, index) => {
    field.sortOrder = index
  })
}

// 暴露方法供父组件调用
defineExpose({
  getQuestionnaire: () => questionnaire.value,
  setQuestionnaire: (data: typeof questionnaire.value) => {
    questionnaire.value = data
    selectedIndex.value = null
  }
})
</script>

<style scoped>
.designer-container {
  display: flex;
  height: calc(100vh - 120px);
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

/* 左侧组件库 */
.component-library {
  width: 200px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: grab;
  transition: all 0.2s;
}

.component-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

.component-item:active {
  cursor: grabbing;
}

/* 中间设计区 */
.design-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow-y: auto;
}

.questionnaire-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.title-input :deep(.el-input__inner) {
  font-size: 18px;
  font-weight: 600;
}

.fields-container {
  min-height: 300px;
}

.field-item {
  position: relative;
  padding: 16px;
  margin-bottom: 12px;
  border: 2px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
}

.field-item:hover {
  border-color: #c0c4cc;
}

.field-item.active {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.field-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.field-type-tag {
  font-size: 12px;
  color: #909399;
  background: #f4f4f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.required-tag {
  font-size: 12px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.field-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: none;
}

.field-item:hover .field-actions {
  display: flex;
  gap: 4px;
}

.ghost-field {
  opacity: 0.5;
  background: #ecf5ff;
  border: 2px dashed #409eff;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #c0c4cc;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
}

.empty-placeholder p {
  margin-top: 12px;
  font-size: 14px;
}

/* 右侧属性编辑器 */
.property-editor {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.no-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
}

.no-selection p {
  margin-top: 12px;
  font-size: 14px;
}
</style>