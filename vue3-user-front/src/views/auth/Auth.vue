<template>
  <div class="auth-container" :class="{ 'register-mode': isRegister }">
    <!-- 左侧视觉区 -->
    <div class="auth-left" :class="{ 'slide-out': isRegister }">
      <!-- 波浪分割线 -->
      <svg class="wave-divider" viewBox="0 0 100 800" preserveAspectRatio="none">
        <path d="M100,0 C60,100 100,200 60,300 C20,400 80,500 40,600 C0,700 60,750 100,800 L100,0 Z" 
              fill="#f8fafc"/>
      </svg>
      
      <!-- 粒子背景 -->
      <div class="particles">
        <span v-for="i in 50" :key="i" class="particle"></span>
      </div>
      
      <!-- 3D悬浮卡片 -->
      <div class="floating-cards">
        <div class="float-card card-1"><div class="card-icon">📝</div><div class="card-text">面试刷题</div></div>
        <div class="float-card card-2"><div class="card-icon">🤖</div><div class="card-text">AI模拟面试</div></div>
        <div class="float-card card-3"><div class="card-icon">💻</div><div class="card-text">代码工坊</div></div>
        <div class="float-card card-4"><div class="card-icon">📚</div><div class="card-text">知识图谱</div></div>
        <div class="float-card card-5"><div class="card-icon">📄</div><div class="card-text">在线简历</div></div>
        <div class="float-card card-6"><div class="card-icon">💬</div><div class="card-text">即时聊天</div></div>
        <div class="float-card card-7"><div class="card-icon">🏆</div><div class="card-text">积分抽奖</div></div>
        <div class="float-card card-8"><div class="card-icon">📅</div><div class="card-text">计划打卡</div></div>
      </div>
      
      <!-- 品牌信息 -->
      <div class="brand-section">
        <h1 class="brand-title">Code Nest</h1>
        <p class="brand-subtitle">开发者成长社区</p>
        <p class="brand-desc">刷题 · 面试 · 知识图谱 · 代码工坊 · 简历 · 社区</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="auth-right" :class="{ 'slide-in': isRegister }">
      <div class="form-container">
        <!-- 登录表单 -->
        <transition name="slide-fade" mode="out-in">
          <div v-if="!isRegister" key="login" class="auth-card">
            <div class="auth-header">
              <h2>欢迎回来</h2>
              <p>登录您的 Code Nest 账户</p>
            </div>

            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="auth-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="用户名或邮箱" size="large" :prefix-icon="User" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" 
                          :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
              </el-form-item>
              <el-form-item prop="captcha" v-if="captchaImage">
                <div class="captcha-row">
                  <el-input v-model="loginForm.captcha" placeholder="验证码" size="large" class="captcha-input" />
                  <div class="captcha-image" @click="refreshCaptcha">
                    <img :src="captchaImage" alt="验证码" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="submit-btn">
                  登 录
                </el-button>
              </el-form-item>
            </el-form>

            <div class="auth-footer">
              <p>还没有账户？<a @click="toggleMode" class="switch-link">立即注册</a></p>
            </div>
          </div>

          <!-- 注册表单 -->
          <div v-else key="register" class="auth-card">
            <div class="auth-header">
              <h2>创建账户</h2>
              <p>注册 Code Nest 开始学习之旅</p>
            </div>

            <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="auth-form">
              <div class="form-row">
                <el-form-item prop="username">
                  <el-input v-model="registerForm.username" placeholder="用户名" size="large" :prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="email">
                  <el-input v-model="registerForm.email" placeholder="邮箱" size="large" :prefix-icon="Message" />
                </el-form-item>
              </div>
              <div class="form-row">
                <el-form-item prop="password">
                  <el-input v-model="registerForm.password" type="password" placeholder="密码" size="large" 
                            :prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" 
                            size="large" :prefix-icon="Lock" show-password />
                </el-form-item>
              </div>
              <el-form-item prop="captcha" v-if="captchaImage">
                <div class="captcha-row">
                  <el-input v-model="registerForm.captcha" placeholder="验证码" size="large" class="captcha-input" />
                  <div class="captcha-image" @click="refreshCaptcha">
                    <img :src="captchaImage" alt="验证码" />
                  </div>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="submit-btn register-btn">
                  注 册
                </el-button>
              </el-form-item>
            </el-form>

            <div class="auth-footer">
              <p>已有账户？<a @click="toggleMode" class="switch-link">立即登录</a></p>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { captchaApi } from '@/api/captcha'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 模式切换
const isRegister = ref(false)

// 根据路由初始化模式
watch(() => route.path, (path) => {
  isRegister.value = path === '/register'
}, { immediate: true })

const toggleMode = () => {
  isRegister.value = !isRegister.value
  router.replace(isRegister.value ? '/register' : '/login')
  refreshCaptcha()
}

// 表单引用
const loginFormRef = ref()
const registerFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  captcha: '',
  captchaKey: ''
})

const captchaImage = ref('')
const loading = ref(false)

// 验证规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
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
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取验证码
const loadCaptcha = async () => {
  try {
    const result = await captchaApi.generateCaptcha()
    captchaImage.value = result.captchaImage
    if (isRegister.value) {
      registerForm.captchaKey = result.captchaKey
    } else {
      loginForm.captchaKey = result.captchaKey
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

const refreshCaptcha = () => {
  loginForm.captcha = ''
  registerForm.captcha = ''
  loadCaptcha()
}

// 登录
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const result = await authApi.login({
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey
    })
    userStore.login(result.accessToken, result.userInfo)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    loading.value = true
    await authApi.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey
    })
    ElMessage.success('注册成功，请登录')
    toggleMode()
  } catch (error) {
    console.error('注册失败:', error)
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  background: #0f0f1a;
  overflow: hidden;
}

/* ========== 左侧视觉区 ========== */
.auth-left {
  flex: 6;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  overflow: hidden;
  transition: flex 0.6s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}

/* 注册模式下左侧缩小 */
.auth-left.slide-out {
  flex: 5;
}

.auth-left.slide-out .brand-section {
  transform: scale(0.95);
  opacity: 0.9;
}

.auth-left.slide-out .floating-cards {
  transform: scale(0.98) translateX(-20px);
}

/* 波浪分割线 */
.wave-divider {
  position: absolute;
  right: -1px;
  top: 0;
  height: 100%;
  width: 80px;
  z-index: 10;
}

/* 粒子效果 */
.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(99, 179, 237, 0.6);
  border-radius: 50%;
  animation: floatParticle 15s infinite;
  opacity: 0;
}

.particle:nth-child(1) { left: 10%; animation-delay: 0s; }
.particle:nth-child(2) { left: 20%; animation-delay: 1s; }
.particle:nth-child(3) { left: 30%; animation-delay: 2s; }
.particle:nth-child(4) { left: 40%; animation-delay: 3s; }
.particle:nth-child(5) { left: 50%; animation-delay: 4s; }
.particle:nth-child(6) { left: 60%; animation-delay: 5s; }
.particle:nth-child(7) { left: 70%; animation-delay: 6s; }
.particle:nth-child(8) { left: 80%; animation-delay: 7s; }
.particle:nth-child(9) { left: 90%; animation-delay: 8s; }
.particle:nth-child(10) { left: 15%; animation-delay: 0.5s; }
.particle:nth-child(11) { left: 25%; animation-delay: 1.5s; }
.particle:nth-child(12) { left: 35%; animation-delay: 2.5s; }
.particle:nth-child(13) { left: 45%; animation-delay: 3.5s; }
.particle:nth-child(14) { left: 55%; animation-delay: 4.5s; }
.particle:nth-child(15) { left: 65%; animation-delay: 5.5s; }
.particle:nth-child(16) { left: 75%; animation-delay: 6.5s; }
.particle:nth-child(17) { left: 85%; animation-delay: 7.5s; }
.particle:nth-child(18) { left: 95%; animation-delay: 8.5s; }

@keyframes floatParticle {
  0% { transform: translateY(100vh) scale(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-100vh) scale(1); opacity: 0; }
}

/* 3D悬浮卡片 */
.floating-cards {
  position: absolute;
  width: 100%;
  height: 100%;
  perspective: 1000px;
}

.float-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transform-style: preserve-3d;
  animation: float3d 6s ease-in-out infinite;
}

.float-card .card-icon { font-size: 22px; }
.float-card .card-text { color: rgba(255, 255, 255, 0.9); font-size: 12px; font-weight: 500; }

.card-1 { top: 8%; left: 8%; animation-delay: 0s; }
.card-2 { top: 8%; right: 15%; animation-delay: -1s; }
.card-3 { top: 32%; left: 5%; animation-delay: -2s; }
.card-4 { top: 35%; right: 8%; animation-delay: -3s; }
.card-5 { bottom: 35%; left: 10%; animation-delay: -4s; }
.card-6 { bottom: 32%; right: 5%; animation-delay: -5s; }
.card-7 { bottom: 8%; left: 6%; animation-delay: -6s; }
.card-8 { bottom: 8%; right: 12%; animation-delay: -7s; }

@keyframes float3d {
  0%, 100% { transform: translateY(0) rotateX(0deg) rotateY(0deg); }
  25% { transform: translateY(-12px) rotateX(5deg) rotateY(5deg); }
  50% { transform: translateY(-4px) rotateX(0deg) rotateY(-5deg); }
  75% { transform: translateY(-16px) rotateX(-5deg) rotateY(3deg); }
}

/* 品牌信息 */
.brand-section {
  position: relative;
  z-index: 10;
  text-align: center;
  transition: all 0.6s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}

.floating-cards {
  transition: all 0.6s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  background: linear-gradient(135deg, #63b3ed 0%, #4fd1c5 50%, #9f7aea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.brand-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 10px;
  font-weight: 500;
}

.brand-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px;
}

/* ========== 右侧表单区 ========== */
.auth-right {
  flex: 4;
  min-width: 480px;
  max-width: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 50px;
  background: #f8fafc;
  position: relative;
  transition: flex 0.6s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}

/* 注册模式下右侧扩大 */
.auth-right.slide-in {
  flex: 5;
}

.form-container {
  width: 100%;
  max-width: 480px;
}

.auth-card {
  background: white;
  border-radius: 20px;
  padding: 44px 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

/* 切换动画 - 增强版 */
.slide-fade-enter-active {
  transition: all 0.5s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}
.slide-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.68, -0.05, 0.32, 1.05);
}
.slide-fade-enter-from {
  transform: translateX(50px) scale(0.95);
  opacity: 0;
}
.slide-fade-leave-to {
  transform: translateX(-50px) scale(0.95);
  opacity: 0;
}

/* 注册模式下的卡片样式 */
.register-mode .auth-card {
  box-shadow: 0 15px 50px rgba(159, 122, 234, 0.15);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-header h2 {
  color: #1a202c;
  margin-bottom: 8px;
  font-size: 26px;
  font-weight: 700;
}

.auth-header p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.auth-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.3s ease;
  padding: 4px 12px;
  box-shadow: none;
}

.auth-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e0;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  border-color: #4299e1;
  background: white;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.15);
}

.auth-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #2d3748;
  height: 40px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-row .el-form-item {
  flex: 1;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  width: 140px;
  height: 46px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.captcha-image:hover {
  border-color: #4299e1;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #4299e1 0%, #667eea 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(66, 153, 225, 0.35);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(66, 153, 225, 0.45);
}

.register-btn {
  background: linear-gradient(135deg, #9f7aea 0%, #ed64a6 100%);
  box-shadow: 0 4px 15px rgba(159, 122, 234, 0.35);
}

.register-btn:hover {
  box-shadow: 0 6px 20px rgba(159, 122, 234, 0.45);
}

.auth-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.auth-footer p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.switch-link {
  color: #4299e1;
  font-weight: 600;
  margin-left: 4px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.switch-link:hover {
  color: #2b6cb0;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .auth-left {
    display: none;
  }
  
  .auth-right {
    flex: 1;
    min-width: auto;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .auth-right {
    padding: 20px;
  }
  
  .auth-card {
    padding: 32px 24px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
