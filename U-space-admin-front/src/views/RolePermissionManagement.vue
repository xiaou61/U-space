<template>
  <div class="role-permission-page">
    <el-card class="header-card">
      <h2>角色权限管理</h2>
      <p class="description">管理系统角色和权限分配，控制用户的菜单访问权限</p>
    </el-card>

    <el-row :gutter="24">
      <!-- 左侧：角色管理 -->
      <el-col :span="8">
        <el-card class="role-card">
          <template #header>
            <div class="card-header">
              <span>角色管理</span>
              <el-button type="primary" size="small" @click="openAddRoleDialog">
                <el-icon><Plus /></el-icon>
                添加角色
              </el-button>
            </div>
          </template>
          
          <div class="role-list">
            <div 
              v-for="role in roles" 
              :key="role.roleCode"
              class="role-item"
              :class="{ active: selectedRole?.roleCode === role.roleCode }"
              @click="selectRole(role)"
            >
              <div class="role-info">
                <el-icon class="role-icon">
                  <User v-if="role.roleCode === 'admin'" />
                  <Avatar v-else-if="role.roleCode === 'teacher'" />
                  <ChatDotRound v-else-if="role.roleCode === 'bbs_admin'" />
                  <UserFilled v-else />
                </el-icon>
                <div>
                  <div class="role-name">{{ role.roleName }}</div>
                  <div class="role-desc">{{ role.description }}</div>
                </div>
              </div>
              <div class="role-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click.stop="editRole(role)"
                  v-if="!role.isSystem"
                >
                  编辑
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click.stop="deleteRole(role)"
                  style="color: #f56c6c"
                  v-if="!role.isSystem"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：权限配置 -->
      <el-col :span="8">
        <el-card class="permission-card">
          <template #header>
            <div class="card-header">
              <span>权限配置</span>
              <el-button 
                type="success" 
                size="small" 
                @click="saveRolePermissions"
                :disabled="!selectedRole"
              >
                保存权限
              </el-button>
            </div>
          </template>

          <div v-if="selectedRole" class="permission-config">
            <div class="selected-role">
              <el-tag type="primary" size="large">{{ selectedRole.roleName }}</el-tag>
            </div>
            
            <el-tree
              ref="permissionTree"
              :data="permissionTreeData"
              :props="treeProps"
              show-checkbox
              node-key="id"
              :default-checked-keys="selectedRole.permissions || []"
              @check="onPermissionCheck"
              class="permission-tree"
              :expand-on-click-node="false"
              default-expand-all
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon class="node-icon">
                    <component :is="getIconComponent(data.menuIcon)" />
                  </el-icon>
                  <span class="node-label">{{ data.permissionName }}</span>
                </div>
              </template>
            </el-tree>
          </div>
          
                      <div v-else class="no-selection">
              <el-empty description="请先选择一个角色" :image-size="80" />
            </div>
        </el-card>
      </el-col>

      <!-- 右侧：用户角色分配 -->
      <el-col :span="8">
        <el-card class="user-role-card">
          <template #header>
            <div class="card-header">
              <span>用户角色分配</span>
              <el-button type="primary" size="small" @click="openUserRoleDialog">
                <el-icon><Setting /></el-icon>
                分配角色
              </el-button>
            </div>
          </template>

          <div class="user-search">
            <el-input
              v-model="userSearchQuery"
              placeholder="搜索用户..."
              @input="searchUsers"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="user-list">
            <div 
              v-for="user in filteredUsers" 
              :key="user.id"
              class="user-item"
            >
              <div class="user-info">
                <el-avatar :size="32" class="user-avatar">
                  {{ user.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <div>
                  <div class="user-name">{{ user.username }}</div>
                  <div class="user-roles">
                    <el-tag 
                      v-for="role in user.roles" 
                      :key="role"
                      size="small"
                      :type="getRoleTagType(role)"
                      class="role-tag"
                    >
                      {{ getRoleName(role) }}
                    </el-tag>
                    <span v-if="!user.roles || user.roles.length === 0" class="no-role">
                      暂无角色
                    </span>
                  </div>
                </div>
              </div>
              <el-button 
                type="text" 
                size="small" 
                @click="editUserRoles(user)"
              >
                编辑
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加/编辑角色对话框 -->
    <el-dialog 
      :title="isEditingRole ? '编辑角色' : '添加角色'"
      v-model="roleDialogVisible" 
      width="500px"
    >
      <el-form :model="roleForm" label-width="80px" :rules="roleRules" ref="roleFormRef">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色代码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色代码" :disabled="isEditingRole" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            v-model="roleForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入角色描述" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户角色分配对话框 -->
    <el-dialog 
      title="分配用户角色"
      v-model="userRoleDialogVisible" 
      width="600px"
    >
      <div v-if="currentEditUser">
        <div class="user-info-header">
          <el-avatar :size="40">{{ currentEditUser.username.charAt(0).toUpperCase() }}</el-avatar>
          <div class="user-details">
            <div class="username">{{ currentEditUser.username }}</div>
            <div class="user-id">ID: {{ currentEditUser.id }}</div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="role-assignment">
          <h4>选择角色</h4>
          <el-checkbox-group v-model="selectedUserRoles">
            <el-checkbox 
              v-for="role in roles" 
              :key="role.roleCode"
              :label="role.roleCode"
              class="role-checkbox"
            >
              <div class="role-option">
                <div class="role-name">{{ role.roleName }}</div>
                <div class="role-desc">{{ role.description }}</div>
              </div>
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="userRoleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, User, Management, Avatar, UserFilled, Files, Monitor, 
  Notebook, House, ChatDotRound, VideoCamera, Setting, Search,
  // 只保留确认存在的基础图标
  Edit, Delete, Check, Close, Star, Bell, 
  Upload, Download, Picture
} from '@element-plus/icons-vue'
import { 
  getRoleList, addRole, updateRole, deleteRole as deleteRoleAPI,
  getMenuPermissions, getRolePermissions, updateRolePermissions,
  getUsersWithRoles, updateUserRoles
} from '../api/rolePermission'

// 响应式数据
const roles = ref([])
const selectedRole = ref(null)
const menuPermissions = ref([])
const users = ref([])
const filteredUsers = ref([])
const userSearchQuery = ref('')

// 对话框控制
const roleDialogVisible = ref(false)
const userRoleDialogVisible = ref(false)
const isEditingRole = ref(false)

// 表单数据
const roleForm = reactive({
  name: '',
  code: '',
  description: '',
  id: null
})

const currentEditUser = ref(null)
const selectedUserRoles = ref([])

// 树形控件配置
const treeProps = {
  children: 'children',
  label: 'permissionName',
  disabled: (data) => false
}

// 表单验证规则
const roleRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色代码', trigger: 'blur' }],
  description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
}

// 初始化数据
onMounted(() => {
  initializeData()
})

// 初始化系统数据
const initializeData = async () => {
  try {
    // 从后端获取角色数据
    const roleRes = await getRoleList()
    roles.value = roleRes.data || []
  } catch (error) {
    console.error('获取角色数据失败:', error)
    ElMessage.error('获取角色数据失败')
  }

  try {
    // 从后端获取菜单权限数据
    const menuRes = await getMenuPermissions()
    menuPermissions.value = menuRes.data || []
    
    // 构建权限树
    permissionTreeData.value = buildPermissionTree(menuPermissions.value)
  } catch (error) {
    console.error('获取菜单权限数据失败:', error)
    ElMessage.error('获取菜单权限数据失败')
  }

  try {
    // 从后端获取用户数据
    const userRes = await getUsersWithRoles()
    users.value = userRes.data || []
    filteredUsers.value = [...users.value]
  } catch (error) {
    console.error('获取用户数据失败:', error)
    ElMessage.error('获取用户数据失败')
  }
}

// 计算属性
const permissionTree = ref()
const permissionTreeData = ref([])

// 选择角色
const selectRole = async (role) => {
  selectedRole.value = role
  
  // 验证角色代码是否存在
  if (!role || !role.roleCode) {
    ElMessage.warning('角色信息不完整，无法获取权限')
    return
  }
  
  try {
    // 从后端获取角色权限
    const res = await getRolePermissions(role.roleCode)
    const permissions = res.data || []
    const permissionIds = permissions.map(p => p.id)
    
    // 设置权限树的选中状态
    if (permissionTree.value) {
      permissionTree.value.setCheckedKeys(permissionIds)
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败')
  }
}

// 权限变更处理
const onPermissionCheck = (data, checked) => {
  if (selectedRole.value) {
    const checkedKeys = permissionTree.value.getCheckedKeys()
    selectedRole.value.permissions = checkedKeys
  }
}

// 保存角色权限
const saveRolePermissions = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请先选择一个角色')
    return
  }
  
  if (!selectedRole.value.roleCode) {
    ElMessage.warning('角色信息不完整，无法保存权限')
    return
  }
  
  try {
    const checkedKeys = permissionTree.value.getCheckedKeys()
    
    // 调用后端API保存权限
    await updateRolePermissions(selectedRole.value.roleCode, checkedKeys)
    
    // 更新本地数据
    selectedRole.value.permissions = checkedKeys
    
    ElMessage.success('权限保存成功')
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error('保存权限失败')
  }
}

// 添加角色
const openAddRoleDialog = () => {
  isEditingRole.value = false
  Object.assign(roleForm, { name: '', code: '', description: '', id: null })
  roleDialogVisible.value = true
}

// 编辑角色
const editRole = (role) => {
  isEditingRole.value = true
  Object.assign(roleForm, { 
    name: role.roleName,
    code: role.roleCode,
    description: role.description,
    id: role.id
  })
  roleDialogVisible.value = true
}

// 保存角色
const saveRole = async () => {
  try {
    // 转换字段名以匹配后端期望
    const roleData = {
      roleName: roleForm.name,
      roleCode: roleForm.code,
      description: roleForm.description
    }
    
    if (isEditingRole.value) {
      // 更新角色时需要包含ID
      roleData.id = roleForm.id
      await updateRole(roleData)
      ElMessage.success('角色更新成功')
    } else {
      // 添加角色
      await addRole(roleData)
      ElMessage.success('角色添加成功')
    }
    
    // 重新加载角色列表
    await initializeData()
    
    roleDialogVisible.value = false
  } catch (error) {
    console.error('保存角色失败:', error)
    ElMessage.error('保存角色失败')
  }
}

// 删除角色
const deleteRole = (role) => {
  if (!role || !role.roleCode) {
    ElMessage.warning('角色信息不完整，无法删除')
    return
  }
  
  ElMessageBox.confirm(`确定要删除角色"${role.roleName}"吗？`, '确认删除', {
    type: 'warning'
  }).then(async () => {
      try {
    // 调用后端API删除角色
    await deleteRoleAPI(role.roleCode)
    
    // 重新加载角色列表
    await initializeData()
    
    ElMessage.success('角色删除成功')
  } catch (error) {
    console.error('删除角色失败:', error)
    ElMessage.error('删除角色失败')
  }
  }).catch(() => {
    // 用户取消删除
  })
}

// 搜索用户
const searchUsers = () => {
  if (!userSearchQuery.value) {
    filteredUsers.value = [...users.value]
  } else {
    filteredUsers.value = users.value.filter(user => 
      user.username.toLowerCase().includes(userSearchQuery.value.toLowerCase())
    )
  }
}

// 打开用户角色分配对话框
const openUserRoleDialog = () => {
  userRoleDialogVisible.value = true
}

// 编辑用户角色
const editUserRoles = (user) => {
  currentEditUser.value = user
  selectedUserRoles.value = [...(user.roles || [])]
  userRoleDialogVisible.value = true
}

// 保存用户角色
const saveUserRoles = async () => {
  if (!currentEditUser.value) return
  
  try {
    // 调用后端API更新用户角色
    await updateUserRoles(currentEditUser.value.id, selectedUserRoles.value)
    
    // 更新本地数据
    currentEditUser.value.roles = [...selectedUserRoles.value]
    ElMessage.success('用户角色分配成功')
    userRoleDialogVisible.value = false
  } catch (error) {
    console.error('保存用户角色失败:', error)
    ElMessage.error('保存用户角色失败')
  }
}

// 获取角色标签类型
const getRoleTagType = (roleCode) => {
  const typeMap = {
    'admin': 'danger',
    'teacher': 'success',
    'bbs_admin': 'warning'
  }
  return typeMap[roleCode] || 'info'
}

// 获取角色名称
const getRoleName = (roleCode) => {
  const role = roles.value.find(r => r.roleCode === roleCode)
  return role ? role.roleName : roleCode
}

// 构建权限树
const buildPermissionTree = (permissions) => {
  if (!permissions || permissions.length === 0) {
    return []
  }
  
  // 检查数据是否已经是树形结构
  const hasTreeStructure = permissions.some(item => 
    item.children && Array.isArray(item.children) && item.children.length > 0
  )
  
  if (hasTreeStructure) {
    console.log('检测到数据已经是树形结构，直接使用')
    // 过滤出根节点（没有parentId的节点）
    const rootNodes = permissions.filter(item => !item.parentId || item.parentId === null)
    console.log('树形结构根节点数量:', rootNodes.length)
    return rootNodes
  }
  
  console.log('检测到扁平数据，开始构建树形结构')
  
  // 对于扁平数据，执行原有的构建逻辑
  const nodeMap = new Map()
  permissions.forEach(permission => {
    nodeMap.set(permission.id, {
      ...permission,
      children: []
    })
  })
  
  const rootNodes = []
  const childNodes = []
  
  permissions.forEach(permission => {
    if (!permission.parentId || permission.parentId === null) {
      rootNodes.push(permission)
    } else {
      childNodes.push(permission)
    }
  })
  
  // 构建父子关系
  childNodes.forEach(childPermission => {
    const childNode = nodeMap.get(childPermission.id)
    const parentNode = nodeMap.get(childPermission.parentId)
    
    if (parentNode) {
      parentNode.children.push(childNode)
    }
  })
  
  return rootNodes.map(root => nodeMap.get(root.id))
}

// 获取图标组件
const getIconComponent = (iconName) => {
  const iconComponents = {
    Plus, House, User, Notebook, Setting, Management, 
    Files, Monitor, ChatDotRound, VideoCamera,
    Avatar, UserFilled, Search, Edit, Delete, Check, Close, Star, Bell, 
    Upload, Download, Picture
  }
  return iconComponents[iconName] || House
}


</script>

<style scoped>
.role-permission-page {
  padding: 24px;
}

.header-card {
  margin-bottom: 24px;
}

.header-card h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-list {
  max-height: 600px;
  overflow-y: auto;
}

.role-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.role-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.12);
}

.role-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.role-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role-icon {
  font-size: 24px;
  color: #409eff;
}

.role-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.role-desc {
  font-size: 12px;
  color: #909399;
}

.permission-config {
  padding: 16px 0;
}

.selected-role {
  text-align: center;
  margin-bottom: 16px;
}

.permission-tree {
  max-height: 500px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-icon {
  font-size: 16px;
  color: #409eff;
}

.no-selection {
  text-align: center;
  padding: 80px 0;
}

.user-search {
  margin-bottom: 16px;
}

.user-list {
  max-height: 600px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-roles {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.role-tag {
  font-size: 12px;
}

.no-role {
  font-size: 12px;
  color: #909399;
}

.user-info-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-details .username {
  font-weight: 600;
  font-size: 16px;
}

.user-details .user-id {
  color: #909399;
  font-size: 12px;
}

.role-assignment h4 {
  margin-bottom: 16px;
}

.role-checkbox {
  display: block;
  margin-bottom: 12px;
}

.role-option {
  margin-left: 8px;
}

.role-option .role-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.role-option .role-desc {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .el-col {
    margin-bottom: 24px;
  }
}
</style> 