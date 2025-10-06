<template>
  <div class="app-container">
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable style="width: 200px">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
            <el-option label="已删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="标题/内容搜索" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="mt-3">
      <el-table :data="articleList" border stripe>
        <el-table-column prop="id" label="文章ID" width="80" />
        <el-table-column prop="userNickname" label="作者" width="120" />
        <el-table-column prop="title" label="文章标题" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">已下架</el-tag>
            <el-tag v-else-if="row.status === 3" type="danger">已删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="warning">置顶</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isTop === 0"
              link
              type="primary"
              size="small"
              @click="handleTop(row)"
            >
              置顶
            </el-button>
            <el-button
              v-else
              link
              type="warning"
              size="small"
              @click="handleCancelTop(row)"
            >
              取消置顶
            </el-button>
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              size="small"
              @click="handleUpdateStatus(row, 2)"
            >
              下架
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </el-card>

    <!-- 置顶对话框 -->
    <el-dialog v-model="topDialogVisible" title="设置置顶" width="400px">
      <el-form :model="topForm">
        <el-form-item label="置顶天数">
          <el-input-number v-model="topForm.duration" :min="1" :max="365" placeholder="请输入天数" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTop">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getArticleList, topArticle, cancelTop, updateArticleStatus, deleteArticle } from '@/api/blog'
import { getCategoryList } from '@/api/blog'

const articleList = ref([])
const categories = ref([])
const total = ref(0)
const topDialogVisible = ref(false)
const currentRow = ref(null)

const queryParams = reactive({
  userId: null,
  categoryId: null,
  status: null,
  keyword: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 20
})

const topForm = reactive({
  duration: 7
})

const getList = async () => {
  try {
    const res = await getArticleList(queryParams)
    articleList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error(error.message || '获取文章列表失败')
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList({ pageNum: 1, pageSize: 100 })
    categories.value = res.records
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    userId: null,
    categoryId: null,
    status: null,
    keyword: '',
    startTime: '',
    endTime: '',
    pageNum: 1,
    pageSize: 20
  })
  getList()
}

const handleTop = (row) => {
  currentRow.value = row
  topDialogVisible.value = true
}

const confirmTop = async () => {
  try {
    await topArticle(currentRow.value.id, topForm.duration)
    ElMessage.success('置顶成功')
    topDialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error(error.message || '置顶失败')
  }
}

const handleCancelTop = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消置顶吗？', '提示', {
      type: 'warning'
    })
    await cancelTop(row.id)
    ElMessage.success('取消置顶成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消置顶失败')
    }
  }
}

const handleUpdateStatus = async (row, status) => {
  try {
    const action = status === 2 ? '下架' : '恢复'
    await ElMessageBox.confirm(`确定要${action}该文章吗？`, '提示', {
      type: 'warning'
    })
    await updateArticleStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '提示', {
      type: 'warning'
    })
    await deleteArticle(row.id)
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
  loadCategories()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.mt-3 {
  margin-top: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

