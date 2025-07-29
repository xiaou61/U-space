<template>
  <nav class="bottom-nav">
    <!-- 单个首页导航，后续可追加 -->
    <div
      v-for="item in navs"
      :key="item.path"
      class="nav-item"
      :class="{ active: isActive(item.path) }"
      @click="go(item.path)"
    >
      <el-icon><component :is="item.icon" /></el-icon>
      <span>{{ item.label }}</span>
    </div>
  </nav>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, UserFilled, Reading, ChatDotRound, Menu } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const navs = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/study', label: '学习', icon: Reading },
  { path: '/rush-selection', label: '抢选', icon: Menu },
  { path: '/forum', label: '论坛', icon: ChatDotRound },
  { path: '/profile', label: '我的', icon: UserFilled },
]

const isActive = (path) => route.path === path
const go = (path) => {
  if (route.path !== path) router.push(path)
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  display: flex;
  background: #ffffff;
  border-top: 1px solid #e0e0e0;
  z-index: 999;
}
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 0.75rem;
}
.nav-item .el-icon {
  font-size: 1.3rem;
}
.nav-item.active {
  color: #409eff;
}
.nav-item.active .el-icon {
  color: #409eff;
}
</style> 