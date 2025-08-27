<template>
  <div class="activities-page">
    <el-card class="feature-card" @click="goVoteActivities" shadow="hover">
      <el-icon class="icon"><Trophy /></el-icon>
      <span>投票活动</span>
    </el-card>
    <!-- 未来可以添加更多活动类型 -->
    <!-- <el-card class="feature-card" @click="goOtherActivity" shadow="hover">
      <el-icon class="icon"><Calendar /></el-icon>
      <span>其他活动</span>
    </el-card> -->
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Trophy } from '@element-plus/icons-vue'

const router = useRouter()

const goVoteActivities = () => {
  router.push('/vote-activities')
}
</script>

<style scoped>
.activities-page {
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
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
}

.feature-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
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