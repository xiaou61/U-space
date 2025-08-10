<template>
  <div class="admin-user-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>管理员用户管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>

      <!-- 用户列表 -->
      <el-table :data="userList" stripe style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="280" />
        <el-table-column prop="username" label="用户名" width="200" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="warning" @click="resetUserPassword(scope.row)">
              重置密码
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteUser(scope.row)"
              :disabled="scope.row.username === 'admin'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      :title="isEdit ? '编辑用户' : '添加用户'"
      v-model="dialogVisible" 
      width="500px"
    >
      <el-form :model="userForm" :rules="userRules" ref="userFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input 
            v-model="userForm.password" 
            type="password" 
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog 
      title="重置密码"
      v-model="passwordDialogVisible" 
      width="400px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getAdminUserList, 
  addAdminUser, 
  updateAdminUser, 
  deleteAdminUser, 
  resetPassword 
} from '../api/adminUser'

// 响应式数据
const userList = ref([])
const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref({})

// 表单数据
const userForm = reactive({
  id: '',
  username: '',
  password: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 表单引用
const userFormRef = ref()
const passwordFormRef = ref()

// 页面加载时获取用户列表
onMounted(() => {
  loadUserList()
})

// 加载用户列表
const loadUserList = async () => {
  try {
    const res = await getAdminUserList()
    userList.value = res.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  resetUserForm()
  dialogVisible.value = true
}

// 编辑用户
const editUser = (user) => {
  isEdit.value = true
  userForm.id = user.id
  userForm.username = user.username
  userForm.password = ''
  dialogVisible.value = true
}

// 重置表单
const resetUserForm = () => {
  userForm.id = ''
  userForm.username = ''
  userForm.password = ''
}

// 保存用户
const saveUser = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateAdminUser(userForm)
          ElMessage.success('更新用户成功')
        } else {
          await addAdminUser(userForm)
          ElMessage.success('添加用户成功')
        }
        dialogVisible.value = false
        loadUserList()
      } catch (error) {
        console.error('保存用户失败:', error)
        ElMessage.error('保存用户失败')
      }
    }
  })
}

// 删除用户
const deleteUser = (user) => {
  ElMessageBox.confirm(
    `确定要删除用户"${user.username}"吗？`,
    '确认删除',
    {
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAdminUser(user.id)
      ElMessage.success('删除用户成功')
      loadUserList()
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 重置用户密码
const resetUserPassword = (user) => {
  currentUser.value = user
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 保存密码
const savePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await resetPassword(currentUser.value.id, passwordForm.newPassword)
        ElMessage.success('重置密码成功')
        passwordDialogVisible.value = false
      } catch (error) {
        console.error('重置密码失败:', error)
        ElMessage.error('重置密码失败')
      }
    }
  })
}
</script>

<style scoped>
.admin-user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 