<template>
  <div class="profile-container">
    <h1 style="margin-bottom: 24px;">个人中心</h1>
    
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <div>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="showEditProfile = true"
                  icon="Edit"
                >
                  编辑资料
                </el-button>
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="showChangePassword = true"
                  icon="Key"
                  style="margin-left: 8px;"
                >
                  修改密码
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="user-card">
            <div class="avatar-section">
              <el-avatar :size="80" :src="userStore.avatar">
                {{ userStore.realName.charAt(0) || userStore.username.charAt(0) }}
              </el-avatar>
              
              <div class="user-info">
                <h3>{{ userStore.realName || userStore.username }}</h3>
                <p class="username">@{{ userStore.username }}</p>
                <p class="roles" v-if="currentRoles">{{ currentRoles }}</p>
              </div>
            </div>
            
            <el-divider />
            
            <div class="stats">
              <div class="stat-item" v-if="userStore.userInfo?.lastLoginTime">
                <div class="stat-value">{{ formatDate(userStore.userInfo.lastLoginTime) }}</div>
                <div class="stat-label">上次登录</div>
              </div>
              
              <div class="stat-item" v-if="userStore.userInfo?.email">
                <div class="stat-value">{{ userStore.userInfo.email }}</div>
                <div class="stat-label">邮箱地址</div>
              </div>
              
              <div class="stat-item" v-if="userStore.userInfo?.phone">
                <div class="stat-value">{{ userStore.userInfo.phone }}</div>
                <div class="stat-label">手机号码</div>
              </div>
              
              <div class="stat-item" v-if="genderText">
                <div class="stat-value">{{ genderText }}</div>
                <div class="stat-label">性别</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 详细信息 -->
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>详细信息</span>
          </template>
          
          <el-descriptions :column="2" border v-if="userStore.userInfo">
            <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ userStore.userInfo.realName || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userStore.userInfo.email || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userStore.userInfo.phone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ genderText || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="上次登录">{{ formatDateTime(userStore.userInfo.lastLoginTime) || '无记录' }}</el-descriptions-item>
            <el-descriptions-item label="个人简介" :span="2">{{ userStore.userInfo.remark || '暂无简介' }}</el-descriptions-item>
            <el-descriptions-item label="角色权限" :span="2">
              <div v-if="userStore.roles.length > 0">
                <el-tag v-for="role in userStore.roles" :key="role" style="margin-right: 8px;">
                  {{ role }}
                </el-tag>
              </div>
              <span v-else>暂无角色</span>
            </el-descriptions-item>
            <el-descriptions-item label="权限列表" :span="2">
              <div v-if="userStore.permissions.length > 0">
                <el-tag 
                  v-for="permission in userStore.permissions" 
                  :key="permission" 
                  type="info" 
                  style="margin-right: 8px; margin-bottom: 4px;"
                >
                  {{ permission }}
                </el-tag>
              </div>
              <span v-else>暂无权限</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑个人信息对话框 -->
    <EditProfile 
      v-model="showEditProfile" 
      :user-info="userStore.userInfo"
      @success="onProfileUpdated"
    />
    
    <!-- 修改密码对话框 -->
    <ChangePassword 
      v-model="showChangePassword"
      @success="onPasswordChanged"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import EditProfile from './EditProfile.vue'
import ChangePassword from './ChangePassword.vue'

const userStore = useUserStore()

// 对话框显示状态
const showEditProfile = ref(false)
const showChangePassword = ref(false)

// 当前角色
const currentRoles = computed(() => {
  if (userStore.roles.length === 0) return ''
  if (Array.isArray(userStore.roles) && userStore.roles.length > 0) {
    // 如果是对象数组
    if (typeof userStore.roles[0] === 'object') {
      return userStore.roles.map(role => role.roleName).join('、')
    }
    // 如果是字符串数组
    return userStore.roles.join('、')
  }
  return ''
})

// 性别文本
const genderText = computed(() => {
  const gender = userStore.userInfo?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  if (gender === 0) return '未知'
  return ''
})

// 格式化日期
const formatDate = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleDateString()
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '暂无'
  return new Date(dateTime).toLocaleString()
}

// 个人信息更新成功回调
const onProfileUpdated = () => {
  ElMessage.success('个人信息已更新')
}

// 密码修改成功回调
const onPasswordChanged = () => {
  ElMessage.success('密码修改成功，即将重新登录')
}
</script>

<style scoped>
.user-card {
  text-align: center;
}

.avatar-section {
  margin-bottom: 20px;
}

.user-info {
  margin-top: 16px;
}

.user-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.username {
  margin: 0 0 8px 0;
  color: #909399;
  font-size: 14px;
}

.roles {
  margin: 0;
  color: #409eff;
  font-size: 14px;
}

.stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-value {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  word-break: break-all;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 