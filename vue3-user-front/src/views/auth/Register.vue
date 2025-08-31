<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>创建您的 Code Nest 账户</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
            @blur="checkUsername"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
            @blur="checkEmail"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名（可选）"
            size="large"
            :prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（可选）"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="captcha" v-if="captchaImage">
          <div class="captcha-row">
            <el-input
              v-model="registerForm.captcha"
              placeholder="请输入验证码"
              size="large"
              style="flex: 1;"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img :src="captchaImage" alt="验证码" />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
            class="register-btn"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>
          已有账户？
          <router-link to="/login" class="login-link">
            立即登录
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { captchaApi } from '@/api/captcha'
import { ElMessage } from 'element-plus'
import { User, Message, Lock, UserFilled, Phone } from '@element-plus/icons-vue'

const router = useRouter()

// 表单引用
const registerFormRef = ref()

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  captcha: '',
  captchaKey: ''
})

// 验证码
const captchaImage = ref('')
const loading = ref(false)

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 获取验证码
const loadCaptcha = async () => {
  try {
    const result = await captchaApi.generateCaptcha()
    captchaImage.value = result.captchaImage
    registerForm.captchaKey = result.captchaKey
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  registerForm.captcha = ''
  loadCaptcha()
}

// 检查用户名
const checkUsername = async () => {
  if (registerForm.username && registerForm.username.length >= 3) {
    try {
      await authApi.checkUsername(registerForm.username)
    } catch (error) {
      // API会返回错误信息
    }
  }
}

// 检查邮箱
const checkEmail = async () => {
  if (registerForm.email && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    try {
      await authApi.checkEmail(registerForm.email)
    } catch (error) {
      // API会返回错误信息
    }
  }
}

// 注册处理
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true

    const registerData = {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      nickname: registerForm.realName || undefined,
      phone: registerForm.phone || undefined,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey
    }

    await authApi.register(registerData)
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
    
  } catch (error) {
    console.error('注册失败:', error)
    refreshCaptcha() // 注册失败刷新验证码
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 200%;
    height: 200%;
    background: 
      radial-gradient(circle at 75% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 25% 75%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: float 10s ease-in-out infinite reverse;
  }
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.15),
    0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 25px 70px rgba(0, 0, 0, 0.2),
      0 12px 40px rgba(0, 0, 0, 0.15);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -15px;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #74b9ff, #0984e3);
    border-radius: 2px;
  }
}

.register-header h2 {
  color: #2c3e50;
  margin-bottom: 8px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-header p {
  color: #64748b;
  margin: 0;
  font-size: 16px;
  font-weight: 400;
}

.register-form {
  margin-bottom: 32px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 20px;
  
  .el-input__wrapper {
    border-radius: 14px;
    border: 2px solid rgba(116, 185, 255, 0.1);
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    padding: 10px 16px;
    height: 48px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &:hover {
      border-color: rgba(116, 185, 255, 0.3);
      background: rgba(255, 255, 255, 0.9);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    &.is-focus {
      border-color: #74b9ff;
      background: white;
      box-shadow: 0 0 0 3px rgba(116, 185, 255, 0.1);
    }
  }
  
  .el-input__inner {
    font-size: 14px;
    font-weight: 500;
    color: #2c3e50;
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
  height: 48px;
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

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  border: none;
  border-radius: 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s;
  }
  
  &:hover {
    background: linear-gradient(135deg, #0984e3 0%, #2d3436 100%);
    box-shadow: 0 8px 25px rgba(116, 185, 255, 0.4);
    transform: translateY(-2px);
    
    &::before {
      left: 100%;
    }
  }
  
  &:active {
    transform: translateY(0);
  }
}

.register-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(116, 185, 255, 0.1);
}

.register-footer p {
  color: #64748b;
  margin: 0;
  font-size: 15px;
}

.login-link {
  color: #74b9ff;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  
  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, #74b9ff, #0984e3);
    transition: width 0.3s ease;
  }
  
  &:hover {
    color: #0984e3;
    
    &::after {
      width: 100%;
    }
  }
}

@media (max-width: 480px) {
  .register-card {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .register-header h2 {
    font-size: 28px;
  }
  
  .captcha-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .captcha-image {
    width: 100%;
    max-width: 250px;
  }
}
</style> 