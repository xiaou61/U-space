<template>
  <div class="home-container">
    <h2 class="welcome">欢迎来到后台管理系统</h2>
  </div>
</template>

<style scoped>
.home-container {
  padding: 40px;
}

.welcome {
  font-size: 28px;
  font-weight: 600;
  color: #409eff;
  animation: slideIn 0.8s ease;
}

@keyframes slideIn {
  0% { opacity: 0; transform: translateY(-20px); }
  100% { opacity: 1; transform: translateY(0); }
}
</style>