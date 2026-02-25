<template>
  <el-dialog
    v-model="visible"
    title="报名签到管理"
    width="700px"
    @close="handleClose"
  >
    <div class="signup-list__header">
      <span>活动：{{ activity?.title }}</span>
      <span>报名人数：{{ signups.length }} / {{ activity?.maxParticipants }}</span>
    </div>

    <el-table :data="signups" v-loading="loading" style="width: 100%">
      <el-table-column prop="nickname" label="昵称" width="120">
        <template #default="{ row }">
          {{ row.nickname || row.username }}
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="signupTime" label="报名时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.signupTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="signedIn" label="签到状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.signedIn ? 'success' : 'info'" size="small">
            {{ row.signedIn ? '已签到' : '未签到' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="signinTime" label="签到时间" width="180">
        <template #default="{ row }">
          {{ row.signinTime ? formatTime(row.signinTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.signedIn"
            type="primary"
            size="small"
            @click="handleSignin(row)"
            :loading="signinLoading === row.userId"
          >
            手动签到
          </el-button>
          <span v-else class="signup-list__signed">已完成</span>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getActivitySignups, signinActivity, type Activity, type ActivitySignup } from '@/api/activity'

const props = defineProps<{
  modelValue: boolean
  activity?: Activity | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = ref(props.modelValue)
const loading = ref(false)
const signinLoading = ref<number | null>(null)
const signups = ref<ActivitySignup[]>([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.activity) {
    fetchSignups()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const fetchSignups = async () => {
  if (!props.activity) return
  loading.value = true
  try {
    const res = await getActivitySignups(props.activity.id)
    signups.value = res.data.data || []
  } catch {
    signups.value = []
  } finally {
    loading.value = false
  }
}

const handleSignin = async (signup: ActivitySignup) => {
  if (!props.activity) return
  signinLoading.value = signup.userId
  try {
    await signinActivity(props.activity.id, signup.userId)
    ElMessage.success('签到成功')
    signup.signedIn = true
    signup.signinTime = new Date().toISOString()
  } catch {
    ElMessage.error('签到失败')
  } finally {
    signinLoading.value = null
  }
}

const handleClose = () => {
  visible.value = false
  signups.value = []
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.signup-list__header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
}

.signup-list__signed {
  color: #9ca3af;
  font-size: 14px;
}
</style>
