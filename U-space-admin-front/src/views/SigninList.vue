<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addSignin, listSignins, detailSignin } from '../api/signin'
import { listGroups } from '../api/group'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: 'created_at', desc: true })
const groups = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listSignins(page)
    const pd = res.data || {}
    tableData.value = pd.list ?? pd.records ?? []
    total.value = Number(pd.total ?? pd.totalCount ?? 0)
    page.pageSize = Number(pd.pageSize ?? page.pageSize)
    page.pageNum = Number(pd.pageNum ?? page.pageNum)
  } finally {
    loading.value = false
  }
}

const fetchGroups = async () => {
  try {
    const res = await listGroups()
    groups.value = res.data || []
  } catch (e) {}
}

onMounted(() => {
  fetchData()
  fetchGroups()
})

const handleSizeChange = (s) => { page.pageSize = s; fetchData() }
const handleCurrentChange = (n) => { page.pageNum = n; fetchData() }
const handleSortChange = ({ prop, order }) => {
  page.sortField = prop
  page.desc = order === 'descending'
  fetchData()
}

// add dialog
const dialogVisible = ref(false)
const form = reactive({ groupId: '', type: 0, password: '', endTime: '' })

const openAdd = () => {
  Object.assign(form, { groupId: '', type: 0, password: '', endTime: '' })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.groupId) {
    ElMessage.warning('请选择所属群组')
    return
  }
  if (form.type === 1 && !form.password) {
    ElMessage.warning('请输入签到密码')
    return
  }
  try {
    const payload = { ...form }
    if (payload.endTime) {
      payload.endTime = dayjs(payload.endTime).format('YYYY-MM-DD HH:mm:ss')
    }
    await addSignin(payload)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const detailDialog = ref(false)
const detailLoading = ref(false)
const detailData = ref([])
const currentDetailId = ref(null)
let detailTimer = null

const fetchDetail = async () => {
  if (!currentDetailId.value) return
  try {
    const res = await detailSignin(currentDetailId.value)
    detailData.value = res.data || []
  } catch (e) {}
}

const openDetail = async (row) => {
  const idVal = row.id ?? row.signinId ?? row.signInId
  if (!idVal) {
    ElMessage.error('获取签到ID失败')
    return
  }
  currentDetailId.value = idVal
  detailLoading.value = true
  await fetchDetail()
  detailLoading.value = false
  detailDialog.value = true
  if (detailTimer) clearInterval(detailTimer)
  detailTimer = setInterval(fetchDetail, 1000)
}

watch(detailDialog, (val) => {
  if (!val && detailTimer) {
    clearInterval(detailTimer)
    detailTimer = null
    currentDetailId.value = null
  }
})

onBeforeUnmount(() => {
  if (detailTimer) clearInterval(detailTimer)
})
</script>

<template>
  <div class="signin-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增签到</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" @sort-change="handleSortChange" style="width:100%">
      <el-table-column prop="groupName" label="群组" />
      <el-table-column prop="type" label="签到类型">
        <template #default="{ row }">
          {{ row.type === 0 ? '普通' : row.type === 1 ? '密码' : '位置' }}
        </template>
      </el-table-column>
      <el-table-column prop="password" label="密码">
        <template #default="{ row }">
          {{ row.type === 1 ? row.password : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="endTime" label="截止时间" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, sizes, total" :page-sizes="[10,20,50]"
        :total="total" :current-page="page.pageNum" :page-size="page.pageSize"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>
  </div>

  <el-dialog title="新增签到" v-model="dialogVisible" width="500px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="所属群组">
        <el-select v-model="form.groupId" placeholder="请选择群组" style="width:100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到类型">
        <el-radio-group v-model="form.type">
          <el-radio :label="0">普通</el-radio>
          <el-radio :label="1">密码</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="密码" v-if="form.type===1"><el-input v-model="form.password" /></el-form-item>
      <el-form-item label="截止时间"><el-date-picker v-model="form.endTime" type="datetime" placeholder="选择日期时间" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog title="签到详情" v-model="detailDialog" width="600px">
    <el-table :data="detailData" v-loading="detailLoading" border style="width:100%">
      <el-table-column prop="studentId" label="学号" />
      <el-table-column prop="studentName" label="姓名" />
      <el-table-column prop="signinTime" label="签到时间" />
      <el-table-column prop="isLate" label="是否迟到">
        <template #default="{ row }">{{ row.isLate===1 ? '迟到' : '正常' }}</template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<style scoped>
.signin-page { padding:24px; }
.toolbar { margin-bottom:16px; }
.pagination { margin-top:16px; display:flex; justify-content:flex-end; }
</style> 