<template>
  <div class="settings-page">
    <BaseCard title="系统配置">
      <ConfigForm :configs="configs" :loading="loading" @saved="fetchConfigs" />
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import ConfigForm from './components/ConfigForm.vue'
import { getConfigs, type SystemConfig } from '@/api/config'

defineOptions({
  name: 'SettingsPage'
})

const configs = ref<SystemConfig[]>([])
const loading = ref(false)

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await getConfigs()
    configs.value = res.data.data || []
  } catch (error) {
    console.error('获取配置失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.settings-page {
  padding: 20px;
}
</style>
