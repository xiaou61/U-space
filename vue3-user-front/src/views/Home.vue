<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo">
          <h2>Code Nest</h2>
        </div>
        <div class="nav-menu">
          <el-button @click="goToInterview" type="success" :icon="Document">
            面试题库
          </el-button>
          <el-button @click="goToProfile" type="primary" :icon="User">
            个人中心
          </el-button>
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
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, SwitchButton, Document } from '@element-plus/icons-vue'

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
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%),
      radial-gradient(circle at 40% 70%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
    animation: float 12s ease-in-out infinite;
  }
}

.header {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 0 20px;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
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

.nav-menu .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-weight: 600;
  padding: 12px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.5);
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.main-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 70px);
  padding: 60px 20px;
  position: relative;
  z-index: 1;
}

.welcome-section {
  text-align: center;
  color: white;
  position: relative;
  animation: slide-up 1s ease-out;
  
  @keyframes slide-up {
    from {
      opacity: 0;
      transform: translateY(50px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

.welcome-title {
  font-size: 56px;
  font-weight: 800;
  margin-bottom: 20px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  letter-spacing: -1px;
  line-height: 1.1;
  background: linear-gradient(45deg, #fff, #e3f2fd, #fff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: float 3s ease-in-out infinite;
  
  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.welcome-subtitle {
  font-size: 22px;
  margin-bottom: 50px;
  opacity: 0.9;
  font-weight: 400;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.5px;
  
  @media (max-width: 768px) {
    font-size: 18px;
    margin-bottom: 40px;
  }
}

.user-info {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px;
  margin-top: 50px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  max-width: 450px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.2),
    0 8px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  }
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 
      0 25px 50px rgba(0, 0, 0, 0.25),
      0 12px 24px rgba(0, 0, 0, 0.15);
    border-color: rgba(255, 255, 255, 0.4);
  }
}

.user-info p {
  margin: 12px 0;
  font-size: 16px;
  font-weight: 500;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  
  &:first-child {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 16px;
  }
}

.user-email {
  opacity: 0.85;
  font-size: 14px !important;
  font-weight: 400 !important;
}

/* 添加一些装饰元素 */
.welcome-section::before,
.welcome-section::after {
  content: '';
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  pointer-events: none;
}

.welcome-section::before {
  top: -50px;
  left: -50px;
  animation: pulse 4s ease-in-out infinite;
}

.welcome-section::after {
  bottom: -50px;
  right: -50px;
  animation: pulse 4s ease-in-out infinite 2s;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.1;
  }
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
  
  .nav-menu .el-button {
    padding: 8px 12px;
    font-size: 14px;
  }
  
  .main-content {
    padding: 40px 16px;
    min-height: calc(100vh - 60px);
  }
  
  .user-info {
    padding: 24px;
    margin-top: 40px;
  }
}
</style> 