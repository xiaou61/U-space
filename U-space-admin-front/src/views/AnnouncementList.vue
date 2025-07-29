<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addAnnouncement, listAnnouncements, deleteAnnouncements } from '../api/announcement'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAnnouncements()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// add dialog
const dialogVisible = ref(false)
const form = reactive({ content: '', needPopup: 0 })

const openAdd = () => {
  Object.assign(form, { content: '', needPopup: 0 })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.content) {
    ElMessage.warning('请输入公告内容')
    return
  }
  try {
    await addAnnouncement(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

// delete
const multipleSelection = ref([])
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection.map(item => item.id)
}

const handleBatchDelete = () => {
  if (!multipleSelection.value.length) {
    ElMessage.warning('请选择要删除的公告')
    return
  }
  confirmDelete(multipleSelection.value)
}

const handleDelete = (row) => {
  confirmDelete([row.id])
}

const confirmDelete = (ids) => {
  ElMessageBox.confirm('确认删除所选公告吗?', '提示', { type: 'warning' })
    .then(async () => {
      await deleteAnnouncements(ids)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}
</script>

<template>
  <div class="announcement-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增公告</el-button>
      <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" @selection-change="handleSelectionChange" style="width:100%">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="content" label="公告内容" />
      <el-table-column prop="needPopup" label="弹框提醒">
        <template #default="{ row }">{{ row.needPopup === 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

  </div>

  <el-dialog title="新增公告" v-model="dialogVisible" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="公告内容">
        <el-input v-model="form.content" type="textarea" rows="3" />
      </el-form-item>
      <el-form-item label="弹框提醒">
        <el-switch v-model="form.needPopup" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.announcement-page {
  padding: 24px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
</style> 