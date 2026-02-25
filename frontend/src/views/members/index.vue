<template>
  <div class="members-page">
    <BaseCard title="成员管理">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索用户名/昵称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="queryParams.role"
          placeholder="角色筛选"
          clearable
          style="width: 140px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in roleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select
          v-model="queryParams.status"
          placeholder="状态筛选"
          clearable
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 成员表格 -->
      <MemberTable
        :data="userList"
        :loading="loading"
        :can-edit-role="canEditRole"
        :can-edit-status="canEditStatus"
        :can-delete="canDelete"
        @edit-role="handleEditRole"
        @edit-status="handleEditStatus"
        @delete="handleDelete"
      />

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </BaseCard>

    <!-- 角色修改弹窗 -->
    <RoleDialog
      v-model="roleDialogVisible"
      :user-id="currentUser?.id ?? null"
      :user-name="currentUser?.nickname || currentUser?.username || ''"
      :current-role="currentUser?.role ?? null"
      @success="fetchUsers"
    />

    <!-- 状态修改弹窗 -->
    <StatusDialog
      v-model="statusDialogVisible"
      :user-id="currentUser?.id ?? null"
      :user-name="currentUser?.nickname || currentUser?.username || ''"
      :current-status="currentUser?.status ?? null"
      @success="fetchUsers"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseCard from '@/components/base/BaseCard.vue'
import MemberTable from './components/MemberTable.vue'
import RoleDialog from './components/RoleDialog.vue'
import StatusDialog from './components/StatusDialog.vue'
import { getUsers, deleteUser } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { User } from '@/types'

const userStore = useUserStore()

const loading = ref(false)
const userList = ref<User[]>([])
const total = ref(0)
const currentUser = ref<User | null>(null)
const roleDialogVisible = ref(false)
const statusDialogVisible = ref(false)

const queryParams = reactive({
  keyword: '',
  role: '',
  status: '',
  page: 1,
  size: 10
})

const roleOptions = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'LEADER', label: '组长' },
  { value: 'VICE_LEADER', label: '副组长' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'INTERN', label: '实习成员' },
  { value: 'APPLICANT', label: '应聘者' }
]

const statusOptions = [
  { value: 'ACTIVE', label: '正常' },
  { value: 'INACTIVE', label: '禁用' },
  { value: 'PENDING', label: '待审核' }
]

// 权限判断
const canEditRole = computed(() => userStore.hasPermission(['ADMIN', 'LEADER']))
const canEditStatus = computed(() => userStore.hasPermission(['ADMIN', 'LEADER']))
const canDelete = computed(() => userStore.hasPermission(['ADMIN']))

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUsers(queryParams)
    userList.value = res.data.data.content
    total.value = res.data.data.totalElements
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  fetchUsers()
}

function handleReset() {
  queryParams.keyword = ''
  queryParams.role = ''
  queryParams.status = ''
  queryParams.page = 1
  fetchUsers()
}

function handleEditRole(user: User) {
  currentUser.value = user
  roleDialogVisible.value = true
}

function handleEditStatus(user: User) {
  currentUser.value = user
  statusDialogVisible.value = true
}

async function handleDelete(user: User) {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e) {
    if (e !== 'cancel') {
      // 错误已在request拦截器中处理
    }
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.members-page {
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
