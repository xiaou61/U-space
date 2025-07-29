<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listGroups } from '../api/group'
import { listHomework, addHomework } from '../api/homework'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()
const groups = ref([])
const selectedGroup = ref('')

const loading = ref(false)
const tableData = ref([])

const fetchGroups = async () => {
  try {
    const res = await listGroups()
    groups.value = res.data || []
  } catch (e) {}
}

const fetchHomework = async () => {
  if (!selectedGroup.value) { tableData.value = []; return }
  loading.value = true
  try {
    const res = await listHomework(selectedGroup.value)
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(async () => {
  await fetchGroups()
})

watch(selectedGroup, () => fetchHomework())

// add dialog
const dialogVisible = ref(false)
const form = reactive({ groupId: '', title: '', description: '', deadline: '' })

const openAdd = () => {
  if (!groups.value.length) { ElMessage.warning('暂无群组'); return }
  Object.assign(form, { groupId: selectedGroup.value || groups.value[0].id, title: '', description: '', deadline: '' })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.title || !form.groupId) { ElMessage.warning('请输入完整信息'); return }
  const payload = { ...form }
  if (payload.deadline) payload.deadline = dayjs(payload.deadline).format('YYYY-MM-DD HH:mm:ss')
  try {
    await addHomework(payload)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchHomework()
  } catch(e) {}
}

const openDetail = (row) => {
  router.push(`/homework/${row.id}`)
}
</script>

<template>
  <div class="hw-page">
    <div class="toolbar">
      <el-select v-model="selectedGroup" placeholder="选择群组" style="width:200px" @change="fetchHomework">
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <el-button type="primary" @click="openAdd">创建作业</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="deadline" label="截止时间" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog title="创建作业" v-model="dialogVisible" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="所属群组">
        <el-select v-model="form.groupId" style="width:100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="描述"><el-input type="textarea" v-model="form.description" rows="3" /></el-form-item>
      <el-form-item label="截止时间"><el-date-picker v-model="form.deadline" type="datetime" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.hw-page{padding:24px;}
.toolbar{margin-bottom:16px;display:flex;gap:12px;align-items:center;}
</style> 