<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listBbsAdmins, addBbsAdmin, deleteBbsAdmin } from '../api/bbs'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listBbsAdmins()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)

// 添加对话框
const dialogVisible = ref(false)
const form = reactive({ username: '', password: '' })
const openAdd = () => {
  Object.assign(form, { username: '', password: '' })
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.username || !form.password) { ElMessage.warning('请输入完整信息'); return }
  try {
    await addBbsAdmin(form.username, form.password)
    ElMessage.success('添加成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await deleteBbsAdmin(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}
</script>

<template>
  <div class="bbs-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">添加管理员</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading" style="width:400px">
      <el-table-column prop="username" label="用户名" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="添加BBS管理员" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="form.username" autocomplete="off" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" autocomplete="off" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bbs-page { padding: 24px; }
.toolbar { margin-bottom: 16px; }
</style> 