<template>
  <el-dialog
    v-model="visible"
    title="编辑个人信息"
    width="600px"
    :before-close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      @submit.prevent
    >
      <el-form-item label="真实姓名" prop="realName">
        <el-input
          v-model="form.realName"
          placeholder="请输入真实姓名"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          type="email"
          placeholder="请输入邮箱地址"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input
          v-model="form.phone"
          placeholder="请输入手机号"
          clearable
        />
      </el-form-item>
      
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender">
          <el-radio :label="0">未知</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="头像URL" prop="avatar">
        <el-input
          v-model="form.avatar"
          placeholder="请输入头像图片URL"
          clearable
        />
        <div v-if="form.avatar" style="margin-top: 10px;">
          <el-avatar :size="60" :src="form.avatar">
            预览
          </el-avatar>
        </div>
      </el-form-item>
      
      <el-form-item label="个人简介" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="请输入个人简介"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

// 控制对话框显示
const visible = ref(false)

// 表单数据
const form = reactive({
  realName: '',
  email: '',
  phone: '',
  gender: 0,
  avatar: '',
  remark: ''
})

// 表单验证规则
const rules = reactive({
  email: [
    {
      type: 'email',
      message: '请输入正确的邮箱格式',
      trigger: 'blur'
    }
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号格式',
      trigger: 'blur'
    }
  ],
  realName: [
    {
      max: 50,
      message: '真实姓名长度不能超过50个字符',
      trigger: 'blur'
    }
  ]
})

// 监听props变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    // 初始化表单数据
    Object.assign(form, {
      realName: props.userInfo.realName || '',
      email: props.userInfo.email || '',
      phone: props.userInfo.phone || '',
      gender: props.userInfo.gender || 0,
      avatar: props.userInfo.avatar || '',
      remark: props.userInfo.remark || ''
    })
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 关闭对话框
const handleClose = () => {
  visible.value = false
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 调用更新接口
    await authApi.updateProfile(form)
    
    ElMessage.success('个人信息更新成功')
    
    // 重新获取用户信息
    await userStore.getUserInfo()
    
    emit('success')
    handleClose()
    
  } catch (error) {
    if (error.name !== 'FormValidateError') {
      console.error('更新个人信息失败:', error)
      ElMessage.error(error.message || '更新失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 