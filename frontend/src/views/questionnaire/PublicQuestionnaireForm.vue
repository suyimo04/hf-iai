<template>
  <div class="public-questionnaire" v-loading="loading">
    <div class="questionnaire-container" v-if="questionnaire">
      <div class="questionnaire-card">
        <div class="questionnaire-header">
          <h1>{{ questionnaire.title }}</h1>
          <p v-if="questionnaire.description" class="description">
            {{ questionnaire.description }}
          </p>
        </div>

        <el-divider />

        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-position="top"
          class="questionnaire-form"
        >
          <template v-for="field in questionnaire.fields" :key="field.fieldKey">
            <FieldRenderer
              :field="field"
              :model-value="formData[field.fieldKey]"
              :all-fields="questionnaire.fields"
              :all-values="formData"
              @update:model-value="val => formData[field.fieldKey] = val"
            />
          </template>
        </el-form>

        <div v-if="!questionnaire.fields?.length" class="empty-fields">
          <el-empty description="该问卷暂无字段" />
        </div>

        <div class="submit-area" v-if="questionnaire.fields?.length">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交问卷
          </el-button>
        </div>
      </div>

      <div class="footer-info">
        <span>由华芬管理系统提供技术支持</span>
      </div>
    </div>

    <!-- 提交成功 -->
    <div class="success-container" v-else-if="submitted">
      <el-result icon="success" title="提交成功" sub-title="感谢您的参与！">
        <template #extra>
          <el-button type="primary" @click="handleReset">再次填写</el-button>
        </template>
      </el-result>
    </div>

    <!-- 问卷不存在 -->
    <div class="error-container" v-else-if="!loading && error">
      <el-result icon="warning" :title="error" sub-title="请检查链接是否正确">
        <template #extra>
          <el-button type="primary" @click="handleRetry">重试</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getPublicQuestionnaire,
  submitQuestionnaireResponse,
  type Questionnaire,
  type QuestionnaireField
} from '@/api/questionnaire'
import { FieldRenderer } from '@/components/questionnaire'

defineOptions({ name: 'PublicQuestionnaireForm' })

const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const submitted = ref(false)
const error = ref('')
const questionnaire = ref<Questionnaire | null>(null)
const formData = reactive<Record<string, any>>({})

const formRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  if (!questionnaire.value?.fields) return rules

  questionnaire.value.fields.forEach((field: QuestionnaireField) => {
    if (field.required) {
      rules[field.fieldKey] = [
        { required: true, message: `请填写${field.label}`, trigger: 'blur' }
      ]
    }
    // 添加自定义校验规则
    if (field.validationRules) {
      const fieldRules: any[] = rules[field.fieldKey] || []
      if (field.validationRules.minLength) {
        fieldRules.push({
          min: field.validationRules.minLength,
          message: `最少${field.validationRules.minLength}个字符`,
          trigger: 'blur'
        })
      }
      if (field.validationRules.maxLength) {
        fieldRules.push({
          max: field.validationRules.maxLength,
          message: `最多${field.validationRules.maxLength}个字符`,
          trigger: 'blur'
        })
      }
      if (field.validationRules.pattern) {
        fieldRules.push({
          pattern: new RegExp(field.validationRules.pattern),
          message: field.validationRules.patternMessage || '格式不正确',
          trigger: 'blur'
        })
      }
      rules[field.fieldKey] = fieldRules
    }
  })

  return rules
})

async function fetchQuestionnaire() {
  const token = route.params.token as string
  if (!token) {
    error.value = '无效的问卷链接'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res = await getPublicQuestionnaire(token)
    questionnaire.value = res.data.data

    if (!questionnaire.value) {
      error.value = '问卷不存在或已关闭'
      return
    }

    if (questionnaire.value.status !== 'PUBLISHED') {
      error.value = '该问卷暂未开放'
      questionnaire.value = null
      return
    }

    // 初始化表单数据
    questionnaire.value.fields?.forEach((field: QuestionnaireField) => {
      formData[field.fieldKey] = getDefaultValue(field)
    })
  } catch (e: any) {
    console.error('获取问卷失败', e)
    error.value = e.response?.data?.message || '获取问卷失败'
  } finally {
    loading.value = false
  }
}

function getDefaultValue(field: QuestionnaireField): any {
  switch (field.fieldType) {
    case 'MULTIPLE_CHOICE':
      return []
    case 'NUMBER':
      return null
    case 'DATE':
      return ''
    default:
      return ''
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    ElMessage.warning('请完善必填项')
    return
  }

  submitting.value = true
  try {
    await submitQuestionnaireResponse(questionnaire.value!.id!, {
      answers: { ...formData }
    })
    submitted.value = true
    questionnaire.value = null
    ElMessage.success('提交成功')
  } catch (e: any) {
    console.error('提交失败', e)
    ElMessage.error(e.response?.data?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function handleReset() {
  submitted.value = false
  fetchQuestionnaire()
}

function handleRetry() {
  fetchQuestionnaire()
}

onMounted(() => {
  fetchQuestionnaire()
})
</script>

<style scoped>
.public-questionnaire {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.questionnaire-container {
  max-width: 700px;
  margin: 0 auto;
}

.questionnaire-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.questionnaire-header {
  text-align: center;
  margin-bottom: 20px;
}

.questionnaire-header h1 {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.questionnaire-header .description {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.questionnaire-form {
  margin-top: 20px;
}

.questionnaire-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.questionnaire-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.empty-fields {
  padding: 40px 0;
}

.submit-area {
  margin-top: 32px;
  text-align: center;
}

.submit-area .el-button {
  min-width: 200px;
  height: 48px;
  font-size: 16px;
}

.footer-info {
  text-align: center;
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
}

.success-container,
.error-container {
  max-width: 500px;
  margin: 100px auto;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}
</style>
