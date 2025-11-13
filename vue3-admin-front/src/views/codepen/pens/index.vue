<template>
  <div class="codepen-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索作品标题或描述"
            clearable
            style="width: 240px"
          />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
            v-model="queryParams.category"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="动画" value="动画" />
            <el-option label="组件" value="组件" />
            <el-option label="游戏" value="游戏" />
            <el-option label="工具" value="工具" />
          </el-select>
        </el-form-item>

        <el-form-item label="类型">
          <el-select
            v-model="queryParams.isFree"
            placeholder="请选择类型"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" :value="null" />
            <el-option label="免费" :value="1" />
            <el-option label="付费" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="全部" :value="null" />
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>作品列表</span>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="title" label="作品标题" width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="viewDetail(row.id)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="userNickname" label="作者" width="120" />

        <el-table-column prop="category" label="分类" width="100" />

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isFree" type="success">免费</el-tag>
            <el-tag v-else type="warning">付费 {{ row.forkPrice }}积分</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="统计数据" width="200">
          <template #default="{ row }">
            <div class="stats-cell">
              <span>浏览: {{ row.viewCount }}</span>
              <span>点赞: {{ row.likeCount }}</span>
              <span>Fork: {{ row.forkCount }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="isRecommend" label="推荐" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isRecommend === 1" type="danger">推荐</el-tag>
            <el-tag v-else type="info">普通</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已发布</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">已下架</el-tag>
            <el-tag v-else type="danger">已删除</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="160" />

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              icon="View"
              @click="viewDetail(row.id)"
            >
              查看
            </el-button>
            
            <el-button
              v-if="row.isRecommend !== 1"
              link
              type="success"
              size="small"
              icon="Star"
              @click="handleRecommend(row)"
            >
              推荐
            </el-button>
            
            <el-button
              v-else
              link
              type="warning"
              size="small"
              icon="StarFilled"
              @click="handleCancelRecommend(row)"
            >
              取消推荐
            </el-button>

            <el-dropdown @command="(command) => handleCommand(command, row)">
              <el-button link type="primary" size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-if="row.status === 1"
                    command="offline"
                    icon="Remove"
                  >
                    下架
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="row.status === 2"
                    command="online"
                    icon="Check"
                  >
                    上架
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" icon="Delete">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 推荐设置对话框 -->
    <el-dialog
      v-model="recommendDialog.visible"
      title="设置推荐"
      width="400px"
    >
      <el-form :model="recommendDialog.form" label-width="100px">
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="recommendDialog.form.expireTime"
            type="datetime"
            placeholder="选择过期时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recommendDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmRecommend" :loading="recommendDialog.loading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { codepenApi } from '@/api/codepen'

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  isFree: null,
  status: null
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 推荐对话框
const recommendDialog = ref({
  visible: false,
  loading: false,
  penId: null,
  form: {
    expireTime: null
  }
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const result = await codepenApi.getPenList(queryParams.value)
    tableData.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.value.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    category: '',
    isFree: null,
    status: null
  }
  loadData()
}

// 查看详情
const viewDetail = (id) => {
  window.open(`/codepen/${id}`, '_blank')
}

// 推荐
const handleRecommend = (row) => {
  recommendDialog.value.visible = true
  recommendDialog.value.penId = row.id
  recommendDialog.value.form.expireTime = null
}

// 确认推荐
const confirmRecommend = async () => {
  try {
    recommendDialog.value.loading = true
    await codepenApi.setRecommend({
      id: recommendDialog.value.penId,
      expireTime: recommendDialog.value.form.expireTime
    })
    
    ElMessage.success('设置推荐成功')
    recommendDialog.value.visible = false
    loadData()
  } catch (error) {
    console.error('设置推荐失败:', error)
  } finally {
    recommendDialog.value.loading = false
  }
}

// 取消推荐
const handleCancelRecommend = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消推荐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await codepenApi.cancelRecommend(row.id)
    ElMessage.success('取消推荐成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消推荐失败:', error)
    }
  }
}

// 更多操作
const handleCommand = async (command, row) => {
  if (command === 'offline') {
    // 下架
    await handleUpdateStatus(row.id, 2, '下架')
  } else if (command === 'online') {
    // 上架
    await handleUpdateStatus(row.id, 1, '上架')
  } else if (command === 'delete') {
    // 删除
    await handleDelete(row)
  }
}

// 更新状态
const handleUpdateStatus = async (id, status, actionName) => {
  try {
    await ElMessageBox.confirm(`确定${actionName}该作品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await codepenApi.updatePenStatus({ id, status })
    ElMessage.success(`${actionName}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionName}失败:`, error)
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除作品"${row.title}"吗？此操作不可恢复。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await codepenApi.deletePen(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.codepen-management {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .stats-cell {
      display: flex;
      flex-direction: column;
      gap: 4px;
      font-size: 12px;
      color: #666;
    }
  }
}
</style>

