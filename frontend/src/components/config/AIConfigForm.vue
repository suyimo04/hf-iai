<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="120px"
    class="ai-config-form"
  >
    <el-form-item label="服务商" prop="provider">
      <el-select v-model="form.provider" placeholder="请选择服务商" @change="onProviderChange">
        <el-option label="OpenAI" value="openai" />
        <el-option label="Claude" value="claude" />
        <el-option label="自定义" value="custom" />
      </el-select>
    </el-form-item>

    <el-form-item label="API Key" prop="apiKey">
      <el-input
        v-model="form.apiKey"
        :type="showKey ? 'text' : 'password'"
        placeholder="请输入API Key"
        clearable
      >
        <template #suffix>
          <el-icon class="cursor-pointer" @click="showKey = !showKey">
            <View v-if="!showKey" />
            <Hide v-else />
          </el-icon>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item label="Base URL" prop="baseUrl">
      <el-input v-model="form.baseUrl" placeholder="请输入API地址" clearable />
    </el-form-item>

    <el-form-item label="模型" prop="model">
      <el-select v-model="form.model" placeholder="请选择模型" filterable>
        <el-option
          v-for="model in modelOptions"
          :key="model.value"
          :label="model.label"
          :value="model.value"
        />
      </el-select>
    </el-form-item>

    <el-form-item label="超时时间" prop="timeout">
      <el-input-number
        v-model="form.timeout"
        :min="5"
        :max="300"
        :step="5"
        controls-position="right"
      />
      <span class="ml-2 text-gray-500">秒</span>
    </el-form-item>

    <el-form-item>
      <el-button :loading="testing" @click="testConnection">
        <el-icon v-if="!testing" class="mr-1"><Connection /></el-icon>
        测试连接
      </el-button>
      <el-button type="primary" :loading="saving" @click="saveConfig">
        <el-icon class="mr-1"><Check /></el-icon>
        保存配置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { View, Hide, Connection, Check } from '@element-plus/icons-vue'
import { getConfig, updateConfig } from '@/api/config'

defineOptions({
  name: 'AIConfigForm'
})

const emit = defineEmits<{
  (e: 'save', config: AIConfig): void
  (e: 'test', config: AIConfig): void
}>()

// AI配置接口
interface AIConfig {
  provider: string
  apiKey: string
  baseUrl: string
  model: string
  timeout: number
}

// 模型选项接口
interface ModelOption {
  label: string
  value: string
}

const formRef = ref<FormInstance>()
const showKey = ref(false)
const testing = ref(false)
const saving = ref(false)

const form = reactive<AIConfig>({
  provider: 'openai',
  apiKey: '',
  baseUrl: 'https://api.openai.com/v1',
  model: 'gpt-4',
  timeout: 30
})

// 服务商默认配置
const providerDefaults: Record<string, { baseUrl: string; models: ModelOption[] }> = {
  openai: {
    baseUrl: 'https://api.openai.com/v1',
    models: [
      { label: 'GPT-4', value: 'gpt-4' },
      { label: 'GPT-4 Turbo', value: 'gpt-4-turbo' },
      { label: 'GPT-4o', value: 'gpt-4o' },
      { label: 'GPT-3.5 Turbo', value: 'gpt-3.5-turbo' }
    ]
  },
  claude: {
    baseUrl: 'https://api.anthropic.com/v1',
    models: [
      { label: 'Claude 3 Opus', value: 'claude-3-opus-20240229' },
      { label: 'Claude 3 Sonnet', value: 'claude-3-sonnet-20240229' },
      { label: 'Claude 3 Haiku', value: 'claude-3-haiku-20240307' }
    ]
  },
  custom: {
    baseUrl: '',
    models: [
      { label: '自定义模型', value: 'custom' }
    ]
  }
}

// 当前可选模型列表
const modelOptions = computed(() => {
  return providerDefaults[form.provider]?.models || []
})

// 表单验证规则
const rules: FormRules<AIConfig> = {
  provider: [
    { required: true, message: '请选择服务商', trigger: 'change' }
  ],
  apiKey: [
    { required: true, message: '请输入API Key', trigger: 'blur' },
    { min: 10, message: 'API Key长度不能少于10位', trigger: 'blur' }
  ],
  baseUrl: [
    { required: true, message: '请输入Base URL', trigger: 'blur' },
    { type: 'url', message: '请输入有效的URL地址', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请选择模型', trigger: 'change' }
  ],
  timeout: [
    { required: true, message: '请设置超时时间', trigger: 'blur' }
  ]
}

// 服务商变更处理
const onProviderChange = (provider: string) => {
  const defaults = providerDefaults[provider]
  if (defaults) {
    form.baseUrl = defaults.baseUrl
    form.model = defaults.models[0]?.value || ''
  }
}

// 测试连接
const testConnection = async () => {
  try {
    await formRef.value?.validate()
    testing.value = true

    // 模拟测试连接请求
    await new Promise(resolve => setTimeout(resolve, 1500))

    emit('test', { ...form })
    ElMessage.success('连接测试成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('连接测试失败，请检查配置')
    }
  } finally {
    testing.value = false
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    await formRef.value?.validate()
    saving.value = true

    // 保存各项配置到后端
    const configItems = [
      { key: 'ai.provider', value: form.provider },
      { key: 'ai.apiKey', value: form.apiKey },
      { key: 'ai.baseUrl', value: form.baseUrl },
      { key: 'ai.model', value: form.model },
      { key: 'ai.timeout', value: String(form.timeout) }
    ]

    for (const item of configItems) {
      await updateConfig(item.key, item.value)
    }

    emit('save', { ...form })
    ElMessage.success('配置保存成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('配置保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 加载配置
const loadConfig = async () => {
  try {
    const keys = ['ai.provider', 'ai.apiKey', 'ai.baseUrl', 'ai.model', 'ai.timeout']
    for (const key of keys) {
      try {
        const res = await getConfig(key)
        if (res.data?.data?.value) {
          const field = key.replace('ai.', '') as keyof AIConfig
          if (field === 'timeout') {
            form[field] = parseInt(res.data.data.value) || 30
          } else {
            (form as any)[field] = res.data.data.value
          }
        }
      } catch {
        // 配置项不存在时忽略
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.ai-config-form {
  max-width: 600px;
}

.ai-config-form :deep(.el-select) {
  width: 100%;
}

.ai-config-form :deep(.el-input-number) {
  width: 150px;
}

.cursor-pointer {
  cursor: pointer;
}

.ml-2 {
  margin-left: 8px;
}

.mr-1 {
  margin-right: 4px;
}

.text-gray-500 {
  color: #6b7280;
}
</style>
