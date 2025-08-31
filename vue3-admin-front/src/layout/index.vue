<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarWidth" class="sidebar">
        <!-- Logo -->
        <div class="logo">
          <span v-if="!collapsed">Code-Nest</span>
          <span v-else>CN</span>
        </div>
        
        <!-- 导航菜单 -->
        <el-menu
          :default-active="currentRoute"
          :collapse="collapsed"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          
          <el-menu-item index="/user">
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-sub-menu index="/logs">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>日志管理</span>
            </template>
            <el-menu-item index="/logs/login">
              <el-icon><UserFilled /></el-icon>
              <span>登录日志</span>
            </el-menu-item>
            <el-menu-item index="/logs/operation">
              <el-icon><Operation /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 头部 -->
        <el-header height="60px" class="header">
          <div class="header-left">
            <!-- 折叠按钮 -->
            <el-button
              type="text"
              @click="toggleSidebar"
              style="font-size: 18px; margin-right: 20px;"
            >
              <el-icon>
                <Expand v-if="collapsed" />
                <Fold v-else />
              </el-icon>
            </el-button>
            
            <!-- 面包屑 -->
            <span style="font-size: 16px; color: #333;">{{ currentTitle }}</span>
          </div>
          
          <div class="header-right">
            <!-- 用户信息 -->
            <div class="user-info">
              <!-- 用户头像 -->
              <el-avatar
                :size="32"
                :src="userStore.avatar"
                style="margin-right: 8px;"
              >
                {{ userStore.realName.charAt(0) || userStore.username.charAt(0) }}
              </el-avatar>
              
              <!-- 用户下拉菜单 -->
              <el-dropdown @command="handleUserCommand">
                <span class="username">
                  {{ userStore.realName || userStore.username }}
                  <el-icon><CaretBottom /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>
                      个人中心
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>
                      <el-icon><SwitchButton /></el-icon>
                      退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        
        <!-- 主内容区域 -->
        <el-main class="main-content">
          <div class="page-container">
            <router-view />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  Odometer, 
  Document, 
  UserFilled, 
  Operation, 
  User, 
  Avatar,
  Expand, 
  Fold, 
  CaretBottom, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const collapsed = ref(false)

// 侧边栏宽度
const sidebarWidth = computed(() => collapsed.value ? '64px' : '200px')

// 当前路由
const currentRoute = computed(() => route.path)

// 当前页面标题
const currentTitle = computed(() => route.meta?.title || '仪表板')

// 切换侧边栏
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

// 处理用户下拉菜单命令
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        
        await userStore.logout()
        router.push('/login')
      } catch (error) {
        console.log('取消退出:', error)
      }
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  transition: width 0.2s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #1e3a8a;
}

.header {
  background-color: white;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.username:hover {
  background-color: #f5f5f5;
}

.sidebar-menu {
  border: none;
  background-color: #001529;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: #1890ff !important;
  color: white;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: white;
}

.sidebar-menu :deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.8);
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: #1890ff !important;
  color: white;
}

.main-content {
  background-color: #f0f2f5;
  padding: 0;
}

.page-container {
  height: 100%;
  overflow: auto;
}
</style> 