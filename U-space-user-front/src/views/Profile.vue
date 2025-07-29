<template>
  <div class="profile-page">
    <!-- 顶部背景与头像 -->
    <div class="profile-header">
      <el-avatar class="avatar-img" :size="80" :src="user.avatar || defaultAvatar" />
      <el-upload
        class="avatar-upload"
        :show-file-list="false"
        :before-upload="handleBeforeUpload"
        accept="image/*"
      >
        <span class="change-avatar">更换头像</span>
      </el-upload>
      <p class="name">{{ user.name }}</p>
      <p class="class-name">{{ user.className }}</p>
    </div>

    <!-- 操作列表 -->
    <el-card class="action-card" shadow="never">
      <div class="action-item" @click="dialogVisible = true">
        <el-icon class="action-icon"><Unlock /></el-icon>
        <span>修改密码</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="action-item" @click="handleLogout">
        <el-icon class="action-icon"><SwitchButton /></el-icon>
        <span>退出登录</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
      <div class="action-item" @click="goAnnouncements">
        <el-icon class="action-icon"><Document /></el-icon>
        <span>公告查看</span>
        <el-icon class="arrow"><ArrowRight /></el-icon>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="dialogVisible" title="修改密码" width="90%" :close-on-click-modal="false">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-position="top">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" autocomplete="off" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSelfInfo, uploadAvatar, updateAvatar, updatePassword, logout as logoutApi } from '../api/profile'
import { Unlock, ArrowRight, SwitchButton, Document } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { closeSSE } from '../utils/sse'

const defaultAvatar = 'https://cdn.jsdelivr.net/gh/avatars-io/placeholder@master/avatar-default.png'

const user = reactive({
  avatar: '',
  name: '',
  className: '',
})

const fetchInfo = async () => {
  try {
    const res = await getSelfInfo()
    Object.assign(user, res.data || {})
  } catch (e) {
    // global handler
  }
}

onMounted(fetchInfo)

const handleBeforeUpload = async (file) => {
  try {
    const res = await uploadAvatar(file)
    const url = res.data // 假设直接返回 url 字符串
    if (url) {
      // 若需要调用 updateAvatar 记录
      try {
        await updateAvatar(url)
      } catch (e) {
        // ignore
      }
      user.avatar = url
      ElMessage.success('头像已更新')
    } else {
      ElMessage.error('上传失败')
    }
  } catch (e) {
    // error handled globally
  }
  // 阻止 el-upload 自动上传
  return false
}

const dialogVisible = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次输入密码不一致'))
  else callback()
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const pwdRef = ref()

const submitPwd = () => {
  pwdRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await updatePassword(pwdForm.oldPassword, pwdForm.newPassword)
      ElMessage.success('密码修改成功，请重新登录')
      dialogVisible.value = false
      // 清理并跳转登录
      localStorage.removeItem('tokenName')
      localStorage.removeItem('tokenValue')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    } catch (e) {
      // handled globally
    }
  })
}

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '退出确认', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await logoutApi()
    } catch (e) {
      // ignore
    }
    // 清理
    localStorage.removeItem('tokenName')
    localStorage.removeItem('tokenValue')
    localStorage.removeItem('userInfo')
    closeSSE()
    ElMessage.success('已退出登录')
    window.location.href = '/login'
  })
}

const router = useRouter()

const goAnnouncements = () => {
  router.push('/announcements')
}
</script>

<style scoped>
/* 顶层布局 */
.profile-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #f5f6f7;
  min-height: calc(100vh - 112px);
}

/* 头部渐变背景 */
.profile-header {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 40px; /* 上下间距缩小 */
  position: relative;
  color: #fff;
}

.avatar-img {
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.avatar-upload {
  margin-top: 8px;
}
.change-avatar {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.9);
  text-decoration: underline;
  cursor: pointer;
}

.name {
  margin-top: 12px;
  font-size: 1.2rem;
  font-weight: 600;
}

.class-name {
  margin-top: 2px;
  font-size: 0.9rem;
  opacity: 0.9;
}

/* 操作列表 */
.action-card {
  width: 94%;
  margin-top: 12px; /* 与 header 保持适度间距 */
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
}

.action-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  font-size: 0.95rem;
  color: #333;
  cursor: pointer;
  transition: background 0.2s;
}
.action-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}
.action-item:hover {
  background: #f8f8f8;
}

.action-icon {
  margin-right: 10px;
  color: #409eff;
}

.arrow {
  margin-left: auto;
  color: #c0c4cc;
}
</style> 