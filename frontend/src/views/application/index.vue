<template>
  <div class="application-page">
    <div class="application-card glass-effect">
      <!-- 已登录且已报名：显示状态 -->
      <template v-if="myApplication">
        <div class="card-header">
          <div class="card-logo">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <h1 class="card-title">我的报名</h1>
          <p class="card-subtitle">查看您的报名状态</p>
        </div>
        <ApplicationStatus :application="myApplication" />
      </template>

      <!-- 提交成功：显示成功页面 -->
      <template v-else-if="submitSuccess">
        <div class="success-container">
          <div class="success-icon">
            <el-icon :size="48"><CircleCheck /></el-icon>
          </div>
          <h2 class="success-title">报名提交成功</h2>
          <p class="success-desc">感谢您的申请，我们会尽快审核您的报名信息</p>
          <div class="success-tips">
            <div class="tip-item">
              <el-icon><Clock /></el-icon>
              <span>审核通常需要 1-3 个工作日</span>
            </div>
            <div class="tip-item">
              <el-icon><Message /></el-icon>
              <span>审核结果将通过邮件通知您</span>
            </div>
          </div>
          <el-button type="primary" size="large" @click="goHome">
            返回首页
          </el-button>
        </div>
      </template>

      <!-- 报名表单 -->
      <template v-else>
        <div class="card-header">
          <div class="card-logo">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <h1 class="card-title">申请加入</h1>
          <p class="card-subtitle">填写以下信息，开启您的花粉之旅</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="application-form"
          @submit.prevent="handleSubmit"
        >
          <!-- 基本信息 -->
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            <div class="form-grid">
              <el-form-item label="姓名" prop="name">
                <el-input
                  v-model="form.name"
                  placeholder="请输入您的真实姓名"
                  :prefix-icon="User"
                  maxlength="20"
                />
              </el-form-item>

              <el-form-item label="年龄" prop="age">
                <el-input-number
                  v-model="form.age"
                  :min="1"
                  :max="120"
                  placeholder="选填"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>

            <div class="form-grid">
              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="form.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Phone"
                  maxlength="11"
                />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱地址"
                  :prefix-icon="Message"
                />
              </el-form-item>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="form-section">
            <h3 class="section-title">详细信息</h3>

            <el-form-item label="自我介绍" prop="introduction">
              <el-input
                v-model="form.introduction"
                type="textarea"
                placeholder="请简单介绍一下自己，包括您的背景、兴趣爱好等"
                :rows="4"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="加入原因" prop="reason">
              <el-input
                v-model="form.reason"
                type="textarea"
                placeholder="请说明您想加入花粉俱乐部的原因"
                :rows="4"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="特长技能" prop="skills">
              <el-input
                v-model="form.skills"
                type="textarea"
                placeholder="选填，请描述您的特长或技能"
                :rows="3"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
          </div>

          <el-form-item class="form-submit">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ submitting ? '提交中...' : '提交报名' }}
            </el-button>
          </el-form-item>
        </el-form>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Phone, Message, EditPen, Document, CircleCheck, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { submitApplication, getMyApplication } from '@/api/application'
import type { Application, ApplicationFormData } from '@/api/application'
import ApplicationStatus from './components/ApplicationStatus.vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const submitSuccess = ref(false)
const myApplication = ref<Application | null>(null)
const loading = ref(false)

const form = reactive<ApplicationFormData>({
  name: '',
  phone: '',
  email: '',
  age: undefined,
  introduction: '',
  reason: '',
  skills: ''
})

// 手机号验证
const validatePhone = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}

// 邮箱验证
const validateEmail = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  introduction: [
    { required: true, message: '请输入自我介绍', trigger: 'blur' },
    { min: 10, max: 500, message: '自我介绍长度为10-500个字符', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入加入原因', trigger: 'blur' },
    { min: 10, max: 500, message: '加入原因长度为10-500个字符', trigger: 'blur' }
  ]
}

// 检查是否已报名
const checkMyApplication = async () => {
  if (!userStore.isLoggedIn) return

  loading.value = true
  try {
    const res = await getMyApplication()
    if (res.data.data) {
      myApplication.value = res.data.data
    }
  } catch {
    // 未报名，忽略错误
  } finally {
    loading.value = false
  }
}

// 提交报名
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    await submitApplication({ formData: form })
    submitSuccess.value = true
    ElMessage.success('报名提交成功')
  } catch (error: any) {
    if (error?.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  } finally {
    submitting.value = false
  }
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  checkMyApplication()
})
</script>

<style scoped>
.application-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.application-card {
  width: 100%;
  max-width: 560px;
  padding: 40px;
  border-radius: var(--border-radius-lg, 16px);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: 16px;
  color: white;
}

.card-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.card-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

/* 表单样式 */
.application-form {
  margin-top: 24px;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 480px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

.form-submit {
  margin-top: 32px;
  margin-bottom: 0;
}

.form-submit :deep(.el-form-item__content) {
  justify-content: center;
}

.form-submit :deep(.el-button) {
  width: 200px;
  height: 44px;
  font-size: 16px;
}

/* 成功页面样式 */
.success-container {
  text-align: center;
  padding: 40px 20px;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: 50%;
  color: white;
}

.success-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.success-desc {
  margin: 0 0 32px;
  font-size: 15px;
  color: #6b7280;
}

.success-tips {
  background: #f0fdf4;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
}

.tip-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #059669;
  font-size: 14px;
}

.tip-item + .tip-item {
  margin-top: 12px;
}

.tip-item .el-icon {
  font-size: 16px;
}

/* Element Plus 样式覆盖 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  padding: 12px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  padding-left: 12px;
}
</style>
