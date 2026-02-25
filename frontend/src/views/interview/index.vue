<template>
  <div class="interview-page">
    <div class="page-header">
      <h2>AI面试中心</h2>
    </div>

    <InterviewList
      ref="listRef"
      @view-detail="handleViewDetail"
      @view-report="handleViewReport"
    />

    <InterviewDetail
      v-model="detailVisible"
      :interview-id="currentInterviewId"
      :show-report="showReport"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import InterviewList from './components/InterviewList.vue'
import InterviewDetail from './components/InterviewDetail.vue'
import type { Interview } from '@/api/interview'

const listRef = ref<InstanceType<typeof InterviewList> | null>(null)
const detailVisible = ref(false)
const currentInterviewId = ref<number | null>(null)
const showReport = ref(false)

function handleViewDetail(interview: Interview) {
  currentInterviewId.value = interview.id
  showReport.value = false
  detailVisible.value = true
}

function handleViewReport(interview: Interview) {
  currentInterviewId.value = interview.id
  showReport.value = true
  detailVisible.value = true
}
</script>

<style scoped>
.interview-page {
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
</style>
