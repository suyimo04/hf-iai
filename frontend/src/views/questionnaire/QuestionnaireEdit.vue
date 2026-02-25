<template>
  <div class="questionnaire-edit" v-loading="loading">
    <div class="edit-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
        <span class="page-title">{{ isCreate ? '新建问卷' : '编辑问卷' }}</span>
        <el-tag v-if="!isCreate && questionnaire" type="info" size="small">
          v{{ questionnaire.version }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button @click="handlePreview">预览</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
        <el-button
          v-if="!isCreate && questionnaire?.status === 'DRAFT'"
          type="success"
          @click="handlePublish"
          :loading="publishing"
        >
          发布
        </el-button>
      </div>
    </div>

    <QuestionnaireDesigner ref="designerRef" />

    <el-dialog v-model="previewVisible" title="问卷预览" width="800px" destroy-on-close>
      <div class="dialog-preview">
        <div class="preview-title">
          <h2>{{ previewData?.title || '未命名问卷' }}</h2>
          <p v-if="previewData?.description">{{ previewData.description }}</p>
        </div>
        <el-divider />
        <el-form label-position="top">
          <template v-for="field in previewData?.fields" :key="field.fieldKey">
            <FieldRenderer
              :field="field"
              :model-value="null"
              :all-fields="previewData?.fields"
              disabled
            />
          </template>
        </el-form>
        <el-empty v-if="!previewData?.fields?.length" description="暂无字段" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuestionnaireDesigner from './QuestionnaireDesigner.vue'
import { FieldRenderer } from '@/components/questionnaire'
import {
  getQuestionnaire,
  createQuestionnaire,
  updateQuestionnaire,
  publishQuestionnaire,
  type Questionnaire,
  type QuestionnaireField
} from '@/api/questionnaire'

defineOptions({ name: 'QuestionnaireEdit' })

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const previewVisible = ref(false)
const questionnaire = ref<Questionnaire | null>(null)
const designerRef = ref<InstanceType<typeof QuestionnaireDesigner> | null>(null)

const previewData = ref<{
  title: string
  description: string
  fields: QuestionnaireField[]
} | null>(null)

const isCreate = computed(() => !route.params.id)

async function fetchQuestionnaire() {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getQuestionnaire(id)
    questionnaire.value = res.data.data || null
    if (questionnaire.value && designerRef.value) {
      designerRef.value.setQuestionnaire({
        title: questionnaire.value.title,
        description: questionnaire.value.description || '',
        fields: questionnaire.value.fields || []
      })
    }
  } catch (e) {
    ElMessage.error('获取问卷详情失败')
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push('/questionnaire')
}

function handlePreview() {
  if (!designerRef.value) return
  previewData.value = designerRef.value.getQuestionnaire()
  previewVisible.value = true
}

async function handleSave() {
  if (!designerRef.value) return

  const data = designerRef.value.getQuestionnaire()
  if (!data.title.trim()) {
    ElMessage.warning('请输入问卷标题')
    return
  }

  saving.value = true
  try {
    const formData = {
      title: data.title,
      description: data.description,
      status: questionnaire.value?.status || 'DRAFT' as const,
      accessType: questionnaire.value?.accessType || 'INTERNAL' as const,
      fields: data.fields
    }

    if (isCreate.value) {
      const res = await createQuestionnaire(formData)
      ElMessage.success('创建成功')
      router.replace(`/questionnaire/edit/${res.data.data?.id}`)
    } else {
      await updateQuestionnaire(Number(route.params.id), formData)
      ElMessage.success('保存成功')
      fetchQuestionnaire()
    }
  } catch (e) {
    ElMessage.error(isCreate.value ? '创建失败' : '保存失败')
    console.error(e)
  } finally {
    saving.value = false
  }
}

async function handlePublish() {
  if (!questionnaire.value?.id) return

  try {
    await ElMessageBox.confirm('发布后问卷将对外可见，确定要发布吗？', '确认发布', {
      type: 'warning'
    })
  } catch {
    return
  }

  publishing.value = true
  try {
    await publishQuestionnaire(questionnaire.value.id)
    ElMessage.success('发布成功')
    fetchQuestionnaire()
  } catch (e) {
    ElMessage.error('发布失败')
    console.error(e)
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  if (!isCreate.value) {
    fetchQuestionnaire()
  }
})
</script>

<style scoped>
.questionnaire-edit {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 8px;
}

.dialog-preview {
  max-height: 60vh;
  overflow-y: auto;
}

.preview-title h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}

.preview-title p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}
</style>
