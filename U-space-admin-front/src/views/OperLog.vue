<script setup>
import { reactive, ref, onMounted } from 'vue'
import { pageOperLog, exportOperLogExcel } from '../api/operlog'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum:1, pageSize:10, sortField:'oper_time', desc:true })
const dateRange = ref([])

const fetchData = async ()=>{
  loading.value=true
  try{
    const res = await pageOperLog(page)
    const pd = res.data || {}
    tableData.value = pd.list ?? pd.records ?? []
    total.value = Number(pd.total ?? 0)
    page.pageNum = Number(pd.pageNum ?? page.pageNum)
    page.pageSize = Number(pd.pageSize ?? page.pageSize)
  }finally{loading.value=false}
}

onMounted(fetchData)

const handleSize=(s)=>{page.pageSize=s;fetchData()}
const handleCurrent=(n)=>{page.pageNum=n;fetchData()}
const camelToSnake = (s)=> s.replace(/[A-Z]/g, letter=>'_'+letter.toLowerCase())
const handleSort=({prop,order})=>{
  page.sortField = camelToSnake(prop)
  page.desc = order==='descending'
  fetchData()
}

const saveBlob=(blob,filename)=>{
  const url=window.URL.createObjectURL(blob)
  const link=document.createElement('a')
  link.href=url;link.download=filename;link.click();window.URL.revokeObjectURL(url)
}
const handleExport=async()=>{
  if(!dateRange.value || dateRange.value.length!==2){
    ElMessage.warning('请选择导出时间范围')
    return
  }
  const [start,end] = dateRange.value
  const beginTime = dayjs(start).format('YYYY-MM-DDTHH:mm:ss')
  const endTime = dayjs(end).format('YYYY-MM-DDTHH:mm:ss')
  const blob= await exportOperLogExcel(beginTime, endTime)
  saveBlob(blob,`操作日志-${dayjs(beginTime).format('YYYYMMDD')}-${dayjs(endTime).format('YYYYMMDD')}.xlsx`)
}
</script>

<template>
  <div class="operlog-page">
    <div class="toolbar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="margin-right:12px"
      />
      <el-button @click="handleExport">导出 Excel</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" border style="width:100%" @sort-change="handleSort" :default-sort="{ prop: 'operTime', order: 'descending' }">
      <el-table-column prop="title" label="模块"/>
      <el-table-column prop="businessType" label="业务类型"/>
      <el-table-column prop="method" label="方法"/>
      <el-table-column prop="requestMethod" label="请求方式"/>
      <el-table-column prop="operName" label="操作人"/>
      <el-table-column prop="operatorType" label="操作端">
        <template #default="{ row }">{{ row.operatorType === 1 ? '后台用户' : row.operatorType === 2 ? '手机用户' : row.operatorType }}</template>
      </el-table-column>
      <el-table-column prop="operIp" label="IP"/>
      <el-table-column prop="operLocation" label="地点"/>
      <el-table-column prop="status" label="状态">
        <template #default="{row}">{{ row.status===0?'正常':'异常' }}</template>
      </el-table-column>
      <el-table-column prop="operTime" label="时间" sortable="custom"/>
      <el-table-column prop="costTime" label="耗时(ms)" sortable="custom"/>
    </el-table>

    <div class="pagination">
      <el-pagination background layout="prev, pager, next, sizes, total" :page-sizes="[10,20,50]"
        :total="total" :current-page="page.pageNum" :page-size="page.pageSize"
        @size-change="handleSize" @current-change="handleCurrent"/>
    </div>
  </div>
</template>

<style scoped>
.operlog-page{padding:24px;}
.toolbar{margin-bottom:16px;}
.pagination{margin-top:16px;display:flex;justify-content:flex-end;}
</style> 