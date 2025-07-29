<script setup>
import { reactive, ref, onMounted } from 'vue'
import { uploadFile, uploadBatch, pageFiles } from '../api/file'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum:1, pageSize:10, sortField:'create_time', desc:true })

const fetchData = async ()=>{
  loading.value=true
  try{
    const res=await pageFiles(page)
    const pd=res.data||{}
    tableData.value=pd.list??[]
    total.value = Number(pd.total??0)
  }finally{loading.value=false}
}

onMounted(fetchData)

const handleSize=(s)=>{page.pageSize=s;fetchData()}
const handleCurrent=(n)=>{page.pageNum=n;fetchData()}

const saveBlob=(blob,name)=>{const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();URL.revokeObjectURL(url)}

const handleUploadRequest = async ({ file, onSuccess, onError }) => {
  const fd = new window.FormData()
  fd.append('file', file)
  try {
    await uploadFile(fd)
    ElMessage.success('上传成功')
    fetchData()
    onSuccess()
  } catch (e) {
    ElMessage.error('上传失败')
    onError(e)
  }
}
</script>

<template>
  <div class="file-page">
    <div class="toolbar">
      <el-upload :http-request="handleUploadRequest" :show-file-list="false" accept="*/*" >
        <el-button type="primary">上传文件</el-button>
      </el-upload>
    </div>
    <el-table :data="tableData" border style="width:100%" v-loading="loading">
      <el-table-column prop="filename" label="文件名"/>
      <el-table-column prop="url" label="URL">
        <template #default="{row}">
          <a :href="row.url" target="_blank">下载</a>
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" />
      <el-table-column prop="ext" label="扩展" />
      <el-table-column prop="platform" label="平台" />
      <el-table-column prop="createTime" label="上传时间" />
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, sizes, total" :page-sizes="[10,20,50]"
        :total="total" :current-page="page.pageNum" :page-size="page.pageSize"
        @size-change="handleSize" @current-change="handleCurrent" />
    </div>
  </div>
</template>

<style scoped>
.file-page{padding:24px;}
.toolbar{margin-bottom:16px;}
.pagination{margin-top:16px;display:flex;justify-content:flex-end;}
</style> 