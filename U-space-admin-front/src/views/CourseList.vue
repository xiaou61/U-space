<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { pageCourse, createCourse, updateCourse, deleteCourse } from '../api/course'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: '', desc: true })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageCourse(page)
    const pageData = res.data || {}
    tableData.value = pageData.list ?? pageData.records ?? []
    total.value = Number(pageData.total ?? pageData.totalCount ?? 0)
    page.pageSize = Number(pageData.pageSize ?? page.pageSize)
    page.pageNum  = Number(pageData.pageNum  ?? page.pageNum)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const handleSizeChange = (size) => { page.pageSize = size; fetchData() }
const handleCurrentChange = (num) => { page.pageNum = num; fetchData() }

const handleSortChange = ({ prop, order }) => {
  page.sortField = prop
  page.desc = order === 'descending'
  fetchData()
}

// dialog add/edit
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(null)
const form = reactive({
  courseCode: '',
  courseName: '',
  teacherName: '',
  capacity: 0,
  credit: 0,
  startTime: null,
  endTime: null,
  classroom: '',
  period: '',
  description: ''
})

const openAdd = () => {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { courseCode: '', courseName: '', teacherName: '', capacity: 0, credit: 0, startTime: null, endTime: null, classroom: '', period: '', description: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    ...row,
    startTime: row.startTime ? new Date(row.startTime) : null,
    endTime: row.endTime ? new Date(row.endTime) : null
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  try {
    const payload = { ...form }
    if (payload.startTime) payload.startTime = dayjs(payload.startTime).format('YYYY-MM-DD HH:mm:ss')
    if (payload.endTime)   payload.endTime   = dayjs(payload.endTime).format('YYYY-MM-DD HH:mm:ss')

    if (isEdit.value) {
      await updateCourse(currentId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createCourse(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该课程吗?', '提示', { type: 'warning' })
    .then(async () => {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}
</script>

<template>
  <div class="course-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增课程</el-button>
    </div>
    <el-table :data="tableData" border style="width:100%" v-loading="loading" @sort-change="handleSortChange">
      <el-table-column prop="courseCode" label="课程编号" sortable="custom"/>
      <el-table-column prop="courseName" label="课程名称" sortable="custom"/>
      <el-table-column prop="teacherName" label="授课教师"/>
      <el-table-column prop="capacity" label="容量" sortable="custom"/>
      <el-table-column prop="selectedCount" label="已选人数" sortable="custom"/>
      <el-table-column prop="credit" label="学分" sortable="custom"/>
      <el-table-column prop="startTime" label="开始时间" sortable="custom" />
      <el-table-column prop="endTime" label="结束时间" sortable="custom" />
      <el-table-column prop="classroom" label="教室" />
      <el-table-column prop="period" label="课时" />
      <el-table-column prop="description" label="描述" />
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

    <el-dialog :title="isEdit ? '编辑课程' : '新增课程'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程编号"><el-input v-model="form.courseCode"/></el-form-item>
        <el-form-item label="课程名称"><el-input v-model="form.courseName"/></el-form-item>
        <el-form-item label="授课教师"><el-input v-model="form.teacherName"/></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0"/></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :step="0.5"/></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" /></el-form-item>
        <el-form-item label="教室"><el-input v-model="form.classroom" /></el-form-item>
        <el-form-item label="课时"><el-input v-model="form.period" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.course-page { padding: 24px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style> 