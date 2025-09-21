<template>
  <div class="home-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎来到 Code Nest</h1>
      </div>
    </div>
  </div>
</template>

<script setup>
// 简化的首页不需要复杂的逻辑
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
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

.welcome-section {
  position: relative;
  z-index: 10;
  text-align: center;
}

.welcome-content {
  animation: fadeInUp 1s ease-out;
}

.welcome-title {
  font-size: 64px;
  font-weight: bold;
  color: white;
  margin: 0;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  letter-spacing: 2px;
}

/* 动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 48px;
    letter-spacing: 1px;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 36px;
    letter-spacing: 0.5px;
  }
}
</style> 