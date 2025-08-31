<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>登录您的 Code Nest 账户</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="captcha" v-if="captchaImage">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
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
            @click="handleLogin"
            class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>
          还没有账户？
          <router-link to="/register" class="register-link">
            立即注册
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { captchaApi } from '@/api/captcha'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: ''
})

// 验证码
const captchaImage = ref('')
const loading = ref(false)

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
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
    loginForm.captchaKey = result.captchaKey
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  loginForm.captcha = ''
  loadCaptcha()
}

// 登录处理
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const loginData = {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey
    }

    const result = await authApi.login(loginData)
    
    // 保存用户信息和token
    userStore.login(result.accessToken, result.userInfo)
    
    ElMessage.success('登录成功')
    router.push('/')
    
  } catch (error) {
    console.error('登录失败:', error)
    refreshCaptcha() // 登录失败刷新验证码
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.login-container {
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
    left: -50%;
    width: 200%;
    height: 200%;
    background: 
      radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: float 8s ease-in-out infinite;
  }
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  width: 100%;
  max-width: 420px;
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

.login-header {
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

.login-header h2 {
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

.login-header p {
  color: #64748b;
  margin: 0;
  font-size: 16px;
  font-weight: 400;
}

.login-form {
  margin-bottom: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
  
  .el-input__wrapper {
    border-radius: 16px;
    border: 2px solid rgba(116, 185, 255, 0.1);
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    padding: 12px 16px;
    height: 52px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &:hover {
      border-color: rgba(116, 185, 255, 0.3);
      background: rgba(255, 255, 255, 0.9);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    &.is-focus {
      border-color: #74b9ff;
      background: white;
      box-shadow: 0 0 0 4px rgba(116, 185, 255, 0.1);
    }
  }
  
  .el-input__inner {
    font-size: 15px;
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
  height: 52px;
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

.login-btn {
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

.login-footer {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(116, 185, 255, 0.1);
}

.login-footer p {
  color: #64748b;
  margin: 0;
  font-size: 15px;
}

.register-link {
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
  .login-card {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .login-header h2 {
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