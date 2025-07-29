<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listCategories, addCategory, updateCategory, deleteCategory } from '../api/bbsCategory'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listCategories()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)

// 添加/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: '', name: '', status: 1 })
const openAdd = () => {
  Object.assign(form, { id: '', name: '', status: 1 })
  isEdit.value = false
  dialogVisible.value = true
}
const openEdit = (row) => {
  Object.assign(form, { id: row.id, name: row.name, status: row.status })
  isEdit.value = true
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.name) { ElMessage.warning('请输入分类名称'); return }
  try {
    if (isEdit.value) {
      await updateCategory(form.id, { name: form.name, status: form.status })
      ElMessage.success('修改成功')
    } else {
      await addCategory({ name: form.name, status: form.status })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}
</script>

<template>
  <div class="bbs-cat-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">添加分类</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="isEdit ? '编辑分类' : '添加分类'" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="分类名称"><el-input v-model="form.name" autocomplete="off" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bbs-cat-page { padding: 24px; }
.toolbar { margin-bottom: 16px; }
</style> 