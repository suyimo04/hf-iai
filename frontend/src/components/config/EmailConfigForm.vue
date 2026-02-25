<template>
  <div class="email-config-form">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="SMTP服务器" prop="host">
        <el-input v-model="formData.host" placeholder="如: smtp.qq.com" />
      </el-form-item>

      <el-form-item label="端口" prop="port">
        <el-input-number v-model="formData.port" :min="1" :max="65535" />
        <span class="port-hint">常用端口: 25, 465(SSL), 587(TLS)</span>
      </el-form-item>

      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" placeholder="邮箱账号" />
      </el-form-item>

      <el-form-item label="密码/授权码" prop="password">
        <el-input
          v-model="formData.password"
          type="password"
          show-password
          placeholder="邮箱密码或授权码"
        />
      </el-form-item>

      <el-form-item label="发件人地址" prop="from">
        <el-input v-model="formData.from" placeholder="如: noreply@example.com" />
      </el-form-item>

      <el-form-item label="发件人名称">
        <el-input v-model="formData.fromName" placeholder="如: 华芬管理系统" />
      </el-form-item>

      <el-form-item label="SSL加密">
        <el-switch v-model="formData.ssl" />
      </el-form-item>

      <el-divider content-position="left">测试发送</el-divider>

      <el-form-item label="测试收件人">
        <el-input v-model="formData.testTo" placeholder="用于测试的收件邮箱" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSave" :loading="loading">
          保存配置
        </el-button>
        <ConfigTestButton type="email" :config="formData" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import ConfigTestButton from './ConfigTestButton.vue'

interface EmailConfig {
  host: string
  port: number
  username: string
  password: string
  from: string
  fromName?: string
  ssl?: boolean
  testTo?: string
}

const props = defineProps<{
  config: EmailConfig
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'save', config: EmailConfig): void
  (e: 'test', config: EmailConfig): void
}>()

const formRef = ref<FormInstance>()
const formData = ref<EmailConfig>({ ...props.config })

watch(() => props.config, (val) => {
  formData.value = { ...val }
}, { deep: true })

const rules: FormRules = {
  host: [{ required: true, message: '请输入SMTP服务器', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  from: [
    { required: true, message: '请输入发件人地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

async function handleSave() {
  const valid = await formRef.value?.validate()
  if (valid) {
    emit('save', { ...formData.value })
  }
}
</script>

<style scoped>
.email-config-form {
  max-width: 600px;
  padding: 20px;
}

.port-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}
</style>
