<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo">
          <h2>Code Nest</h2>
        </div>
        <div class="user-actions">
          <el-button @click="handleLogout" :icon="SwitchButton">
            退出登录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <div class="welcome-section">
        <h1 class="welcome-title">欢迎来到 Code Nest</h1>
        <p class="welcome-subtitle">您的代码管理平台</p>
        <div class="user-info" v-if="userStore.userInfo">
          <p>欢迎您，{{ userStore.userInfo.username }}</p>
          <p class="user-email">{{ userStore.userInfo.email }}</p>
        </div>
      </div>

      <!-- 功能按钮区域 -->
      <div class="features-section">
        <div class="features-grid">
          <div class="feature-card" @click="goToInterview">
            <div class="feature-icon">
              <el-icon size="48"><Document /></el-icon>
            </div>
            <h3>面试题库</h3>
            <p>精选面试题，助力求职成功</p>
          </div>

          <div class="feature-card" @click="goToCommunity">
            <div class="feature-icon">
              <el-icon size="48"><ChatDotRound /></el-icon>
            </div>
            <h3>技术社区</h3>
            <p>分享经验，交流技术，共同成长</p>
          </div>

          <div class="feature-card" @click="goToMoments">
            <div class="feature-icon">
              <el-icon size="48"><Picture /></el-icon>
            </div>
            <h3>朋友圈</h3>
            <p>分享动态，记录美好时光</p>
          </div>

          <div class="feature-card" @click="goToNotification">
            <div class="feature-icon">
              <el-icon size="48"><Bell /></el-icon>
            </div>
            <h3>消息中心</h3>
            <p>查看系统通知和个人消息</p>
          </div>

          <div class="feature-card" @click="goToProfile">
            <div class="feature-icon">
              <el-icon size="48"><User /></el-icon>
            </div>
            <h3>个人中心</h3>
            <p>管理个人信息和设置</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, SwitchButton, Document, ChatDotRound, Bell, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 获取用户信息
const loadUserInfo = async () => {
  if (!userStore.userInfo) {
    try {
      const userInfo = await userApi.getCurrentUserInfo()
      userStore.setUserInfo(userInfo)
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
}

// 跳转到面试题库
const goToInterview = () => {
  router.push('/interview')
}

// 跳转到技术社区
const goToCommunity = () => {
  router.push('/community')
}

// 跳转到朋友圈
const goToMoments = () => {
  router.push('/moments')
}

// 跳转到消息中心
const goToNotification = () => {
  router.push('/notification')
}

// 跳转到个人中心
const goToProfile = () => {
  router.push('/profile')
}





// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #74b9ff 0%, #0984e3 100%);
  position: relative;
  overflow: hidden;
}

.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.header {
  position: relative;
  z-index: 10;
  padding: 20px 40px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.logo h2 {
  margin: 0;
  color: white;
  font-size: 28px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.main-content {
  position: relative;
  z-index: 10;
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  color: white;
  margin-bottom: 60px;
}

.welcome-title {
  font-size: 48px;
  font-weight: bold;
  margin: 0 0 16px 0;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 1s ease-out;
}

.welcome-subtitle {
  font-size: 20px;
  margin: 0 0 30px 0;
  opacity: 0.9;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 1s ease-out 0.2s both;
}

.user-info {
  animation: fadeInUp 1s ease-out 0.4s both;
}

.user-info p {
  margin: 8px 0;
  font-size: 16px;
  opacity: 0.9;
}

.user-email {
  font-size: 14px !important;
  opacity: 0.7 !important;
}

.features-section {
  animation: fadeInUp 1s ease-out 0.6s both;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  justify-content: center;
}

.feature-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 1);
}

.feature-icon {
  margin-bottom: 20px;
  color: #409eff;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1);
  color: #0984e3;
}

.feature-card h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.feature-card p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 15px 20px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .main-content {
    padding: 20px;
  }

  .welcome-title {
    font-size: 36px;
  }

  .welcome-subtitle {
    font-size: 18px;
  }

  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .feature-card {
    padding: 30px 20px;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 28px;
  }

  .welcome-subtitle {
    font-size: 16px;
  }

  .logo h2 {
    font-size: 24px;
  }
}
</style> 