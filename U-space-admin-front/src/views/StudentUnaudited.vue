<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUnauditedStudents, auditStudent } from '../api/student'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUnauditedStudents()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(fetchData)

const handleAudit = (row) => {
  const sid = row.id
  ElMessageBox.confirm('确认审核通过该学生?','提示',{type:'warning'}).then(async()=>{
    await auditStudent(sid)
    ElMessage.success('审核成功')
    fetchData()
  }).catch(()=>{})
}
</script>

<template>
  <div class="studentEntity-page">
    <el-table :data="tableData" border style="width:100%" v-loading="loading">
      <el-table-column prop="studentNo" label="学号"/>
      <el-table-column prop="name" label="姓名"/>
      <el-table-column prop="className" label="班级"/>
      <el-table-column prop="phone" label="手机号"/>
      <el-table-column prop="status" label="审核状态">
        <template #default>未审核</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button type="primary" size="small" @click="handleAudit(row)">审核</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.studentEntity-page{padding:24px;}
</style> 