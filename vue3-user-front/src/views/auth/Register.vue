<template>
  <div class="register-container">
    <!-- 左侧视觉区 -->
    <div class="register-left">
      <!-- 波浪背景 -->
      <div class="wave-bg">
        <svg class="wave wave1" viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path fill="rgba(159, 122, 234, 0.1)" d="M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
        <svg class="wave wave2" viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path fill="rgba(237, 100, 166, 0.08)" d="M0,64L48,80C96,96,192,128,288,128C384,128,480,96,576,90.7C672,85,768,107,864,144C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
        </svg>
      </div>
      <!-- 粒子背景 -->
      <div class="particles">
        <span v-for="i in 50" :key="i" class="particle"></span>
      </div>
      <!-- 3D装饰卡片 - 展示平台功能 -->
      <div class="floating-cards">
        <div class="float-card card-1">
          <div class="card-icon">📝</div>
          <div class="card-text">面试刷题</div>
        </div>
        <div class="float-card card-2">
          <div class="card-icon">🤖</div>
          <div class="card-text">AI模拟面试</div>
        </div>
        <div class="float-card card-3">
          <div class="card-icon">💻</div>
          <div class="card-text">代码工坊</div>
        </div>
        <div class="float-card card-4">
          <div class="card-icon">📚</div>
          <div class="card-text">知识图谱</div>
        </div>
        <div class="float-card card-5">
          <div class="card-icon">📄</div>
          <div class="card-text">在线简历</div>
        </div>
        <div class="float-card card-6">
          <div class="card-icon">💬</div>
          <div class="card-text">即时聊天</div>
        </div>
        <div class="float-card card-7">
          <div class="card-icon">🏆</div>
          <div class="card-text">积分抽奖</div>
        </div>
        <div class="float-card card-8">
          <div class="card-icon">📅</div>
          <div class="card-text">计划打卡</div>
        </div>
      </div>
      <!-- 品牌信息 -->
      <div class="brand-section">
        <h1 class="brand-title">Code Nest</h1>
        <p class="brand-subtitle">加入我们，开启编程之旅</p>
        <p class="brand-desc">刷题 · 面试 · 知识图谱 · 代码工坊 · 简历 · 社区</p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="register-right">
      <div class="register-card">
        <div class="register-header">
          <h2>创建账户</h2>
          <p>注册 Code Nest 开始你的学习之旅</p>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <div class="form-row">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名"
                size="large"
                :prefix-icon="User"
                @blur="checkUsername"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱"
                size="large"
                :prefix-icon="Message"
                @blur="checkEmail"
              />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                size="large"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="realName">
              <el-input
                v-model="registerForm.realName"
                placeholder="姓名（可选）"
                size="large"
                :prefix-icon="UserFilled"
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号（可选）"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>
          </div>

          <!-- 验证码 -->
          <el-form-item prop="captcha" v-if="captchaImage">
            <div class="captcha-row">
              <el-input
                v-model="registerForm.captcha"
                placeholder="请输入验证码"
                size="large"
                class="captcha-input"
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
              注 册
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
  display: flex;
  background: #0f0f1a;
}

/* ========== 左侧视觉区 ========== */
.register-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px;
  overflow: hidden;
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
  background: rgba(159, 122, 234, 0.6);
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
.particle:nth-child(19) { left: 5%; animation-delay: 9s; }
.particle:nth-child(20) { left: 12%; animation-delay: 9.5s; }

@keyframes floatParticle {
  0% {
    transform: translateY(100vh) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) scale(1);
    opacity: 0;
  }
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
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  transform-style: preserve-3d;
  animation: float3d 6s ease-in-out infinite;
}

.float-card .card-icon {
  font-size: 28px;
}

.float-card .card-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 500;
}

.card-1 {
  top: 8%;
  left: 8%;
  animation-delay: 0s;
}

.card-2 {
  top: 8%;
  right: 12%;
  animation-delay: -1s;
}

.card-3 {
  top: 35%;
  left: 5%;
  animation-delay: -2s;
}

.card-4 {
  top: 38%;
  right: 8%;
  animation-delay: -3s;
}

.card-5 {
  bottom: 35%;
  left: 12%;
  animation-delay: -4s;
}

.card-6 {
  bottom: 32%;
  right: 5%;
  animation-delay: -5s;
}

.card-7 {
  bottom: 8%;
  left: 6%;
  animation-delay: -6s;
}

.card-8 {
  bottom: 10%;
  right: 10%;
  animation-delay: -7s;
}

@keyframes float3d {
  0%, 100% {
    transform: translateY(0) rotateX(0deg) rotateY(0deg);
  }
  25% {
    transform: translateY(-15px) rotateX(5deg) rotateY(5deg);
  }
  50% {
    transform: translateY(-5px) rotateX(0deg) rotateY(-5deg);
  }
  75% {
    transform: translateY(-20px) rotateX(-5deg) rotateY(3deg);
  }
}

/* 品牌信息 */
.brand-section {
  position: relative;
  z-index: 10;
  text-align: center;
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  background: linear-gradient(135deg, #9f7aea 0%, #ed64a6 50%, #f687b3 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  text-shadow: 0 0 40px rgba(159, 122, 234, 0.3);
}

.brand-subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12px;
  font-weight: 500;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px;
}

/* 波浪背景 */
.wave-bg {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 200px;
}

.wave1 {
  animation: waveMove 12s linear infinite;
}

.wave2 {
  animation: waveMove 8s linear infinite reverse;
  bottom: 10px;
}

@keyframes waveMove {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* ========== 右侧表单区 ========== */
.register-right {
  width: 580px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8fafc;
  position: relative;
  overflow: hidden;
}

/* 右侧波浪装饰 */
.register-right::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at 30% 20%, rgba(159, 122, 234, 0.05) 0%, transparent 50%);
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.register-card {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 24px;
  padding: 40px 36px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
  animation: cardEnter 0.5s ease-out;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h2 {
  color: #1a202c;
  margin-bottom: 8px;
  font-size: 26px;
  font-weight: 700;
}

.register-header p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.form-row .el-form-item {
  flex: 1;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  background: #f8fafc;
  transition: all 0.3s ease;
  padding: 4px 12px;
  box-shadow: none;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e0;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: #9f7aea;
  background: white;
  box-shadow: 0 0 0 3px rgba(159, 122, 234, 0.15);
}

.register-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #2d3748;
  height: 40px;
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
  width: 120px;
  height: 40px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.captcha-image:hover {
  border-color: #9f7aea;
  transform: scale(1.02);
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #9f7aea 0%, #ed64a6 100%);
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(159, 122, 234, 0.35);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(159, 122, 234, 0.45);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.register-footer p {
  color: #718096;
  margin: 0;
  font-size: 14px;
}

.login-link {
  color: #9f7aea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #805ad5;
}

/* ========== 响应式 ========== */
@media (max-width: 1024px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .register-right {
    padding: 20px;
  }
  
  .register-card {
    padding: 32px 24px;
  }
  
  .register-header h2 {
    font-size: 22px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .captcha-row {
    flex-direction: column;
  }
  
  .captcha-image {
    width: 100%;
  }
}
</style>
