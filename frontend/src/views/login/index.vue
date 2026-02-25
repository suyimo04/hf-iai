<template>
  <div class="login-page">
    <div class="login-card glass-effect">
      <div class="login-card__header">
        <div class="login-card__logo">
          <el-icon :size="32"><Promotion /></el-icon>
        </div>
        <h1 class="login-card__title">花粉俱乐部</h1>
        <p class="login-card__subtitle">管理系统登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-card__form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-card__submit"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-card__footer">
        <span class="login-card__text">还没有账号？</span>
        <router-link to="/apply" class="login-card__link">申请加入</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Promotion } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')

    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'response' in error) {
      const err = error as { response?: { data?: { message?: string } } }
      ElMessage.error(err.response?.data?.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: var(--border-radius-lg, 16px);
}

.login-card__header {
  text-align: center;
  margin-bottom: 32px;
}

.login-card__logo {
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

.login-card__title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.login-card__subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.login-card__form {
  margin-bottom: 24px;
}

.login-card__submit {
  width: 100%;
}

.login-card__footer {
  text-align: center;
  font-size: 14px;
}

.login-card__text {
  color: #6b7280;
}

.login-card__link {
  color: var(--color-primary, #10b981);
  text-decoration: none;
  margin-left: 4px;
}

.login-card__link:hover {
  text-decoration: underline;
}
</style>
