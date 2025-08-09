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
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/school" v-if="isAdmin">
          <template #title>
            <el-icon><User /></el-icon>
            <span>学校教学管理</span>
          </template>
          <el-menu-item index="/class">班级管理</el-menu-item>
          <el-menu-item index="/teacher">教师管理</el-menu-item>
          <el-sub-menu index="/student">
            <template #title>学生信息管理</template>
            <el-menu-item index="/student/unaudited">未审批学生</el-menu-item>
            <el-menu-item index="/student/all">全部学生</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>
        <el-sub-menu index="/system" v-if="isAdmin">
          <template #title>
            <el-icon><Notebook /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/operlog">操作日志</el-menu-item>
          <el-menu-item index="/file">文件管理</el-menu-item>
          <el-menu-item index="/online">在线用户</el-menu-item>
          <el-menu-item index="/announcement">公告管理</el-menu-item>
          <el-menu-item index="/schoolinfo">学校信息管理</el-menu-item>
          <el-menu-item index="/bbs">BBS管理员</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/dorm" v-if="isAdmin">
          <template #title>
            <el-icon><User /></el-icon>
            <span>宿舍信息</span>
          </template>
          <el-menu-item index="/dorm/building">宿舍楼管理</el-menu-item>
          <el-menu-item index="/dorm/room">宿舍房间管理</el-menu-item>
          <el-menu-item index="/dorm/bed">床位管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/group" v-if="isTeacher">
          <el-icon><User /></el-icon>
          <span>群组管理</span>
        </el-menu-item>
        <el-menu-item index="/signin" v-if="isTeacher">
          <el-icon><Notebook /></el-icon>
          <span>签到管理</span>
        </el-menu-item>
        <el-menu-item index="/homework" v-if="isTeacher">
          <el-icon><Notebook /></el-icon>
          <span>作业管理</span>
        </el-menu-item>
        <el-menu-item index="/material" v-if="isTeacher">
          <el-icon><Notebook /></el-icon>
          <span>资料管理</span>
        </el-menu-item>

        <el-sub-menu index="/course" v-if="isAdmin">
          <template #title>
            <el-icon><Notebook /></el-icon>
            <span>课程相关</span>
          </template>
          <el-menu-item index="/course/management">课程管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/ai" v-if="isAdmin">
          <el-icon><Notebook /></el-icon>
          <span>AI对话管理</span>
        </el-menu-item>
        <el-menu-item index="/word" v-if="isAdmin">
          <el-icon><Notebook /></el-icon>
          <span>单词管理</span>
        </el-menu-item>
        <el-menu-item index="/video" v-if="isAdmin">
          <el-icon><VideoCamera /></el-icon>
          <span>入学必看视频管理</span>
        </el-menu-item>
        <el-menu-item index="/bbs-category" v-if="isBbsAdmin">校园论坛分类管理</el-menu-item>
        <el-menu-item index="/bbs-sensitive" v-if="isBbsAdmin">校园论坛敏感词</el-menu-item>
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
import { getInfo, logout as logoutApi, updatePassword, getRole } from '../api/auth'
import { ElMessage } from 'element-plus'
import { House, Notebook, User, Moon, VideoCamera } from '@element-plus/icons-vue'
import { toggleTheme } from '../utils/theme'

const router = useRouter()
const route = useRoute()
const username = ref('')
const activeMenu = ref(route.path)

watch(() => route.path, (val) => { activeMenu.value = val })

// 密码修改对话框
const pwdDialog = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const roles = ref([])
const isAdmin = computed(() => roles.value.includes('admin'))
const isTeacher = computed(() => roles.value.includes('teacher'))
const isBbsAdmin = computed(() => roles.value.includes('bbs_admin'))

onMounted(async () => {
  try {
    const res = await getInfo()
    username.value = res.data.username || res.data.name || '用户'
  } catch (e) {}

  try {
    const roleRes = await getRole()
    // 后端把角色字符串放在 msg 字段
    const str = roleRes.data || roleRes.msg || ''
    roles.value = str.replace(/\[|\]/g, '').split(',').map(r => r.trim()).filter(Boolean)
  } catch (e) {}
})

const handleMenuSelect = (index) => {
  if (index !== route.path) {
    router.push(index)
  }
}

const handleLogout = async () => {
  try { await logoutApi() } catch (e) {}
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
  } catch (e) {}
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