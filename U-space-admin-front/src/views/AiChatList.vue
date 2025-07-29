<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAllChats } from '../api/chat'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAllChats()
    // 后端返回的数据假设在 data 字段，回退到 msg
    tableData.value = res.data || res.msg || []
  } catch (e) {
    ElMessage.error('获取对话列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="ai-chat-page">
    <el-table :data="tableData" border v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="120" />
      <el-table-column prop="userContent" label="提问内容" />
      <el-table-column prop="aiContent" label="AI回答" />
      <el-table-column prop="enableRag" label="RAG" width="100">
        <template #default="{ row }">{{ row.enableRag ? '启用' : '关闭' }}</template>
      </el-table-column>
      <el-table-column prop="enableTools" label="工具" width="100">
        <template #default="{ row }">{{ row.enableTools ? '启用' : '关闭' }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
  </div>
</template>

<style scoped>
.ai-chat-page {
  padding: 24px;
}
</style> 