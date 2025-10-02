<template>
  <div class="app-container">
    <el-card>
      <el-table :data="tagList" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="tagName" label="标签名称" />
        <el-table-column prop="useCount" label="使用次数" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleMerge(row)">合并</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 合并标签对话框 -->
    <el-dialog v-model="mergeDialogVisible" title="合并标签" width="500px">
      <el-form label-width="120px">
        <el-form-item label="源标签">
          <el-input :value="currentTag?.tagName" disabled />
        </el-form-item>
        <el-form-item label="目标标签">
          <el-select v-model="targetTagId" placeholder="请选择目标标签" filterable>
            <el-option
              v-for="tag in tagList.filter(t => t.id !== currentTag?.id)"
              :key="tag.id"
              :label="`${tag.tagName} (${tag.useCount}次)`"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          title="提示：合并后，源标签将被删除，使用源标签的文章将改为使用目标标签"
          type="warning"
          :closable="false"
          show-icon
        />
      </el-form>
      <template #footer>
        <el-button @click="mergeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmMerge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTagList, mergeTags, deleteTag } from '@/api/blog'

const tagList = ref([])
const total = ref(0)
const mergeDialogVisible = ref(false)
const currentTag = ref(null)
const targetTagId = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20
})

const getList = async () => {
  try {
    const res = await getTagList(queryParams)
    tagList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error(error.message || '获取标签列表失败')
  }
}

const handleMerge = (row) => {
  currentTag.value = row
  targetTagId.value = null
  mergeDialogVisible.value = true
}

const confirmMerge = async () => {
  if (!targetTagId.value) {
    ElMessage.warning('请选择目标标签')
    return
  }

  try {
    await ElMessageBox.confirm('确定要合并这两个标签吗？此操作不可恢复', '提示', {
      type: 'warning'
    })
    await mergeTags(currentTag.value.id, targetTagId.value)
    ElMessage.success('合并成功')
    mergeDialogVisible.value = false
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '合并失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该标签吗？', '提示', {
      type: 'warning'
    })
    await deleteTag(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

