<script setup>
import { reactive, ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageTeacher, deleteTeacher, addTeacher } from '../api/teacher'

const loading = ref(false)

const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: '', desc: true })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageTeacher(page)
    const pageData = res.data || {}
    tableData.value = pageData.list ?? pageData.records ?? []
    total.value = Number(pageData.total ?? pageData.totalCount ?? 0)
    page.pageSize = Number(pageData.pageSize ?? page.pageSize)
    page.pageNum = Number(pageData.pageNum ?? page.pageNum)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const handleSizeChange = (val) => { page.pageSize = val; fetchData() }
const handleCurrentChange = (val) => { page.pageNum = val; fetchData() }

const handleSortChange = ({ prop, order }) => {
  page.sortField = prop
  page.desc = order === 'descending'
  fetchData()
}

// add dialog (basic)
const dialogVisible = ref(false)
const form = reactive({ employeeNo: '', name: '', phone: '' })

const openAdd = () => {
  Object.keys(form).forEach(k => form[k] = '')
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!/^\d{8}$/.test(form.employeeNo)) {
    ElMessage.warning('教工编号必须为8位数字')
    return
  }
  try {
    await addTeacher(form)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchData()
  } catch(e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该教师吗?', '提示', { type:'warning' }).then(async() => {
    await deleteTeacher(row.id)
    ElMessage.success('删除成功');
    fetchData()
  }).catch(()=>{})
}

// dynamic columns keys
const columnKeys = ref([])
watch(tableData, val => {
  columnKeys.value = val.length ? Object.keys(val[0]).filter(k => !['id','password'].includes(k)) : []
})
</script>

<template>
  <div class="teacher-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增教师</el-button>
    </div>
    <el-table :data="tableData" border style="width:100%" v-loading="loading" @sort-change="handleSortChange">
      <el-table-column v-for="key in columnKeys" :key="key" :prop="key" :label="key" sortable="custom" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, sizes, total" :page-sizes="[10,20,50]"
        :total="total" :current-page="page.pageNum" :page-size="page.pageSize"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog title="新增教师" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="教工编号">
          <el-input v-model="form.employeeNo" maxlength="8" show-word-limit @input="val => form.employeeNo = val.replace(/[^\d]/g,'').slice(0,8)" />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name"/></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.teacher-page { padding:24px; }
.toolbar { margin-bottom:16px; }
.pagination { margin-top:16px; display:flex; justify-content:flex-end; }
</style> 