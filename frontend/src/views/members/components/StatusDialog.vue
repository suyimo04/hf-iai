<template>
  <el-dialog
    v-model="visible"
    title="修改状态"
    width="400px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" label-width="80px">
      <el-form-item label="当前用户">
        <span>{{ userName }}</span>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
          <el-option
            v-for="item in statusOptions"
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
import { updateUserStatus } from '@/api/user'

const props = defineProps<{
  modelValue: boolean
  userId: number | null
  userName: string
  currentStatus: string | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const visible = ref(false)
const loading = ref(false)
const form = reactive({
  status: '' as string
})

const statusOptions = [
  { value: 'ACTIVE', label: '正常' },
  { value: 'INACTIVE', label: '禁用' },
  { value: 'PENDING', label: '待审核' }
]

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.currentStatus) {
    form.status = props.currentStatus
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
  form.status = ''
}

async function handleSubmit() {
  if (!props.userId) return
  if (!form.status) {
    ElMessage.warning('请选择状态')
    return
  }

  loading.value = true
  try {
    await updateUserStatus(props.userId, form.status)
    ElMessage.success('状态修改成功')
    emit('success')
    handleClose()
  } finally {
    loading.value = false
  }
}
</script>
