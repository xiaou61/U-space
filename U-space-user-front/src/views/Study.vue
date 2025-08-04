<template>
  <div class="study-page">
    <el-card class="feature-card" @click="goMyClasses" shadow="hover">
      <el-icon class="icon"><Reading /></el-icon>
      <span>我的班级</span>
    </el-card>
    <el-card class="feature-card" @click="goAI" shadow="hover">
      <el-icon class="icon"><ChatLineSquare /></el-icon>
      <span>AI 助手</span>
    </el-card>
    <el-card class="feature-card" @click="goWordLearning" shadow="hover">
      <el-icon class="icon"><Document /></el-icon>
      <span>单词学习</span>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Reading, ChatLineSquare, Document } from '@element-plus/icons-vue'

const router = useRouter()
const goMyClasses = () => {
  router.push('/my-classes')
}
const goAI = () => {
  router.push('/ai-assistant')
}
const goWordLearning = () => {
  router.push('/word-learning')
}
</script>

<style scoped>
.study-page {
  display: flex;
  flex-wrap: wrap;
  padding: 16px;
  gap: 16px;
}
.feature-card {
  flex: 1 1 40%;
  min-width: 140px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 16px;
  color: #fff;
  transition: transform 0.25s, box-shadow 0.25s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
.feature-card:nth-child(1) {
  background: linear-gradient(135deg, #4d9eff 0%, #5ac8fa 100%);
}
.feature-card:nth-child(2) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}
.feature-card:nth-child(3) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.icon {
  font-size: 2.4rem;
  margin-bottom: 8px;
}
.feature-card span {
  font-size: 1rem;
  font-weight: 600;
}
</style> 