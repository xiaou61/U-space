<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="发布讨论"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <el-form-item label="标题" prop="title">
        <el-input 
          v-model="form.title" 
          placeholder="请输入讨论标题"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="内容" prop="content">
        <el-input 
          v-model="form.content" 
          type="textarea"
          :rows="8"
          placeholder="请输入讨论内容..."
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        发布
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import teamApi from '@/api/team'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  teamId: { type: [String, Number], required: true }
})

const emit = defineEmits(['update:modelValue', 'success'])

const formRef = ref()
const submitting = ref(false)

const form = ref({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入讨论标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入讨论内容', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  if (val) {
    resetForm()
  }
})

const resetForm = () => {
  form.value = {
    title: '',
    content: ''
  }
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    await teamApi.createDiscussion(props.teamId, form.value)
    ElMessage.success('发布成功')
    emit('update:modelValue', false)
    emit('success')
  } catch (error) {
    console.error('发布失败:', error)
  } finally {
    submitting.value = false
  }
}
</script>
