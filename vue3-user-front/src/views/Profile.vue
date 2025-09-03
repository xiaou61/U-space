<template>
  <div class="profile-container">
    <!-- 导航栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo" @click="goHome">
          <h2>Code Nest</h2>
        </div>
        <div class="nav-menu">
          <el-button @click="goHome" :icon="House">
            返回首页
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="profile-section">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <h3>个人资料</h3>
              <el-button type="primary" @click="editMode = !editMode">
                {{ editMode ? '取消编辑' : '编辑资料' }}
              </el-button>
            </div>
          </template>

          <div class="profile-content" v-loading="loading">
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              :disabled="!editMode"
            >
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="profileForm.username" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="profileForm.email" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="profileForm.realName" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="profileForm.phone" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="创建时间">
                    <el-input v-model="profileForm.createTime" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="最后登录">
                    <el-input v-model="profileForm.lastLoginTime" disabled />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item v-if="editMode">
                <el-button type="primary" @click="handleSave" :loading="saveLoading">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <!-- 修改密码卡片 -->
        <el-card class="password-card">
          <template #header>
            <h3>修改密码</h3>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入当前密码"
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item label="验证码" prop="captcha" v-if="passwordCaptchaImage">
              <div class="captcha-row">
                <el-input
                  v-model="passwordForm.captcha"
                  placeholder="请输入验证码"
                  style="flex: 1;"
                />
                <div class="captcha-image" @click="loadPasswordCaptcha">
                  <img :src="passwordCaptchaImage" alt="验证码" />
                </div>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { captchaApi } from '@/api/captcha'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const profileFormRef = ref()
const passwordFormRef = ref()

// 编辑模式
const editMode = ref(false)
const loading = ref(false)
const saveLoading = ref(false)
const passwordLoading = ref(false)
const passwordCaptchaImage = ref('')

// 个人资料表单
const profileForm = reactive({
  username: '',
  email: '',
  realName: '',
  phone: '',
  createTime: '',
  lastLoginTime: ''
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: ''
})

// 个人资料验证规则
const profileRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 密码修改验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true
  try {
    const userInfo = await userApi.getCurrentUserInfo()
    Object.assign(profileForm, {
      username: userInfo.username,
      email: userInfo.email,
      realName: userInfo.realName || '',
      phone: userInfo.phone || '',
      createTime: userInfo.createTime || '',
      lastLoginTime: userInfo.lastLoginTime || '从未登录'
    })
    userStore.setUserInfo(userInfo)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 保存个人资料
const handleSave = async () => {
  try {
    await profileFormRef.value.validate()
    saveLoading.value = true

    const updateData = {
      email: profileForm.email,
      realName: profileForm.realName || undefined,
      phone: profileForm.phone || undefined
    }

    const result = await userApi.updateUserInfo(updateData)
    userStore.setUserInfo(result)
    
    ElMessage.success('个人资料更新成功')
    editMode.value = false
  } catch (error) {
    console.error('更新个人资料失败:', error)
  } finally {
    saveLoading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
      captcha: passwordForm.captcha,
      captchaKey: passwordForm.captchaKey
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    
    // 修改密码成功后自动登出
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
    
  } catch (error) {
    console.error('修改密码失败:', error)
    loadPasswordCaptcha() // 修改密码失败时刷新验证码
  } finally {
    passwordLoading.value = false
  }
}

// 加载密码修改验证码
const loadPasswordCaptcha = async () => {
  try {
    const result = await captchaApi.generateCaptcha()
    passwordCaptchaImage.value = result.captchaImage
    passwordForm.captchaKey = result.captchaKey
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 重置密码表单
const resetPasswordForm = () => {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    captcha: '',
    captchaKey: ''
  })
  passwordFormRef.value?.clearValidate()
  loadPasswordCaptcha() // 重置时重新加载验证码
}

// 返回首页
const goHome = () => {
  router.push('/')
}



onMounted(() => {
  loadUserInfo()
  loadPasswordCaptcha() // 页面加载时获取密码修改验证码
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  position: relative;
  overflow-x: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 15% 15%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 85% 85%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: float 15s ease-in-out infinite;
  }
}

.header {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 10;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  cursor: pointer;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.logo h2 {
  color: white;
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  background: linear-gradient(45deg, #fff, #e3f2fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  display: flex;
  gap: 16px;
}

.nav-menu :deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white !important;
  font-weight: 600;
  padding: 12px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.5);
    color: white !important;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  }
  
  /* 确保图标和文字都是白色 */
  .el-icon,
  span {
    color: white !important;
  }
}

.main-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 20px;
  position: relative;
  z-index: 1;
}

.profile-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
  animation: slide-up 0.8s ease-out;
  
  @keyframes slide-up {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.profile-card,
.password-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 16px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #74b9ff, #0984e3);
  }
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 25px 50px rgba(0, 0, 0, 0.2),
      0 12px 24px rgba(0, 0, 0, 0.15);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px 0;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.profile-content {
  padding: 32px;
}

.profile-content :deep(.el-form-item) {
  margin-bottom: 24px;
  
  .el-form-item__label {
    font-weight: 600;
    color: #2c3e50;
  }
  
  .el-input__wrapper {
    border-radius: 12px;
    border: 2px solid rgba(116, 185, 255, 0.1);
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &:hover {
      border-color: rgba(116, 185, 255, 0.3);
      background: rgba(255, 255, 255, 0.9);
    }
    
    &.is-focus {
      border-color: #74b9ff;
      background: white;
      box-shadow: 0 0 0 3px rgba(116, 185, 255, 0.1);
    }
    
    &.is-disabled {
      background: rgba(245, 247, 250, 0.8);
      border-color: rgba(200, 200, 200, 0.3);
    }
  }
}

.captcha-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.captcha-image {
  cursor: pointer;
  border: 2px solid rgba(116, 185, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  width: 160px;
  height: 40px;
  transition: all 0.3s ease;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(45deg, rgba(116, 185, 255, 0.1), rgba(9, 132, 227, 0.1));
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &:hover {
    border-color: #74b9ff;
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(116, 185, 255, 0.2);
    
    &::before {
      opacity: 1;
    }
  }
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  position: relative;
  z-index: 1;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  
  &.el-button--primary {
    background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
    border: none;
    
    &:hover {
      background: linear-gradient(135deg, #0984e3 0%, #2d3436 100%);
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(116, 185, 255, 0.4);
    }
  }
  
  &:not(.el-button--primary) {
    background: rgba(116, 185, 255, 0.1);
    border: 2px solid rgba(116, 185, 255, 0.2);
    color: #74b9ff;
    
    &:hover {
      background: rgba(116, 185, 255, 0.2);
      border-color: #74b9ff;
      transform: translateY(-1px);
    }
  }
}

/* 表单行样式 */
:deep(.el-row) {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .header-content {
    height: 60px;
    padding: 0 16px;
  }
  
  .logo h2 {
    font-size: 24px;
  }
  
  .nav-menu {
    gap: 8px;
  }
  
  .nav-menu :deep(.el-button) {
    padding: 8px 12px;
    font-size: 14px;
    color: white !important;
    
    /* 移动端也确保图标和文字都是白色 */
    .el-icon,
    span {
      color: white !important;
    }
  }
  
  .main-content {
    padding: 20px 16px;
  }
  
  .profile-card,
  .password-card {
    margin: 0 8px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
    padding: 20px 24px 0;
  }
  
  .profile-content {
    padding: 24px;
  }
  
  .captcha-row {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .captcha-image {
    width: 100%;
    max-width: 250px;
    align-self: center;
  }
}
</style> 