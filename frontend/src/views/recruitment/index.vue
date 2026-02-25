<template>
  <div class="recruitment-page">
    <BaseCard title="招募管理">
      <template #extra>
        <el-input
          v-model="keyword"
          placeholder="搜索姓名/手机/邮箱"
          clearable
          style="width: 200px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审核" name="PENDING" />
        <el-tab-pane label="审核中" name="REVIEWING" />
        <el-tab-pane label="面试中" name="INTERVIEW" />
        <el-tab-pane label="已通过" name="PASSED" />
        <el-tab-pane label="已拒绝" name="REJECTED" />
      </el-tabs>

      <ApplicationTable
        :data="applicationList"
        :loading="loading"
        @view="handleView"
        @review="handleReview"
        @interview="handleInterview"
      />

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </BaseCard>

    <ApplicationDetail
      v-model="detailVisible"
      :application="currentApplication"
    />

    <ReviewDialog
      v-model="reviewVisible"
      :application="currentApplication"
      @success="fetchList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import ApplicationTable from './components/ApplicationTable.vue'
import ApplicationDetail from './components/ApplicationDetail.vue'
import ReviewDialog from './components/ReviewDialog.vue'
import { getApplications, type Application, type ApplicationStatus } from '@/api/application'

const router = useRouter()

const loading = ref(false)
const applicationList = ref<Application[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeTab = ref('all')
const keyword = ref('')

const detailVisible = ref(false)
const reviewVisible = ref(false)
const currentApplication = ref<Application | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: activeTab.value === 'all' ? undefined : activeTab.value as ApplicationStatus,
      keyword: keyword.value || undefined
    }
    const res = await getApplications(params)
    applicationList.value = res.data.data.content || []
    total.value = res.data.data.totalElements || 0
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  fetchList()
}

function handleSearch() {
  currentPage.value = 1
  fetchList()
}

function handleView(row: Application) {
  currentApplication.value = row
  detailVisible.value = true
}

function handleReview(row: Application) {
  currentApplication.value = row
  reviewVisible.value = true
}

function handleInterview(row: Application) {
  ElMessage.info('即将跳转到面试页面')
  router.push({ path: '/interview', query: { applicationId: row.id } })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.recruitment-page {
  padding: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
