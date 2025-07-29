<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { listGroups } from '../api/group'
import { pageMaterials, addMaterial, deleteMaterial } from '../api/material'
import { uploadFile } from '../api/file'

// 群组下拉
const groups = ref([])
const selectedGroup = ref('')

// 列表与分页
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = reactive({ pageNum: 1, pageSize: 10, sortField: 'updated_at', desc: true })

// 获取老师自己的群组
const fetchGroups = async () => {
  try {
    const res = await listGroups()
    groups.value = res.data || []
    if (!selectedGroup.value && groups.value.length) {
      selectedGroup.value = groups.value[0].id
    }
  } catch (e) {}
}

// 获取资料分页
const fetchMaterials = async () => {
  loading.value = true
  try {
    const req = { ...page, sortField: page.sortField, desc: page.desc }
    const res = await pageMaterials(req)
    const pd = res.data || {}
    let list = pd.list ?? []
    // 若选择了群组则前端过滤
    if (selectedGroup.value) {
      list = list.filter(item => item.groupId === selectedGroup.value)
    }
    tableData.value = list
    total.value = Number(pd.total ?? 0)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchGroups()
  await fetchMaterials()
})

watch(selectedGroup, () => {
  page.pageNum = 1
  fetchMaterials()
})

const handleSize = (s) => { page.pageSize = s; fetchMaterials() }
const handleCurrent = (n) => { page.pageNum = n; fetchMaterials() }

// 添加资料对话框
const dialogVisible = ref(false)
const form = reactive({ groupId: '', title: '', description: '', fileUrls: [] })

const openAdd = () => {
  if (!groups.value.length) { ElMessage.warning('暂无群组'); return }
  Object.assign(form, { groupId: selectedGroup.value || groups.value[0].id, title: '', description: '', fileUrls: [] })
  dialogVisible.value = true
}

// 上传文件
const handleUploadRequest = async ({ file, onSuccess, onError }) => {
  const fd = new window.FormData()
  fd.append('file', file)
  try {
    const res = await uploadFile(fd)
    // 约定后端返回的 URL 在 res.data 或 res.msg
    const url = res.data || res.msg || ''
    if (url) {
      form.fileUrls.push(url)
    }
    ElMessage.success('上传成功')
    onSuccess()
  } catch (e) {
    ElMessage.error('上传失败')
    onError(e)
  }
}

const removeFile = (url) => {
  form.fileUrls = form.fileUrls.filter(u => u !== url)
}

const submitForm = async () => {
  if (!form.title || !form.groupId) { ElMessage.warning('请输入完整信息'); return }
  try {
    await addMaterial({ ...form })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchMaterials()
  } catch (e) {}
}

const handleDelete = async (row) => {
  try {
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    fetchMaterials()
  } catch (e) {}
}
</script>

<template>
  <div class="material-page">
    <div class="toolbar">
      <el-select v-model="selectedGroup" placeholder="选择群组" style="width:200px">
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
      <el-button type="primary" @click="openAdd">添加资料</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="groupName" label="所属群组" />
      <el-table-column prop="updatedAt" label="更新时间" />
      <el-table-column label="文件">
        <template #default="{ row }">
          <div style="max-width:300px;white-space:pre-wrap;">
            <div v-for="url in row.fileUrls" :key="url">
              <a :href="url" target="_blank">{{ url }}</a>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        background
        layout="prev, pager, next, sizes, total"
        :page-sizes="[10, 20, 50]"
        :total="total"
        :current-page="page.pageNum"
        :page-size="page.pageSize"
        @size-change="handleSize"
        @current-change="handleCurrent"
      />
    </div>
  </div>

  <!-- 添加资料弹窗 -->
  <el-dialog title="添加资料" v-model="dialogVisible" width="600px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="所属群组">
        <el-select v-model="form.groupId" style="width:100%">
          <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input type="textarea" v-model="form.description" rows="3" />
      </el-form-item>
      <el-form-item label="上传文件">
        <el-upload
          :http-request="handleUploadRequest"
          :show-file-list="false"
          multiple
          accept="*/*"
        >
          <el-button type="primary">选择文件</el-button>
        </el-upload>
        <div class="file-list" v-if="form.fileUrls.length">
          <div class="file-item" v-for="url in form.fileUrls" :key="url">
            <a :href="url" target="_blank">{{ url }}</a>
            <span style="cursor:pointer;color:red;margin-left:4px" @click="removeFile(url)">删除</span>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.material-page { padding: 24px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.file-list { margin-top: 8px; }
.file-item { font-size: 12px; color: var(--el-color-primary); }
</style> 