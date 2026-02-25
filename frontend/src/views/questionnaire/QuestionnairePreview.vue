<template>
  <div class="questionnaire-preview" v-loading="loading">
    <div class="preview-header">
      <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
      <span class="preview-badge">预览模式</span>
    </div>

    <div class="preview-container" v-if="questionnaire">
      <div class="questionnaire-card">
        <div class="questionnaire-title">
          <h1>{{ questionnaire.title }}</h1>
          <el-tag :type="getStatusTagType(questionnaire.status)" size="small">
            {{ getStatusLabel(questionnaire.status) }}
          </el-tag>
        </div>

        <p v-if="questionnaire.description" class="questionnaire-desc">
          {{ questionnaire.description }}
        </p>

        <el-divider />

        <el-form label-position="top" class="preview-form">
          <template v-for="field in questionnaire.fields" :key="field.fieldKey">
            <FieldRenderer
              :field="field"
              :model-value="formData[field.fieldKey]"
              :all-fields="questionnaire.fields"
              :all-values="formData"
              disabled
              @update:model-value="val => formData[field.fieldKey] = val"
            />
          </template>
        </el-form>

        <div v-if="!questionnaire.fields?.length" class="empty-fields">
          <el-empty description="该问卷暂无字段" />
        </div>
      </div>

      <div class="preview-footer">
        <span class="version-info">版本: v{{ questionnaire.version }}</span>
        <span class="time-info">创建时间: {{ formatDate(questionnaire.createdAt) }}</span>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="问卷不存在或已被删除" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getQuestionnaire, type Questionnaire, type QuestionnaireStatus } from '@/api/questionnaire'
import { FieldRenderer } from '@/components/questionnaire'

defineOptions({ name: 'QuestionnairePreview' })

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const questionnaire = ref<Questionnaire | null>(null)
const formData = reactive<Record<string, any>>({})

const statusMap: Record<QuestionnaireStatus, string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  ARCHIVED: '已归档'
}

function getStatusLabel(status: QuestionnaireStatus): string {
  return statusMap[status] || status
}

function getStatusTagType(status: QuestionnaireStatus): '' | 'success' | 'warning' | 'info' {
  const typeMap: Record<QuestionnaireStatus, '' | 'success' | 'warning' | 'info'> = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    ARCHIVED: 'warning'
  }
  return typeMap[status] || 'info'
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function fetchQuestionnaire() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getQuestionnaire(id)
    questionnaire.value = res.data.data || null
  } catch (e) {
    console.error('获取问卷详情失败', e)
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}

onMounted(() => {
  fetchQuestionnaire()
})
</script>

<style scoped>
.questionnaire-preview {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.preview-badge {
  padding: 4px 12px;
  background: #e6a23c;
  color: #fff;
  border-radius: 4px;
  font-size: 12px;
}

.preview-container {
  max-width: 800px;
  margin: 0 auto;
}

.questionnaire-card {
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.questionnaire-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.questionnaire-title h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.questionnaire-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.preview-form {
  margin-top: 20px;
}

.preview-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.empty-fields {
  padding: 40px 0;
}

.preview-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding: 0 8px;
  color: #909399;
  font-size: 12px;
}
</style>
