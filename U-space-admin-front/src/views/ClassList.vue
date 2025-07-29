<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { pageClass, deleteClass, addClass, updateClass, importClassExcel, exportClassExcel, downloadClassTemplate } from '../api/class'

const loading = ref(false)

const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: '', desc: true })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageClass(page)
    const pageData = res.data || {}
    tableData.value = pageData.list ?? pageData.records ?? []
    const totalRaw = pageData.total ?? pageData.totalCount ?? 0
    total.value = Number(totalRaw)
    page.pageSize = Number(pageData.pageSize ?? page.pageSize)
    page.pageNum  = Number(pageData.pageNum  ?? page.pageNum)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const handleSizeChange = (size) => {
  page.pageSize = size
  fetchData()
}
const handleCurrentChange = (num) => {
  page.pageNum = num
  fetchData()
}

const handleSortChange = ({ prop, order }) => {
  page.sortField = prop
  page.desc = order === 'descending'
  fetchData()
}

// dialog
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  className: '',
  grade: '',
  major: '',
  classTeacher: '',
  studentCount: 0
})
const currentId = ref(null)

const openAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { className: '', grade: '', major: '', classTeacher: '', studentCount: 0 })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    if (isEdit.value) {
      await updateClass(currentId.value, form)
      ElMessage.success('修改成功')
    } else {
      await addClass(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该班级吗?', '提示', { type: 'warning' })
    .then(async () => {
      await deleteClass(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

// file upload
const uploadRequest = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  const loadingSrv = ElLoading.service({ fullscreen: true, text: '正在导入，请稍候...', background: 'rgba(0,0,0,0.4)' })
  try {
    await importClassExcel(formData)
    ElMessage.success('导入成功')
    fetchData()
    options.onSuccess()
  } catch (e) {
    ElMessage.error('导入失败')
    options.onError(e)
  } finally {
    loadingSrv.close()
  }
}

const saveBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

const handleExport = async () => {
  const blobRes = await exportClassExcel()
  saveBlob(blobRes, '班级信息.xlsx')
}

const handleDownloadTemplate = async () => {
  const blobRes = await downloadClassTemplate()
  saveBlob(blobRes, '班级导入模板.xlsx')
}
</script>

<template>
  <div class="class-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增班级</el-button>
      <el-upload :show-file-list="false" :http-request="uploadRequest" accept=".xlsx,.xls">
        <el-button>导入 Excel</el-button>
      </el-upload>
      <el-button @click="handleExport">导出 Excel</el-button>
      <el-button @click="handleDownloadTemplate">下载模板</el-button>
    </div>
    <el-table :data="tableData" border style="width: 100%" v-loading="loading" @sort-change="handleSortChange">
      <el-table-column prop="className" label="班级名称" sortable="custom"/>
      <el-table-column prop="grade" label="年级" sortable="custom"/>
      <el-table-column prop="major" label="专业"/>
      <el-table-column prop="classTeacher" label="班主任"/>
      <el-table-column prop="studentCount" label="学生人数" sortable="custom"/>
      <el-table-column prop="createdAt" label="创建时间" sortable="custom"/>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :page-sizes="[10,20,50,100]"
        :total="total"
        :current-page="page.pageNum"
        :page-size="page.pageSize"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog :title="isEdit ? '编辑班级' : '新增班级'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="班级名称">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="form.grade" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班主任">
          <el-input v-model="form.classTeacher" />
        </el-form-item>
        <el-form-item label="学生人数">
          <el-input-number v-model="form.studentCount" :min="0" />
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
.class-page {
  padding: 24px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style> 