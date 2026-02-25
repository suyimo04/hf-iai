<template>
  <el-dialog
    v-model="visible"
    title="审核报名"
    width="500px"
    destroy-on-close
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="申请人">
        <span>{{ application?.formData?.name || '-' }}</span>
      </el-form-item>
      <el-form-item label="当前状态">
        <el-tag :type="getStatusType(application?.status)" size="small">
          {{ getStatusLabel(application?.status) }}
        </el-tag>
      </el-form-item>
      <el-form-item label="审核结果" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio value="PASSED">通过</el-radio>
          <el-radio value="REJECTED">拒绝</el-radio>
          <el-radio value="INTERVIEW">进入面试</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="审核意见" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入审核意见"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确认
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { reviewApplication, APPLICATION_STATUS_MAP, type Application, type ApplicationStatus } from '@/api/application'

const props = defineProps<{
  modelValue: boolean
  application: Application | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: () => {}
})

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = ref<{
  status: ApplicationStatus | ''
  comment: string
}>({
  status: '',
  comment: ''
})

const rules: FormRules = {
  status: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

watch(() => props.modelValue, (val) => {
  if (val) {
    form.value = { status: '', comment: '' }
  }
})

function getStatusLabel(status?: ApplicationStatus): string {
  if (!status) return '-'
  return APPLICATION_STATUS_MAP[status]?.label || status
}

function getStatusType(status?: ApplicationStatus) {
  if (!status) return 'info'
  return APPLICATION_STATUS_MAP[status]?.type || 'info'
}

function handleClose() {
  emit('update:modelValue', false)
}

async function handleSubmit() {
  if (!formRef.value || !props.application) return

  await formRef.value.validate()

  loading.value = true
  try {
    await reviewApplication(props.application.id, {
      status: form.value.status as ApplicationStatus,
      comment: form.value.comment || undefined
    })
    ElMessage.success('审核成功')
    emit('success')
    handleClose()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>
