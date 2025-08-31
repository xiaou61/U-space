<template>
  <div class="dashboard">
    <h1 style="margin-bottom: 24px;">仪表板</h1>
    
    <!-- 欢迎信息 -->
    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>欢迎回来</span>
          </template>
          
          <div style="padding: 20px 0;">
            <h2>欢迎回来，{{ userStore.realName || userStore.username }}！</h2>
            <p style="margin-top: 10px; color: #666;">
              今天是 {{ currentDate }}，祝您工作愉快！
            </p>
            
            <div style="margin-top: 20px;" v-if="userStore.roles.length > 0">
              <el-tag type="success" style="margin-right: 10px;">
                当前角色：{{ currentRoles }}
              </el-tag>
              <el-tag type="info" v-if="userStore.userInfo?.lastLoginTime">
                上次登录：{{ userStore.userInfo.lastLoginTime }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          
          <div class="quick-actions">
            <el-button
              type="primary"
              @click="$router.push('/login-logs')"
              style="margin-bottom: 10px; width: 100%;"
            >
              <el-icon><Document /></el-icon>
              登录日志
            </el-button>
            
            <el-button
              type="success"
              @click="$router.push('/profile')"
              style="width: 100%;"
            >
              <el-icon><User /></el-icon>
              个人中心
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 当前角色
const currentRoles = computed(() => {
  if (userStore.roles.length === 0) return '暂无角色'
  return userStore.roles.map(role => role.roleName).join('、')
})
</script>

<style scoped>
.quick-actions {
  display: flex;
  flex-direction: column;
}
</style> 