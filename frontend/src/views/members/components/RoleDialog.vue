<template>
  <el-dialog
    v-model="visible"
    title="修改角色"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="80px">
      <el-form-item label="当前用户">
        <span>{{ userName }}</span>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
          <el-option
            v-for="item in roleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUserRole } from '@/api/user'
import type { Role } from '@/types'

const props = defineProps<{
  modelValue: boolean
  userId: number | null
  userName: string
  currentRole: Role | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const visible = ref(false)
const loading = ref(false)
const form = reactive({
  role: '' as string
})

const roleOptions = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'LEADER', label: '组长' },
  { value: 'VICE_LEADER', label: '副组长' },
  { value: 'MEMBER', label: '正式成员' },
  { value: 'INTERN', label: '实习成员' },
  { value: 'APPLICANT', label: '应聘者' }
]

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.currentRole) {
    form.role = props.currentRole
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
  form.role = ''
}

async function handleSubmit() {
  if (!props.userId) return
  if (!form.role) {
    ElMessage.warning('请选择角色')
    return
  }

  loading.value = true
  try {
    await updateUserRole(props.userId, form.role)
    ElMessage.success('角色修改成功')
    emit('success')
    handleClose()
  } finally {
    loading.value = false
  }
}
</script>
