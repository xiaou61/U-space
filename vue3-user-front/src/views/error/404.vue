<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <div class="error-number">404</div>
      </div>
      
      <div class="error-info">
        <h2>页面不存在</h2>
        <p>抱歉，您访问的页面不存在或已被删除</p>
        
        <div class="error-actions">
          <el-button type="primary" @click="goHome" :icon="House">
            返回首页
          </el-button>
          <el-button @click="goBack" :icon="ArrowLeft">
            返回上页
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/')
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.error-container {
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
      radial-gradient(circle at 30% 40%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 70% 60%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
    animation: float 10s ease-in-out infinite;
  }
}

.error-content {
  text-align: center;
  color: white;
  position: relative;
  z-index: 1;
  animation: slide-up 1s ease-out;
  
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

.error-image {
  margin-bottom: 50px;
  position: relative;
}

.error-number {
  font-size: 140px;
  font-weight: 900;
  text-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  margin-bottom: 20px;
  background: linear-gradient(45deg, #fff, #e3f2fd, #fff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -5px;
  position: relative;
  animation: float 3s ease-in-out infinite;
  
  &::before {
    content: '404';
    position: absolute;
    top: 4px;
    left: 4px;
    color: rgba(0, 0, 0, 0.1);
    z-index: -1;
    -webkit-text-fill-color: rgba(0, 0, 0, 0.1);
  }
}

.error-info h2 {
  font-size: 36px;
  margin-bottom: 20px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  font-weight: 700;
  letter-spacing: -0.5px;
}

.error-info p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 50px;
  max-width: 450px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.error-actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

.error-actions .el-button {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
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
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.5);
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
    
    &::before {
      left: 100%;
    }
  }
  
  &:active {
    transform: translateY(-1px);
  }
}

.error-actions .el-button--primary {
  background: rgba(255, 255, 255, 0.95);
  color: #74b9ff;
  border-color: rgba(255, 255, 255, 0.8);
  
  &:hover {
    background: white;
    color: #0984e3;
    box-shadow: 0 12px 32px rgba(255, 255, 255, 0.3);
  }
}

/* 添加一些装饰性动画元素 */
.error-content::before,
.error-content::after {
  content: '';
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  pointer-events: none;
}

.error-content::before {
  top: 20%;
  left: 10%;
  animation: pulse 3s ease-in-out infinite;
}

.error-content::after {
  bottom: 20%;
  right: 10%;
  animation: pulse 3s ease-in-out infinite 1.5s;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.1;
  }
}

@media (max-width: 768px) {
  .error-number {
    font-size: 100px;
    letter-spacing: -3px;
  }
  
  .error-info h2 {
    font-size: 28px;
  }
  
  .error-info p {
    font-size: 16px;
    margin-bottom: 40px;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
  
  .error-actions .el-button {
    width: 240px;
    padding: 14px 24px;
  }
}

@media (max-width: 480px) {
  .error-number {
    font-size: 80px;
  }
  
  .error-info h2 {
    font-size: 24px;
  }
  
  .error-actions .el-button {
    width: 200px;
    padding: 12px 20px;
    font-size: 14px;
  }
}
</style> 