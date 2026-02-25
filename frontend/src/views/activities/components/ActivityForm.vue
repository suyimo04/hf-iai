<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑活动' : '新建活动'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="活动标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入活动标题" />
      </el-form-item>

      <el-form-item label="活动描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入活动描述"
        />
      </el-form-item>

      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="活动地点" prop="location">
        <el-input v-model="form.location" placeholder="请输入活动地点" />
      </el-form-item>

      <el-form-item label="最大人数" prop="maxParticipants">
        <el-input-number v-model="form.maxParticipants" :min="1" :max="9999" />
      </el-form-item>

      <el-form-item label="签到积分" prop="signinPoints">
        <el-input-number v-model="form.signinPoints" :min="0" :max="9999" />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="进行中" value="ONGOING" />
          <el-option label="已结束" value="ENDED" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Activity, ActivityForm, ActivityStatus } from '@/api/activity'

const props = defineProps<{
  modelValue: boolean
  activity?: Activity | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [form: ActivityForm]
}>()

const visible = ref(props.modelValue)
const loading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const getDefaultForm = (): ActivityForm => ({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 50,
  signinPoints: 10,
  status: 'DRAFT' as ActivityStatus
})

const form = reactive<ActivityForm>(getDefaultForm())

const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.activity, (activity) => {
  if (activity) {
    isEdit.value = true
    Object.assign(form, {
      title: activity.title,
      description: activity.description,
      startTime: activity.startTime,
      endTime: activity.endTime,
      location: activity.location,
      maxParticipants: activity.maxParticipants,
      signinPoints: activity.signinPoints,
      status: activity.status
    })
  } else {
    isEdit.value = false
    Object.assign(form, getDefaultForm())
  }
}, { immediate: true })

const handleClose = () => {
  visible.value = false
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    emit('submit', { ...form })
    handleClose()
  } finally {
    loading.value = false
  }
}
</script>
