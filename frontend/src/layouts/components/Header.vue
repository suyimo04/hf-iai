<template>
  <header class="header glass-effect">
    <div class="header__breadcrumb">
      <Breadcrumb />
    </div>

    <div class="header__actions">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="header__user">
          <el-avatar :size="32" :src="userStore.user?.avatar">
            <el-icon :size="16"><User /></el-icon>
          </el-avatar>
          <span class="header__username">{{ userStore.user?.nickname || userStore.user?.username || '用户' }}</span>
          <el-icon class="header__arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <el-icon><User /></el-icon>
              <span>{{ roleLabel }}</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'

const router = useRouter()
const userStore = useUserStore()

const roleLabels: Record<string, string> = {
  ADMIN: '管理员',
  LEADER: '部长',
  VICE_LEADER: '副部长',
  MEMBER: '正式成员',
  INTERN: '实习成员',
  APPLICANT: '申请者'
}

const roleLabel = computed(() => {
  const role = userStore.user?.role
  return role ? roleLabels[role] || role : '未知角色'
})

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 100%;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 0;
}

.header__breadcrumb {
  display: flex;
  align-items: center;
}

.header__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header__user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.header__user:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.header__username {
  font-size: 14px;
  color: #4b5563;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header__arrow {
  font-size: 12px;
  color: #9ca3af;
  transition: transform 0.2s ease;
}

.header__user:hover .header__arrow {
  transform: rotate(180deg);
}
</style>
