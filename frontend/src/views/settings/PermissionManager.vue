<template>
  <div class="permission-manager">
    <div class="page-header">
      <h2>权限管理</h2>
      <el-button
        v-permission="'permission:create'"
        type="primary"
        :icon="Plus"
        @click="handleCreate"
      >
        新建权限
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 角色列表 -->
      <el-col :span="6">
        <el-card class="role-card">
          <template #header>
            <span>角色列表</span>
          </template>
          <div class="role-list">
            <div
              v-for="role in roles"
              :key="role.value"
              :class="['role-item', { active: selectedRole === role.value }]"
              @click="handleSelectRole(role.value)"
            >
              <el-icon><User /></el-icon>
              <span>{{ role.label }}</span>
              <el-tag size="small" type="info">{{ getRolePermissionCount(role.value) }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 权限配置 -->
      <el-col :span="18">
        <el-card class="permission-card">
          <template #header>
            <div class="card-header">
              <span>{{ getSelectedRoleLabel() }} - 权限配置</span>
              <el-button
                v-permission="'permission:assign'"
                type="primary"
                size="small"
                @click="handleSavePermissions"
                :loading="saving"
              >
                保存配置
              </el-button>
            </div>
          </template>

          <div class="permission-groups" v-loading="loading">
            <div v-for="group in permissionGroups" :key="group.name" class="permission-group">
              <div class="group-header">
                <el-checkbox
                  :model-value="isGroupAllChecked(group)"
                  :indeterminate="isGroupIndeterminate(group)"
                  @change="handleGroupCheckChange(group, $event)"
                >
                  {{ group.label }}
                </el-checkbox>
              </div>
              <div class="group-permissions">
                <el-checkbox
                  v-for="perm in group.permissions"
                  :key="perm.id"
                  :model-value="checkedPermissions.has(perm.id)"
                  @change="handlePermissionChange(perm.id, $event)"
                >
                  {{ perm.name }}
                  <el-tooltip :content="perm.code" placement="top">
                    <el-icon class="code-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </el-checkbox>
              </div>
            </div>

            <el-empty v-if="permissionGroups.length === 0" description="暂无权限数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建权限对话框 -->
    <el-dialog v-model="dialogVisible" title="新建权限" width="500px">
      <el-form :model="permissionForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="permissionForm.code" placeholder="如: user:create" />
        </el-form-item>
        <el-form-item label="所属菜单" prop="menuId">
          <el-tree-select
            v-model="permissionForm.menuId"
            :data="menuTree"
            :props="{ label: 'name', value: 'id' }"
            placeholder="请选择所属菜单"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="permissionForm.description"
            type="textarea"
            :rows="3"
            placeholder="权限描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, User, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getPermissions,
  createPermission,
  assignPermissionsToRole,
  getRolePermissions
} from '@/api/permission'
import { getMenuTree } from '@/api/menu'

defineOptions({ name: 'PermissionManager' })

interface Permission {
  id: number
  name: string
  code: string
  menuId?: number
  menuName?: string
  description?: string
}

interface PermissionGroup {
  name: string
  label: string
  permissions: Permission[]
}

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const roles = [
  { value: 'ADMIN', label: '超级管理员' },
  { value: 'LEADER', label: '群主' },
  { value: 'VICE_LEADER', label: '副群主' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'PROBATION', label: '试用成员' }
]

const selectedRole = ref('ADMIN')
const allPermissions = ref<Permission[]>([])
const checkedPermissions = ref<Set<number>>(new Set())
const rolePermissionsMap = ref<Map<string, number[]>>(new Map())
const menuTree = ref<any[]>([])

const permissionForm = reactive({
  name: '',
  code: '',
  menuId: undefined as number | undefined,
  description: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[a-z]+:[a-z]+$/, message: '格式如: module:action', trigger: 'blur' }
  ]
}

const permissionGroups = computed<PermissionGroup[]>(() => {
  const groups: Map<string, PermissionGroup> = new Map()

  allPermissions.value.forEach(perm => {
    const menuName = perm.menuName || '其他'
    if (!groups.has(menuName)) {
      groups.set(menuName, {
        name: menuName,
        label: menuName,
        permissions: []
      })
    }
    groups.get(menuName)!.permissions.push(perm)
  })

  return Array.from(groups.values())
})

function getSelectedRoleLabel(): string {
  return roles.find(r => r.value === selectedRole.value)?.label || ''
}

function getRolePermissionCount(role: string): number {
  return rolePermissionsMap.value.get(role)?.length || 0
}

function isGroupAllChecked(group: PermissionGroup): boolean {
  return group.permissions.every(p => checkedPermissions.value.has(p.id))
}

function isGroupIndeterminate(group: PermissionGroup): boolean {
  const checkedCount = group.permissions.filter(p => checkedPermissions.value.has(p.id)).length
  return checkedCount > 0 && checkedCount < group.permissions.length
}

function handleGroupCheckChange(group: PermissionGroup, checked: boolean | string | number) {
  group.permissions.forEach(p => {
    if (checked) {
      checkedPermissions.value.add(p.id)
    } else {
      checkedPermissions.value.delete(p.id)
    }
  })
}

function handlePermissionChange(permId: number, checked: boolean | string | number) {
  if (checked) {
    checkedPermissions.value.add(permId)
  } else {
    checkedPermissions.value.delete(permId)
  }
}

async function fetchPermissions() {
  loading.value = true
  try {
    const res = await getPermissions()
    allPermissions.value = res.data.data || []
  } catch (e) {
    console.error('获取权限列表失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchRolePermissions(role: string) {
  try {
    const res = await getRolePermissions(role)
    const permIds = (res.data.data || []).map((p: Permission) => p.id)
    rolePermissionsMap.value.set(role, permIds)
    if (role === selectedRole.value) {
      checkedPermissions.value = new Set(permIds)
    }
  } catch (e) {
    console.error('获取角色权限失败', e)
  }
}

async function fetchMenuTree() {
  try {
    const res = await getMenuTree()
    menuTree.value = res.data.data || []
  } catch (e) {
    console.error('获取菜单树失败', e)
  }
}

function handleSelectRole(role: string) {
  selectedRole.value = role
  const cached = rolePermissionsMap.value.get(role)
  if (cached) {
    checkedPermissions.value = new Set(cached)
  } else {
    fetchRolePermissions(role)
  }
}

async function handleSavePermissions() {
  saving.value = true
  try {
    await assignPermissionsToRole({
      role: selectedRole.value,
      permissionIds: Array.from(checkedPermissions.value)
    })
    rolePermissionsMap.value.set(selectedRole.value, Array.from(checkedPermissions.value))
    ElMessage.success('权限配置保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function handleCreate() {
  Object.assign(permissionForm, {
    name: '',
    code: '',
    menuId: undefined,
    description: ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    await createPermission(permissionForm)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchPermissions()
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPermissions()
  fetchMenuTree()
  roles.forEach(r => fetchRolePermissions(r.value))
})
</script>

<style scoped>
.permission-manager {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.role-card {
  height: calc(100vh - 180px);
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.role-item:hover {
  background: #f5f7fa;
}

.role-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.role-item .el-tag {
  margin-left: auto;
}

.permission-card {
  height: calc(100vh - 180px);
}

.permission-card :deep(.el-card__body) {
  height: calc(100% - 60px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.permission-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.permission-group {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

.group-header {
  padding: 12px 16px;
  background: #f5f7fa;
  font-weight: 600;
}

.group-permissions {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
}

.group-permissions .el-checkbox {
  margin-right: 0;
}

.code-icon {
  margin-left: 4px;
  color: #909399;
  font-size: 12px;
}
</style>
