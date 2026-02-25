<template>
  <div class="config-test-button">
    <el-button
      :type="buttonType"
      :loading="testing"
      @click="runTest"
    >
      <el-icon v-if="!testing"><Connection /></el-icon>
      {{ testing ? '测试中...' : '测试连接' }}
    </el-button>

    <div v-if="result" class="test-result" :class="result.success ? 'success' : 'error'">
      <el-icon>
        <CircleCheck v-if="result.success" />
        <CircleClose v-else />
      </el-icon>
      <span>{{ result.message }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Connection, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { testAIConnection, testOSSConnection, testEmailConnection } from '@/api/config'

type TestType = 'ai' | 'oss' | 'email'

defineOptions({
  name: 'ConfigTestButton'
})

const props = defineProps<{
  type: TestType
  config: Record<string, any>
}>()

const testing = ref(false)
const result = ref<{ success: boolean; message: string } | null>(null)

const buttonType = computed(() => {
  if (!result.value) return 'primary'
  return result.value.success ? 'success' : 'danger'
})

const runTest = async () => {
  testing.value = true
  result.value = null

  try {
    let response: string
    switch (props.type) {
      case 'ai':
        response = await testAIConnection(props.config)
        break
      case 'oss':
        response = await testOSSConnection(props.config)
        break
      case 'email':
        response = await testEmailConnection(props.config)
        break
    }
    result.value = { success: true, message: response || '连接成功' }
  } catch (error: any) {
    result.value = { success: false, message: error.message || '连接失败' }
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.config-test-button {
  display: flex;
  align-items: center;
  gap: 12px;
}
.test-result {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}
.test-result.success { color: #67c23a; }
.test-result.error { color: #f56c6c; }
</style>
