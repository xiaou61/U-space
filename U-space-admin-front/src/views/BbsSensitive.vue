<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSensitiveWords, addSensitiveWord, deleteSensitiveWord } from '../api/bbsSensitive'

const loading = ref(false)
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listSensitiveWords()
    const mapData = res.data || res.msg || {}
    // Map<String,Integer> -> array
    tableData.value = Object.entries(mapData).map(([word, level]) => ({ word, level: Number(level) }))
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// add dialog
const dialogVisible = ref(false)
const form = reactive({ word: '', level: 1 })

const openAdd = () => {
  Object.assign(form, { word: '', level: 1 })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.word) {
    ElMessage.warning('请输入敏感词')
    return
  }
  try {
    await addSensitiveWord({ word: form.word, level: form.level })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除敏感词：${row.word} ?`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteSensitiveWord(row.word)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}
</script>

<template>
  <div class="sensitive-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">新增敏感词</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="width: 100%">
      <el-table-column prop="word" label="敏感词" />
      <el-table-column prop="level" label="级别">
        <template #default="{ row }">
          {{ row.level === 1 ? '禁止发布' : '替换' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增敏感词" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="敏感词"><el-input v-model="form.word" /></el-form-item>
        <el-form-item label="级别">
          <el-radio-group v-model="form.level">
            <el-radio :label="1">禁止发布</el-radio>
            <el-radio :label="2">替换</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.sensitive-page { padding: 24px; }
.toolbar { margin-bottom: 16px; }
</style> 