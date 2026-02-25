<template>
  <div class="activities">
    <div class="activities__header">
      <h2 class="activities__title">活动管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>新建活动
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="草稿" name="DRAFT" />
      <el-tab-pane label="已发布" name="PUBLISHED" />
      <el-tab-pane label="进行中" name="ONGOING" />
      <el-tab-pane label="已结束" name="ENDED" />
    </el-tabs>

    <div v-loading="loading" class="activities__content">
      <template v-if="activities.length > 0">
        <ActivityCard
          v-for="activity in activities"
          :key="activity.id"
          :activity="activity"
          @edit="handleEdit"
          @delete="handleDelete"
          @signups="handleSignups"
        />
      </template>
      <el-empty v-else description="暂无活动" />
    </div>

    <div class="activities__pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchActivities"
        @current-change="fetchActivities"
      />
    </div>

    <ActivityForm
      v-model="formVisible"
      :activity="currentActivity"
      @submit="handleSubmit"
    />

    <SignupList
      v-model="signupVisible"
      :activity="currentActivity"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ActivityCard from './components/ActivityCard.vue'
import ActivityForm from './components/ActivityForm.vue'
import SignupList from './components/SignupList.vue'
import {
  getActivities,
  createActivity,
  updateActivity,
  deleteActivity,
  type Activity,
  type ActivityForm as ActivityFormType,
  type ActivityStatus
} from '@/api/activity'

const activeTab = ref('all')
const loading = ref(false)
const activities = ref<Activity[]>([])
const formVisible = ref(false)
const signupVisible = ref(false)
const currentActivity = ref<Activity | null>(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchActivities = async () => {
  loading.value = true
  try {
    const params: { page: number; size: number; status?: ActivityStatus } = {
      page: pagination.page,
      size: pagination.size
    }
    if (activeTab.value !== 'all') {
      params.status = activeTab.value as ActivityStatus
    }
    const res = await getActivities(params)
    const data = res.data.data
    activities.value = data.content || []
    pagination.total = data.totalElements || 0
  } catch {
    activities.value = []
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  pagination.page = 1
  fetchActivities()
}

const handleCreate = () => {
  currentActivity.value = null
  formVisible.value = true
}

const handleEdit = (activity: Activity) => {
  currentActivity.value = activity
  formVisible.value = true
}

const handleDelete = async (activity: Activity) => {
  try {
    await deleteActivity(activity.id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleSignups = (activity: Activity) => {
  currentActivity.value = activity
  signupVisible.value = true
}

const handleSubmit = async (form: ActivityFormType) => {
  try {
    if (currentActivity.value) {
      await updateActivity(currentActivity.value.id, form)
      ElMessage.success('更新成功')
    } else {
      await createActivity(form)
      ElMessage.success('创建成功')
    }
    fetchActivities()
  } catch {
    ElMessage.error(currentActivity.value ? '更新失败' : '创建失败')
  }
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.activities {
  padding: 24px;
}

.activities__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.activities__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.activities__content {
  min-height: 400px;
  margin-top: 16px;
}

.activities__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
