<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBuildings, addBuilding, removeBuilding } from '../api/dorm'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listBuildings()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// add dialog
const dialogVisible = ref(false)
const form = reactive({ name: '', remark: '' })

const openAdd = () => {
  Object.assign(form, { name: '', remark: '' })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入宿舍楼名称')
    return
  }
  try {
    await addBuilding(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除宿舍楼【${row.name}】?`, '提示', { type: 'warning' })
    .then(async () => {
      await removeBuilding(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}
</script>

<template>
  <div class="dorm-building-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增宿舍楼</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="name" label="宿舍楼名称" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增宿舍楼" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dorm-building-page{padding:24px;}
.toolbar{margin-bottom:16px;}
</style> 