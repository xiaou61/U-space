<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listGroups, addGroup, deleteGroup, generateId, listGroupMembers } from '../api/group'

const loading = ref(false)
const groups = ref([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await listGroups()
    groups.value = res.data || []
  } catch (e) {}
  loading.value = false
}

onMounted(fetchList)

// add dialog
const dialogVisible = ref(false)
const form = reactive({ name: '', description: '' })

const openAdd = () => {
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.description) {
    ElMessage.warning('请输入完整信息')
    return
  }
  try {
    await addGroup(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchList()
  } catch(e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定解散该群组吗?', '提示', { type:'warning' }).then(async() => {
    await deleteGroup(row.id)
    ElMessage.success('已解散')
    fetchList()
  }).catch(()=>{})
}

const handleGenerate = async (row) => {
  try {
    const res = await generateId(row.id)
    const code = res.data || res.msg
    await ElMessageBox.alert(`加入码：${code}`, '生成成功', { confirmButtonText: '复制', callback: () => { navigator.clipboard?.writeText(code) } })
  } catch(e) {}
}

const membersDialog = ref(false)
const membersLoading = ref(false)
const members = ref([])

const openMembers = async (row) => {
  membersLoading.value = true
  try {
    const res = await listGroupMembers(row.id)
    members.value = res.data || []
    membersDialog.value = true
  } catch (e) {}
  membersLoading.value = false
}
</script>

<template>
  <div class="group-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增群组</el-button>
    </div>

    <el-table :data="groups" v-loading="loading" border style="width:100%">
      <el-table-column prop="name" label="群组名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleGenerate(row)">生成加入码</el-button>
          <el-button size="small" @click="openMembers(row)">成员</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">解散</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增群组" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="群组名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="群组成员" v-model="membersDialog" width="600px">
      <el-table :data="members" v-loading="membersLoading" border style="width:100%">
        <el-table-column prop="userId" label="成员ID" />
        <el-table-column prop="userName" label="姓名" />
        <el-table-column prop="joinedAt" label="加入时间" />
      </el-table>
    </el-dialog>
  </div>
</template>

<style scoped>
.group-page { padding:24px; }
.toolbar { margin-bottom:16px; }
</style> 