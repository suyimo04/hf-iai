<template>
  <div class="oss-config-form">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="OSS提供商" prop="provider">
        <el-select v-model="formData.provider" placeholder="请选择OSS提供商">
          <el-option label="阿里云OSS" value="aliyun" />
          <el-option label="腾讯云COS" value="tencent" />
          <el-option label="七牛云" value="qiniu" />
          <el-option label="MinIO" value="minio" />
        </el-select>
      </el-form-item>

      <el-form-item label="Endpoint" prop="endpoint">
        <el-input v-model="formData.endpoint" placeholder="如: oss-cn-hangzhou.aliyuncs.com" />
      </el-form-item>

      <el-form-item label="Access Key" prop="accessKey">
        <el-input v-model="formData.accessKey" placeholder="请输入Access Key" />
      </el-form-item>

      <el-form-item label="Secret Key" prop="secretKey">
        <el-input
          v-model="formData.secretKey"
          type="password"
          show-password
          placeholder="请输入Secret Key"
        />
      </el-form-item>

      <el-form-item label="Bucket" prop="bucket">
        <el-input v-model="formData.bucket" placeholder="请输入Bucket名称" />
      </el-form-item>

      <el-form-item label="自定义域名">
        <el-input v-model="formData.customDomain" placeholder="可选，如: cdn.example.com" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSave" :loading="loading">
          保存配置
        </el-button>
        <ConfigTestButton type="oss" :config="formData" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import ConfigTestButton from './ConfigTestButton.vue'

interface OSSConfig {
  provider: string
  endpoint: string
  accessKey: string
  secretKey: string
  bucket: string
  customDomain?: string
}

const props = defineProps<{
  config: OSSConfig
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'save', config: OSSConfig): void
  (e: 'test', config: OSSConfig): void
}>()

const formRef = ref<FormInstance>()
const formData = ref<OSSConfig>({ ...props.config })

watch(() => props.config, (val) => {
  formData.value = { ...val }
}, { deep: true })

const rules: FormRules = {
  provider: [{ required: true, message: '请选择OSS提供商', trigger: 'change' }],
  endpoint: [{ required: true, message: '请输入Endpoint', trigger: 'blur' }],
  accessKey: [{ required: true, message: '请输入Access Key', trigger: 'blur' }],
  secretKey: [{ required: true, message: '请输入Secret Key', trigger: 'blur' }],
  bucket: [{ required: true, message: '请输入Bucket名称', trigger: 'blur' }]
}

async function handleSave() {
  const valid = await formRef.value?.validate()
  if (valid) {
    emit('save', { ...formData.value })
  }
}
</script>

<style scoped>
.oss-config-form {
  max-width: 600px;
  padding: 20px;
}
</style>
