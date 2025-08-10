<template>
  <div class="menu-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>菜单权限管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            添加菜单
          </el-button>
        </div>
      </template>

      <!-- 权限树表格 -->
      <el-table 
        :data="permissionTreeData" 
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        default-expand-all
      >
        <el-table-column prop="permissionName" label="菜单名称" width="200" />
        <el-table-column prop="permissionCode" label="权限代码" width="200" />
        <el-table-column prop="permissionType" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.permissionType)">
              {{ getTypeText(scope.row.permissionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuPath" label="路径" width="200" />
        <el-table-column prop="menuIcon" label="图标" width="100">
          <template #default="scope">
            <el-icon v-if="scope.row.menuIcon">
              <component :is="getIconComponent(scope.row.menuIcon)" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" @click="editPermission(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="success" @click="addChildPermission(scope.row)">
              添加子菜单
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deletePermission(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑权限对话框 -->
    <el-dialog 
      :title="isEdit ? '编辑菜单' : '添加菜单'"
      v-model="dialogVisible" 
      width="600px"
    >
      <el-form :model="permissionForm" :rules="permissionRules" ref="permissionFormRef" label-width="120px">
        <el-form-item label="父级菜单" prop="parentId" v-if="!isEdit">
          <el-select v-model="permissionForm.parentId" placeholder="请选择父级菜单（可为空）" clearable style="width: 100%">
            <el-option label="无（根菜单）" value="" />
            <el-option 
              v-for="permission in flatPermissions" 
              :key="permission.id" 
              :label="permission.permissionName" 
              :value="permission.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权限代码" prop="permissionCode">
          <el-input v-model="permissionForm.permissionCode" placeholder="请输入权限代码（唯一）" />
        </el-form-item>
        <el-form-item label="菜单名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-select v-model="permissionForm.permissionType" placeholder="请选择权限类型" style="width: 100%">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单路径" prop="menuPath" v-if="permissionForm.permissionType === 'MENU'">
          <el-input v-model="permissionForm.menuPath" placeholder="请输入菜单路径" />
        </el-form-item>
        <el-form-item label="菜单图标" prop="menuIcon" v-if="permissionForm.permissionType === 'MENU'">
          <el-select 
            v-model="permissionForm.menuIcon" 
            placeholder="请选择图标" 
            style="width: 100%"
            filterable
          >
            <el-option 
              v-for="icon in iconOptions" 
              :key="icon.value" 
              :label="icon.label" 
              :value="icon.value"
            >
              <div style="display: flex; align-items: center;">
                <el-icon style="margin-right: 8px;">
                  <component :is="getIconComponent(icon.value)" />
                </el-icon>
                <span>{{ icon.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="permissionForm.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermission">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, House, User, Notebook, Setting, Management, 
  Files, Monitor, ChatDotRound, VideoCamera,
  Search, Edit, Delete, Check, Close, Star, Bell, 
  Upload, Download, Picture
} from '@element-plus/icons-vue'
import { 
  getPermissionTree, getAllPermissions, addPermission, 
  updatePermission, deletePermission as deletePermissionAPI, getPermissionById 
} from '../api/menuManagement'

// 响应式数据
const permissionTreeData = ref([])
const flatPermissions = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

// 表单数据
const permissionForm = reactive({
  id: '',
  parentId: '',
  permissionCode: '',
  permissionName: '',
  permissionType: 'MENU',
  menuPath: '',
  menuIcon: '',
  sortOrder: 1
})

// 表单验证规则
const permissionRules = {
  permissionCode: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { min: 2, max: 50, message: '权限代码长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  permissionName: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '菜单名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  permissionType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 表单引用
const permissionFormRef = ref()

// 页面加载
onMounted(() => {
  loadPermissionTree()
  loadFlatPermissions()
})

// 图标组件映射
const iconComponents = {
  Plus, House, User, Notebook, Setting, Management, 
  Files, Monitor, ChatDotRound, VideoCamera,
  Search, Edit, Delete, Check, Close, Star, Bell, 
  Upload, Download, Picture
}

// 图标选项数组
const iconOptions = [
  { label: '首页', value: 'House' },
  { label: '用户', value: 'User' },
  { label: '笔记本', value: 'Notebook' },
  { label: '设置', value: 'Setting' },
  { label: '管理', value: 'Management' },
  { label: '文件夹', value: 'Files' },
  { label: '监控', value: 'Monitor' },
  { label: '聊天', value: 'ChatDotRound' },
  { label: '视频', value: 'VideoCamera' },
  { label: '搜索', value: 'Search' },
  { label: '编辑', value: 'Edit' },
  { label: '删除', value: 'Delete' },
  { label: '检查', value: 'Check' },
  { label: '关闭', value: 'Close' },
  { label: '星星', value: 'Star' },
  { label: '铃铛', value: 'Bell' },
  { label: '上传', value: 'Upload' },
  { label: '下载', value: 'Download' },
  { label: '图片', value: 'Picture' },
  { label: '加号', value: 'Plus' }
]

// 获取图标组件
const getIconComponent = (iconName) => {
  return iconComponents[iconName] || House
}

// 获取类型标签类型
const getTypeTagType = (type) => {
  const typeMap = {
    'MENU': 'primary',
    'BUTTON': 'success',
    'API': 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = {
    'MENU': '菜单',
    'BUTTON': '按钮',
    'API': 'API'
  }
  return typeMap[type] || type
}

// 加载权限树
const loadPermissionTree = async () => {
  try {
    const res = await getPermissionTree()
    permissionTreeData.value = res.data || []
  } catch (error) {
    console.error('获取权限树失败:', error)
    ElMessage.error('获取权限树失败')
  }
}

// 加载扁平权限列表（用于父级选择）
const loadFlatPermissions = async () => {
  try {
    const res = await getAllPermissions()
    flatPermissions.value = res.data || []
  } catch (error) {
    console.error('获取权限列表失败:', error)
  }
}

// 打开添加对话框
const openAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 添加子权限
const addChildPermission = (parent) => {
  isEdit.value = false
  resetForm()
  permissionForm.parentId = parent.id
  dialogVisible.value = true
}

// 编辑权限
const editPermission = async (permission) => {
  isEdit.value = true
  try {
    const res = await getPermissionById(permission.id)
    const data = res.data
    Object.assign(permissionForm, {
      id: data.id,
      parentId: data.parentId || '',
      permissionCode: data.permissionCode,
      permissionName: data.permissionName,
      permissionType: data.permissionType,
      menuPath: data.menuPath || '',
      menuIcon: data.menuIcon || '',
      sortOrder: data.sortOrder || 1,
      status: data.status
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取权限详情失败:', error)
    ElMessage.error('获取权限详情失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(permissionForm, {
    id: '',
    parentId: '',
    permissionCode: '',
    permissionName: '',
    permissionType: 'MENU',
    menuPath: '',
    menuIcon: '',
    sortOrder: 1,
    status: 1
  })
}

// 保存权限
const savePermission = async () => {
  if (!permissionFormRef.value) return
  
  await permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updatePermission(permissionForm)
          ElMessage.success('更新菜单成功')
        } else {
          await addPermission(permissionForm)
          ElMessage.success('添加菜单成功')
        }
        dialogVisible.value = false
        loadPermissionTree()
        loadFlatPermissions()
      } catch (error) {
        console.error('保存菜单失败:', error)
        ElMessage.error('保存菜单失败')
      }
    }
  })
}

// 删除权限
const deletePermission = (permission) => {
  ElMessageBox.confirm(
    `确定要删除菜单"${permission.permissionName}"吗？`,
    '确认删除',
    {
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePermissionAPI(permission.id)
      ElMessage.success('删除菜单成功')
      loadPermissionTree()
      loadFlatPermissions()
    } catch (error) {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}
</script>

<style scoped>
.menu-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 