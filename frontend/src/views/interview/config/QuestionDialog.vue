<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑题目' : '新增题目'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类" filterable allow-create style="width: 100%">
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
      </el-form-item>

      <el-form-item label="题型" prop="questionType">
        <el-radio-group v-model="form.questionType" @change="handleTypeChange">
          <el-radio value="SINGLE_CHOICE">单选题</el-radio>
          <el-radio value="MULTIPLE_CHOICE">多选题</el-radio>
          <el-radio value="TEXT">问答题</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="题目内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="3"
          placeholder="请输入题目内容"
        />
      </el-form-item>

      <!-- 选择题选项 -->
      <el-form-item
        v-if="form.questionType !== 'TEXT'"
        label="选项"
        prop="options"
      >
        <div class="options-editor">
          <div v-for="(opt, index) in form.options" :key="index" class="option-row">
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <el-input v-model="form.options[index]" placeholder="请输入选项内容" />
            <el-button
              type="danger"
              :icon="Delete"
              circle
              size="small"
              :disabled="form.options.length <= 2"
              @click="removeOption(index)"
            />
          </div>
          <el-button
            type="primary"
            link
            :icon="Plus"
            :disabled="form.options.length >= 6"
            @click="addOption"
          >
            添加选项
          </el-button>
        </div>
      </el-form-item>

      <!-- 选择题答案 -->
      <el-form-item
        v-if="form.questionType === 'SINGLE_CHOICE'"
        label="正确答案"
        prop="answer"
      >
        <el-radio-group v-model="form.answer">
          <el-radio
            v-for="(opt, index) in form.options"
            :key="index"
            :value="String.fromCharCode(65 + index)"
          >
            {{ String.fromCharCode(65 + index) }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-if="form.questionType === 'MULTIPLE_CHOICE'"
        label="正确答案"
        prop="answer"
      >
        <el-checkbox-group v-model="multipleAnswers">
          <el-checkbox
            v-for="(opt, index) in form.options"
            :key="index"
            :value="String.fromCharCode(65 + index)"
            :label="String.fromCharCode(65 + index)"
          />
        </el-checkbox-group>
      </el-form-item>

      <!-- 问答题关键词 -->
      <el-form-item v-if="form.questionType === 'TEXT'" label="评分关键词" prop="keywords">
        <el-select
          v-model="form.keywords"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="输入关键词后回车添加"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="分值" prop="score">
        <el-input-number v-model="form.score" :min="1" :max="100" />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="9999" />
      </el-form-item>

      <el-form-item label="启用状态" prop="enabled">
        <el-switch v-model="form.enabled" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  createQuestion,
  updateQuestion,
  getQuestionCategories,
  type Question,
  type QuestionType
} from '@/api/interview'

const props = defineProps<{
  modelValue: boolean
  question: Question | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const categories = ref<string[]>([])
const multipleAnswers = ref<string[]>([])

const isEdit = computed(() => !!props.question)

const form = reactive({
  category: '',
  content: '',
  questionType: 'SINGLE_CHOICE' as QuestionType,
  options: ['', ''] as string[],
  answer: '',
  keywords: [] as string[],
  score: 10,
  sort: 0,
  enabled: true
})

const rules: FormRules = {
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    fetchCategories()
    if (props.question) {
      Object.assign(form, {
        category: props.question.category,
        content: props.question.content,
        questionType: props.question.questionType,
        options: props.question.options?.length ? [...props.question.options] : ['', ''],
        answer: props.question.answer || '',
        keywords: props.question.keywords || [],
        score: props.question.score,
        sort: props.question.sort,
        enabled: props.question.enabled
      })
      if (props.question.questionType === 'MULTIPLE_CHOICE' && props.question.answer) {
        multipleAnswers.value = props.question.answer.split('')
      }
    }
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(multipleAnswers, (val) => {
  if (form.questionType === 'MULTIPLE_CHOICE') {
    form.answer = val.sort().join('')
  }
})

async function fetchCategories() {
  try {
    const res = await getQuestionCategories()
    categories.value = res.data.data
  } catch {
    categories.value = ['技术基础', '项目经验', '综合素质', '专业知识']
  }
}

function handleTypeChange() {
  form.answer = ''
  form.keywords = []
  multipleAnswers.value = []
  if (form.questionType !== 'TEXT' && form.options.length < 2) {
    form.options = ['', '']
  }
}

function addOption() {
  if (form.options.length < 6) {
    form.options.push('')
  }
}

function removeOption(index: number) {
  if (form.options.length > 2) {
    form.options.splice(index, 1)
    // 重新调整答案
    if (form.questionType === 'SINGLE_CHOICE') {
      const answerIndex = form.answer.charCodeAt(0) - 65
      if (answerIndex === index) {
        form.answer = ''
      } else if (answerIndex > index) {
        form.answer = String.fromCharCode(answerIndex + 64)
      }
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  // 验证选项
  if (form.questionType !== 'TEXT') {
    const validOptions = form.options.filter(o => o.trim())
    if (validOptions.length < 2) {
      ElMessage.warning('请至少填写2个选项')
      return
    }
    if (!form.answer) {
      ElMessage.warning('请选择正确答案')
      return
    }
  }

  submitting.value = true
  try {
    const data = {
      ...form,
      options: form.questionType !== 'TEXT' ? form.options.filter(o => o.trim()) : undefined,
      keywords: form.questionType === 'TEXT' ? form.keywords : undefined,
      answer: form.questionType !== 'TEXT' ? form.answer : undefined
    }

    if (isEdit.value && props.question) {
      await updateQuestion(props.question.id, data)
      ElMessage.success('更新成功')
    } else {
      await createQuestion(data)
      ElMessage.success('创建成功')
    }
    emit('success')
    handleClose()
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
  Object.assign(form, {
    category: '',
    content: '',
    questionType: 'SINGLE_CHOICE',
    options: ['', ''],
    answer: '',
    keywords: [],
    score: 10,
    sort: 0,
    enabled: true
  })
  multipleAnswers.value = []
}
</script>

<style scoped>
.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.option-label {
  width: 24px;
  font-weight: 500;
}

.option-row .el-input {
  flex: 1;
}
</style>
