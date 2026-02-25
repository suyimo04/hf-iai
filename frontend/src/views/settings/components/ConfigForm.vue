<template>
  <el-form :model="form" label-width="150px" :disabled="loading">
    <el-form-item label="薪酬池总额">
      <el-input-number v-model="form.SALARY_POOL" :min="0" :max="10000" />
      <span class="config-unit">元/月</span>
    </el-form-item>

    <el-form-item label="AI面试功能">
      <el-switch v-model="form.AI_INTERVIEW_ENABLED" />
    </el-form-item>

    <el-form-item label="签到积分">
      <el-input-number v-model="form.CHECKIN_POINTS" :min="1" :max="100" />
      <span class="config-unit">分/次</span>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" :loading="saving" @click="handleSave">
        保存配置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { SystemConfig } from '@/api/config'
import { updateConfig } from '@/api/config'

defineOptions({
  name: 'ConfigForm'
})

const props = defineProps<{
  configs: SystemConfig[]
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'saved'): void
}>()

interface FormData {
  SALARY_POOL: number
  AI_INTERVIEW_ENABLED: boolean
  CHECKIN_POINTS: number
}

const form = ref<FormData>({
  SALARY_POOL: 0,
  AI_INTERVIEW_ENABLED: false,
  CHECKIN_POINTS: 1
})

const saving = ref(false)

// 监听配置变化，更新表单
watch(
  () => props.configs,
  (configs) => {
    if (configs.length > 0) {
      configs.forEach((config) => {
        if (config.key === 'SALARY_POOL') {
          form.value.SALARY_POOL = Number(config.value) || 0
        } else if (config.key === 'AI_INTERVIEW_ENABLED') {
          form.value.AI_INTERVIEW_ENABLED = config.value === 'true'
        } else if (config.key === 'CHECKIN_POINTS') {
          form.value.CHECKIN_POINTS = Number(config.value) || 1
        }
      })
    }
  },
  { immediate: true }
)

async function handleSave() {
  saving.value = true
  try {
    await Promise.all([
      updateConfig('SALARY_POOL', String(form.value.SALARY_POOL)),
      updateConfig('AI_INTERVIEW_ENABLED', String(form.value.AI_INTERVIEW_ENABLED)),
      updateConfig('CHECKIN_POINTS', String(form.value.CHECKIN_POINTS))
    ])
    ElMessage.success('配置保存成功')
    emit('saved')
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.config-unit {
  margin-left: 8px;
  color: #9ca3af;
}
</style>
