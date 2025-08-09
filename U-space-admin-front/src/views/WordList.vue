<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { addWord, addBatchWord, updateWord, deleteWord, listWordsPage, exportAllWords } from '../api/word'
import * as XLSX from 'xlsx'

const loading = ref(false)
const tableData = ref([])
const pageInfo = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  sortField: 'created_at',
  desc: true
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listWordsPage({
      pageNum: pageInfo.pageNum,
      pageSize: pageInfo.pageSize,
      sortField: pageInfo.sortField,
      desc: pageInfo.desc
    })
    if (res.data) {
      tableData.value = res.data.list || []
      pageInfo.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error('获取单词列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

// 单个添加对话框
const dialogVisible = ref(false)
const editMode = ref(false)
const currentId = ref('')
const form = reactive({
  word: '',
  definition: '',
  phonetic: '',
  exampleSentence: '',
  partOfSpeech: '',
  definitionEn: '',
  source: '',
  tags: '',
  difficulty: 1,
  status: 1
})

const openAdd = () => {
  editMode.value = false
  Object.assign(form, {
    word: '',
    definition: '',
    phonetic: '',
    exampleSentence: '',
    partOfSpeech: '',
    definitionEn: '',
    source: '',
    tags: '',
    difficulty: 1,
    status: 1
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  editMode.value = true
  currentId.value = row.id
  Object.assign(form, {
    word: row.word || '',
    definition: row.definition || '',
    phonetic: row.phonetic || '',
    exampleSentence: row.exampleSentence || '',
    partOfSpeech: row.partOfSpeech || '',
    definitionEn: row.definitionEn || '',
    source: row.source || '',
    tags: row.tags || '',
    difficulty: row.difficulty || 1,
    status: row.status || 1
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.word || !form.definition) {
    ElMessage.warning('请填写单词和含义')
    return
  }
  try {
    if (editMode.value) {
      await updateWord(currentId.value, form)
      ElMessage.success('修改成功')
    } else {
      await addWord(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(editMode.value ? '修改失败' : '添加失败')
  }
}

// Excel导入对话框
const importDialogVisible = ref(false)
const uploadRef = ref()
const fileList = ref([])

const openImport = () => {
  fileList.value = []
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  fileList.value = [file]
  return false // 阻止自动上传
}

const handleFileRemove = () => {
  fileList.value = []
}

const submitImport = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要导入的Excel文件')
    return
  }
  
  try {
    // 读取Excel文件
    const file = fileList.value[0].raw
    const arrayBuffer = await file.arrayBuffer()
    const workbook = XLSX.read(arrayBuffer, { type: 'array' })
    const worksheet = workbook.Sheets[workbook.SheetNames[0]]
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
    
    if (jsonData.length < 2) {
      ElMessage.warning('Excel文件数据为空')
      return
    }
    
    // 跳过表头，处理数据行
    const dataRows = jsonData.slice(1)
    const wordList = dataRows.map(row => {
      // 处理难度等级转换
      let difficulty = 1
      if (row[8]) {
        const difficultyStr = String(row[8])
        if (difficultyStr.includes('1') || difficultyStr.includes('简单')) difficulty = 1
        else if (difficultyStr.includes('2') || difficultyStr.includes('较易')) difficulty = 2
        else if (difficultyStr.includes('3') || difficultyStr.includes('中等')) difficulty = 3
        else if (difficultyStr.includes('4') || difficultyStr.includes('较难')) difficulty = 4
        else if (difficultyStr.includes('5') || difficultyStr.includes('困难')) difficulty = 5
        else difficulty = parseInt(difficultyStr) || 1
      }
      
      // 处理状态转换
      let status = 1
      if (row[9]) {
        const statusStr = String(row[9])
        if (statusStr.includes('禁用') || statusStr.includes('0')) status = 0
        else status = 1
      }
      
      return {
        word: row[0] || '',
        definition: row[1] || '',
        phonetic: row[2] || '',
        partOfSpeech: row[3] || '',
        definitionEn: row[4] || '',
        exampleSentence: row[5] || '',
        source: row[6] || '',
        tags: row[7] || '',
        difficulty: difficulty,
        status: status
      }
    }).filter(item => item.word && item.definition) // 过滤无效数据
    
    if (wordList.length === 0) {
      ElMessage.warning('没有有效的单词数据')
      return
    }
    
    // 发送到后端
    const res = await addBatchWord(wordList)
    if (res.data) {
      const result = res.data
      const successCount = result.success?.length || 0
      const failCount = result.fail?.length || 0
      
      if (successCount > 0 && failCount > 0) {
        ElMessage.success(`导入完成：成功 ${successCount} 个，失败 ${failCount} 个（重复单词）`)
      } else if (successCount > 0) {
        ElMessage.success(`导入成功：${successCount} 个单词`)
      } else {
        ElMessage.warning('所有单词都已存在，未导入任何新单词')
      }
    } else {
      ElMessage.success('导入成功')
    }
    importDialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error('导入失败:', e)
    ElMessage.error('导入失败：' + (e.message || '未知错误'))
  }
}

const handleDownloadTemplate = () => {
  try {
    // 创建Excel工作簿
    const wb = XLSX.utils.book_new()
    
    // 定义表头
    const headers = [
      '单词*', 
      '中文释义*', 
      '音标', 
      '词性', 
      '英文释义', 
      '例句', 
      '词库来源', 
      '标签', 
      '难度等级', 
      '状态'
    ]
    
    // 创建示例数据（可选，帮助用户理解格式）
    const exampleData = [
      ['apple', '苹果', '/ˈæpəl/', 'n.', 'A round fruit', 'I like to eat an apple.', 'CET-4', '常见,水果', '1级(简单)', '启用'],
      ['book', '书', '/bʊk/', 'n.', 'A written work', 'I read a book every day.', 'CET-4', '常见,学习', '1级(简单)', '启用']
    ]
    
    // 合并表头和示例数据
    const wsData = [headers, ...exampleData]
    
    // 创建工作表
    const ws = XLSX.utils.aoa_to_sheet(wsData)
    
    // 设置列宽
    const colWidths = [
      { wch: 15 }, // 单词
      { wch: 20 }, // 中文释义
      { wch: 15 }, // 音标
      { wch: 10 }, // 词性
      { wch: 30 }, // 英文释义
      { wch: 40 }, // 例句
      { wch: 15 }, // 词库来源
      { wch: 20 }, // 标签
      { wch: 15 }, // 难度等级
      { wch: 10 }  // 状态
    ]
    ws['!cols'] = colWidths
    
    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(wb, ws, '单词数据')
    
    // 生成Excel文件并下载
    XLSX.writeFile(wb, '单词导入模板.xlsx')
    
    ElMessage.success('模板下载成功')
  } catch (e) {
    console.error('模板生成失败:', e)
    ElMessage.error('模板下载失败')
  }
}

const handleExportWords = async () => {
  try {
    ElMessage.info('正在导出，请稍等...')
    const res = await exportAllWords()
    // 创建下载链接
    const blob = new Blob([res], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名（包含导出时间）
    const now = new Date()
    const dateStr = now.toLocaleDateString('zh-CN').replace(/\//g, '-')
    const timeStr = now.toLocaleTimeString('zh-CN', { hour12: false }).replace(/:/g, '-')
    link.download = `单词数据导出_${dateStr}_${timeStr}.xlsx`
    
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// 删除功能
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除这个单词吗?', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteWord(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 分页处理
const handleSizeChange = (val) => {
  pageInfo.pageSize = val
  pageInfo.pageNum = 1
  fetchData()
}

const handleCurrentChange = (val) => {
  pageInfo.pageNum = val
  fetchData()
}

// 排序处理
const handleSortChange = ({ column, prop, order }) => {
  if (order) {
    // 转换字段名：驼峰 -> 下划线
    const fieldMap = {
      'createdAt': 'created_at',
      'updatedAt': 'updated_at',
      'word': 'word',
      'id': 'id',
      'difficulty': 'difficulty'
    }
    pageInfo.sortField = fieldMap[prop] || 'created_at'
    pageInfo.desc = order === 'descending'
  } else {
    pageInfo.sortField = 'created_at'
    pageInfo.desc = true
  }
  pageInfo.pageNum = 1
  fetchData()
}

// 难度标签颜色
const getDifficultyType = (difficulty) => {
  const types = ['success', 'info', 'warning', 'warning', 'danger']
  return types[difficulty - 1] || 'info'
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}
</script>

<template>
  <div class="word-page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd">添加单词</el-button>
      <el-button type="success" @click="openImport">Excel导入</el-button>
      <el-button type="info" @click="handleDownloadTemplate">下载模板</el-button>
      <el-button type="warning" @click="handleExportWords">导出Excel</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width: 100%" @sort-change="handleSortChange">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="word" label="单词" width="120" sortable="custom" />
      <el-table-column prop="definition" label="中文释义" width="150" />
      <el-table-column prop="phonetic" label="音标" width="120" />
      <el-table-column prop="partOfSpeech" label="词性" width="80" />
      <el-table-column prop="exampleSentence" label="例句" min-width="200" show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="getDifficultyType(row.difficulty)">{{ row.difficulty }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" sortable="custom">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pageInfo.pageNum"
        v-model:page-size="pageInfo.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pageInfo.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>

  <!-- 单个添加/编辑对话框 -->
  <el-dialog :title="editMode ? '编辑单词' : '添加单词'" v-model="dialogVisible" width="600px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="单词" required>
        <el-input v-model="form.word" placeholder="请输入单词" />
      </el-form-item>
      <el-form-item label="中文释义" required>
        <el-input v-model="form.definition" type="textarea" rows="2" placeholder="请输入中文释义" />
      </el-form-item>
      <el-form-item label="音标">
        <el-input v-model="form.phonetic" placeholder="请输入音标，如 /ˈæpəl/" />
      </el-form-item>
      <el-form-item label="词性">
        <el-select v-model="form.partOfSpeech" placeholder="请选择词性" clearable>
          <el-option label="名词 (n.)" value="n." />
          <el-option label="动词 (v.)" value="v." />
          <el-option label="形容词 (adj.)" value="adj." />
          <el-option label="副词 (adv.)" value="adv." />
          <el-option label="介词 (prep.)" value="prep." />
          <el-option label="连词 (conj.)" value="conj." />
          <el-option label="代词 (pron.)" value="pron." />
          <el-option label="感叹词 (int.)" value="int." />
        </el-select>
      </el-form-item>
      <el-form-item label="英文释义">
        <el-input v-model="form.definitionEn" type="textarea" rows="2" placeholder="请输入英文释义" />
      </el-form-item>
      <el-form-item label="例句">
        <el-input v-model="form.exampleSentence" type="textarea" rows="3" placeholder="请输入例句" />
      </el-form-item>
      <el-form-item label="词库来源">
        <el-input v-model="form.source" placeholder="如：CET-4, CET-6, GRE, TOEFL" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="用逗号分隔，如：常考,高频" />
      </el-form-item>
      <el-form-item label="难度等级">
        <el-select v-model="form.difficulty" placeholder="请选择难度">
          <el-option label="1级 (简单)" :value="1" />
          <el-option label="2级 (较易)" :value="2" />
          <el-option label="3级 (中等)" :value="3" />
          <el-option label="4级 (较难)" :value="4" />
          <el-option label="5级 (困难)" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>

  <!-- Excel导入对话框 -->
  <el-dialog title="Excel导入单词" v-model="importDialogVisible" width="600px">
    <el-form label-width="100px">
      <el-form-item label="导入说明">
        <div style="color: #666; font-size: 14px; line-height: 1.6;">
          1. 请先下载导入模板，按照模板格式填写单词数据<br>
          2. 必填字段：单词、中文释义<br>
          3. 支持的文件格式：.xlsx<br>
          4. 导入时会自动去重，重复的单词不会覆盖
        </div>
      </el-form-item>
      <el-form-item label="选择文件">
                 <el-upload
           ref="uploadRef"
           v-model:file-list="fileList"
           :auto-upload="false"
           :limit="1"
           accept=".xlsx,.xls"
           :before-upload="handleFileChange"
           :on-remove="handleFileRemove"
           drag
         >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将Excel文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/xls文件，且不超过10MB
            </div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="importDialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submitImport">开始导入</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.word-page {
  padding: 24px;
}
.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 