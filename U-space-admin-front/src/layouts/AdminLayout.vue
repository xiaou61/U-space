<template>
  <el-container class="admin-layout">
    <el-aside width="200px" class="aside">
      <el-menu
        :default-active="activeMenu"
        @select="handleMenuSelect"
        class="menu"
        background-color="transparent"
        text-color="#cfd3dc"
        active-text-color="#fff"
      >
        <!-- 动态渲染菜单 -->
        <template v-for="menu in filteredMenus" :key="menu.key">
          <!-- 有子菜单的项目 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="`/${menu.key}`">
            <template #title>
              <el-icon>
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.title }}</span>
            </template>
            
            <template v-for="subMenu in menu.children" :key="subMenu.key">
              <!-- 二级子菜单 -->
              <el-sub-menu v-if="subMenu.children && subMenu.children.length > 0" :index="subMenu.key">
                <template #title>{{ subMenu.title }}</template>
                <el-menu-item 
                  v-for="subSubMenu in subMenu.children" 
                  :key="subSubMenu.key"
                  :index="subSubMenu.path"
                >
                  {{ subSubMenu.title }}
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 一级子菜单 -->
              <el-menu-item v-else :index="subMenu.path">
                {{ subMenu.title }}
              </el-menu-item>
            </template>
          </el-sub-menu>
          
          <!-- 无子菜单的项目 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon>
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button size="small" @click="toggleTheme">
            <el-icon><Moon /></el-icon>
          </el-button>
          <el-button size="small" @click="openPwdDialog">修改密码</el-button>
          <el-button type="primary" size="small" @click="handleLogout" style="margin-left:8px">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-dialog title="修改密码" v-model="pwdDialog" width="400px">
    <el-form label-width="100px">
      <el-form-item label="旧密码">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="pwdForm.confirm" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdDialog=false">取消</el-button>
      <el-button type="primary" @click="submitPwd">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getInfo, logout as logoutApi, updatePassword, getRole, getUserPermissions } from '../api/auth'
import { ElMessage } from 'element-plus'
import { Moon } from '@element-plus/icons-vue'
import { toggleTheme } from '../utils/theme'
import { menuConfig, filterMenuByPermissions, getDefaultPermissionsByRole } from '../config/menu'

const router = useRouter()
const route = useRoute()
const username = ref('')
const activeMenu = ref(route.path)

watch(() => route.path, (val) => { activeMenu.value = val })

// 密码修改对话框
const pwdDialog = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

// 用户角色和权限
const roles = ref([])
const userPermissions = ref([])

// 计算过滤后的菜单
const filteredMenus = computed(() => {
  return filterMenuByPermissions(menuConfig, userPermissions.value)
})

onMounted(async () => {
  try {
    // 获取用户信息
    const res = await getInfo()
    username.value = res.data.username || res.data.name || '用户'
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }

  try {
    // 获取用户角色
    const roleRes = await getRole()
    const roleStr = roleRes.data || roleRes.msg || ''
    roles.value = roleStr.replace(/\[|\]/g, '').split(',').map(r => r.trim()).filter(Boolean)
    
    console.log('用户角色:', roles.value)
  } catch (e) {
    console.error('获取用户角色失败:', e)
    roles.value = []
  }

  try {
    // 从后端获取用户实际权限
    const permRes = await getUserPermissions()
    userPermissions.value = permRes.data || []
    
    console.log('从后端获取的用户权限:', userPermissions.value)
    console.log('权限过滤前的菜单数量:', menuConfig.length)
    console.log('权限过滤后的菜单:', filterMenuByPermissions(menuConfig, userPermissions.value))
  } catch (e) {
    console.error('获取用户权限失败:', e)
    // 如果获取权限失败，使用角色默认权限作为fallback
    userPermissions.value = getDefaultPermissionsByRole(roles.value)
    console.log('使用默认权限:', userPermissions.value)
  }
})

const handleMenuSelect = (index) => {
  if (index !== route.path) {
    router.push(index)
  }
}

const handleLogout = async () => {
  try { 
    await logoutApi() 
  } catch (e) {
    console.error('退出登录失败:', e)
  }
  localStorage.removeItem('tokenName')
  localStorage.removeItem('tokenValue')
  ElMessage.success('已退出登录')
  router.push('/login')
}

const openPwdDialog = () => {
  Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirm: '' })
  pwdDialog.value = true
}

const submitPwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请输入完整信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  try {
    await updatePassword(pwdForm.oldPassword, pwdForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    handleLogout()
  } catch (e) {
    console.error('修改密码失败:', e)
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.aside {
  background: var(--aside-bg);
  color: var(--text-color);
}
.menu {
  border-right: none;
}
.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  background: var(--bg-color);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  color: var(--text-color);
}
.header-right {
  display: flex;
  align-items: center;
}
.username {
  margin-right: 12px;
}
.main {
  padding: 24px;
  background: var(--bg-color);
  color: var(--text-color);
  overflow: auto;
}
.el-menu-item {
  height: 48px !important;
  line-height: 48px !important;
}
.el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.15) !important;
}
.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
}
</style> 