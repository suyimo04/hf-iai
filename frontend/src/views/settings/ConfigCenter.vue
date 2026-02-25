<template>
  <div class="config-center">
    <div class="page-header">
      <h2>配置中心</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- AI配置 -->
      <el-tab-pane label="AI配置" name="ai">
        <AIConfigForm
          :config="aiConfig"
          :loading="loading.ai"
          @save="handleSaveAI"
          @test="handleTestAI"
        />
      </el-tab-pane>

      <!-- OSS配置 -->
      <el-tab-pane label="OSS配置" name="oss">
        <OSSConfigForm
          :config="ossConfig"
          :loading="loading.oss"
          @save="handleSaveOSS"
          @test="handleTestOSS"
        />
      </el-tab-pane>

      <!-- 邮件配置 -->
      <el-tab-pane label="邮件配置" name="email">
        <EmailConfigForm
          :config="emailConfig"
          :loading="loading.email"
          @save="handleSaveEmail"
          @test="handleTestEmail"
        />
      </el-tab-pane>

      <!-- 系统配置 -->
      <el-tab-pane label="系统配置" name="system">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="系统名称">
            <el-input v-model="systemConfig.siteName" placeholder="请输入系统名称" />
          </el-form-item>
          <el-form-item label="系统Logo">
            <el-input v-model="systemConfig.siteLogo" placeholder="请输入Logo URL" />
          </el-form-item>
          <el-form-item label="注册开关">
            <el-switch v-model="systemConfig.registerEnabled" />
          </el-form-item>
          <el-form-item label="面试开关">
            <el-switch v-model="systemConfig.interviewEnabled" />
          </el-form-item>
          <el-form-item label="面试最大轮次">
            <el-input-number v-model="systemConfig.maxInterviewRounds" :min="5" :max="30" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSaveSystem" :loading="loading.system">
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AIConfigForm from '@/components/config/AIConfigForm.vue'
import OSSConfigForm from '@/components/config/OSSConfigForm.vue'
import EmailConfigForm from '@/components/config/EmailConfigForm.vue'
import {
  getConfigByGroup,
  updateConfig,
  testAIConnection,
  testOSSConnection,
  testEmailConnection
} from '@/api/config'

defineOptions({ name: 'ConfigCenter' })

const activeTab = ref('ai')

const loading = reactive({
  ai: false,
  oss: false,
  email: false,
  system: false
})

const aiConfig = ref({
  provider: 'openai',
  apiKey: '',
  baseUrl: '',
  model: 'gpt-4'
})

const ossConfig = ref({
  provider: 'aliyun',
  endpoint: '',
  accessKey: '',
  secretKey: '',
  bucket: ''
})

const emailConfig = ref({
  host: '',
  port: 465,
  username: '',
  password: '',
  from: '',
  testTo: ''
})

const systemConfig = ref({
  siteName: '华芬管理系统',
  siteLogo: '',
  registerEnabled: true,
  interviewEnabled: true,
  maxInterviewRounds: 15
})

async function fetchConfigs() {
  try {
    // 获取AI配置
    const aiRes = await getConfigByGroup('ai')
    if (aiRes.data.data) {
      Object.assign(aiConfig.value, aiRes.data.data)
    }

    // 获取OSS配置
    const ossRes = await getConfigByGroup('oss')
    if (ossRes.data.data) {
      Object.assign(ossConfig.value, ossRes.data.data)
    }

    // 获取邮件配置
    const emailRes = await getConfigByGroup('email')
    if (emailRes.data.data) {
      Object.assign(emailConfig.value, emailRes.data.data)
    }

    // 获取系统配置
    const systemRes = await getConfigByGroup('system')
    if (systemRes.data.data) {
      Object.assign(systemConfig.value, systemRes.data.data)
    }
  } catch (e) {
    console.error('获取配置失败', e)
  }
}

async function handleSaveAI(config: typeof aiConfig.value) {
  loading.ai = true
  try {
    await updateConfig({ group: 'ai', configs: config })
    ElMessage.success('AI配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.ai = false
  }
}

async function handleTestAI(config: typeof aiConfig.value) {
  loading.ai = true
  try {
    const res = await testAIConnection(config)
    if (res.data.data?.success) {
      ElMessage.success('AI连接测试成功')
    } else {
      ElMessage.error(res.data.data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    loading.ai = false
  }
}

async function handleSaveOSS(config: typeof ossConfig.value) {
  loading.oss = true
  try {
    await updateConfig({ group: 'oss', configs: config })
    ElMessage.success('OSS配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.oss = false
  }
}

async function handleTestOSS(config: typeof ossConfig.value) {
  loading.oss = true
  try {
    const res = await testOSSConnection(config)
    if (res.data.data?.success) {
      ElMessage.success('OSS连接测试成功')
    } else {
      ElMessage.error(res.data.data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    loading.oss = false
  }
}

async function handleSaveEmail(config: typeof emailConfig.value) {
  loading.email = true
  try {
    await updateConfig({ group: 'email', configs: config })
    ElMessage.success('邮件配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.email = false
  }
}

async function handleTestEmail(config: typeof emailConfig.value) {
  loading.email = true
  try {
    const res = await testEmailConnection(config)
    if (res.data.data?.success) {
      ElMessage.success('邮件发送测试成功')
    } else {
      ElMessage.error(res.data.data?.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    loading.email = false
  }
}

async function handleSaveSystem() {
  loading.system = true
  try {
    await updateConfig({ group: 'system', configs: systemConfig.value })
    ElMessage.success('系统配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.system = false
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.config-center {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.config-form {
  max-width: 600px;
  padding: 20px;
}
</style>
