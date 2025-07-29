<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageStudent } from '../api/studentEntity'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: '', desc: true })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await pageStudent(page)
    const pd = res.data || {}
    tableData.value = pd.list ?? pd.records ?? []
    total.value = Number(pd.total ?? pd.totalCount ?? 0)
    page.pageSize = Number(pd.pageSize ?? page.pageSize)
    page.pageNum = Number(pd.pageNum ?? page.pageNum)
  } finally { loading.value = false }
}

onMounted(fetchData)

const handleSizeChange = (s)=>{page.pageSize=s;fetchData()}
const handleCurrentChange = (n)=>{page.pageNum=n;fetchData()}
const handleSortChange = ({prop,order})=>{page.sortField=prop;page.desc=order==='descending';fetchData()}
</script>

<template>
  <div class="studentEntity-page">
    <el-table :data="tableData" border style="width:100%" v-loading="loading" @sort-change="handleSortChange">
      <el-table-column prop="studentNo" label="学号" sortable="custom"/>
      <el-table-column prop="name" label="姓名"/>
      <el-table-column prop="className" label="班级"/>
      <el-table-column prop="phone" label="手机号"/>
      <el-table-column prop="status" label="审核状态">
        <template #default="{ row }">
          {{ (row.status === '1' || row.status === 1) ? '已通过' : '未审核' }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, sizes, total" :page-sizes="[10,20,50]"
                     :total="total" :current-page="page.pageNum" :page-size="page.pageSize"
                     @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
    </div>
  </div>
</template>

<style scoped>
.studentEntity-page{padding:24px;}
.pagination{margin-top:16px;display:flex;justify-content:flex-end;}
</style> 