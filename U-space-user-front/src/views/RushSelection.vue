<template>
  <div class="rush-selection-page">
    <el-card class="feature-card" @click="goDormSelection" shadow="hover">
      <el-icon class="icon"><House /></el-icon>
      <span>抢宿舍</span>
    </el-card>
    <el-card class="feature-card" @click="goCourseSelection" shadow="hover">
      <el-icon class="icon"><Reading /></el-icon>
      <span>抢课程</span>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House, Reading } from '@element-plus/icons-vue'

const router = useRouter()
const goDormSelection = () => {
  router.push('/dorm-selection')
}
const goCourseSelection = () => {
  router.push('/course-selection')
}
</script>

<style scoped>
.rush-selection-page {
  display: flex;
  flex-wrap: wrap;
  padding: 16px;
  gap: 16px;
  padding-bottom: 72px; /* 避免底部导航遮挡 */
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
  background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
}
.feature-card:nth-child(2) {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%);
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