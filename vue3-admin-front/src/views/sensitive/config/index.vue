<template>
  <div class="sensitive-config-page">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 同音字管理 -->
      <el-tab-pane label="同音字管理" name="homophone">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="toolbar">
            <el-row :gutter="10">
              <el-col :span="1.5">
                <el-button type="primary" @click="handleAddHomophone">
                  <el-icon><Plus /></el-icon>
                  新增
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" @click="handleRefreshHomophoneCache">
                  <el-icon><Refresh /></el-icon>
                  刷新缓存
                </el-button>
              </el-col>
            </el-row>
          </div>

          <!-- 数据表格 -->
          <el-table
            v-loading="homophoneLoading"
            :data="homophoneData"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="originalChar" label="原始字符" width="120" />
            <el-table-column prop="homophoneChars" label="同音字列表" min-width="300" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEditHomophone(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteHomophone(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="homophoneQuery.pageNum"
              v-model:page-size="homophoneQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="homophoneTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadHomophones"
              @current-change="loadHomophones"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 形似字管理 -->
      <el-tab-pane label="形似字管理" name="similarChar">
        <div class="tab-content">
          <!-- 操作栏 -->
          <div class="toolbar">
            <el-row :gutter="10">
              <el-col :span="1.5">
                <el-button type="primary" @click="handleAddSimilarChar">
                  <el-icon><Plus /></el-icon>
                  新增
                </el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" @click="handleRefreshSimilarCharCache">
                  <el-icon><Refresh /></el-icon>
                  刷新缓存
                </el-button>
              </el-col>
            </el-row>
          </div>

          <!-- 数据表格 -->
          <el-table
            v-loading="similarCharLoading"
            :data="similarCharData"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="originalChar" label="原始字符" width="120" />
            <el-table-column prop="similarChars" label="形似字列表" min-width="300" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="row.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleEditSimilarChar(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeleteSimilarChar(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="similarCharQuery.pageNum"
              v-model:page-size="similarCharQuery.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="similarCharTotal"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="loadSimilarChars"
              @current-change="loadSimilarChars"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 同音字对话框 -->
    <el-dialog
      v-model="homophoneDialogVisible"
      :title="homophoneDialogTitle"
      width="600px"
      @close="resetHomophoneForm"
    >
      <el-form
        ref="homophoneFormRef"
        :model="homophoneForm"
        :rules="homophoneFormRules"
        label-width="100px"
      >
        <el-form-item label="原始字符" prop="originalChar">
          <el-input
            v-model="homophoneForm.originalChar"
            placeholder="请输入原始字符"
            maxlength="10"
          />
        </el-form-item>
        <el-form-item label="同音字" prop="homophoneChars">
          <el-input
            v-model="homophoneForm.homophoneChars"
            type="textarea"
            :rows="3"
            placeholder="请输入同音字，用逗号分隔，如：沙,煞,啥"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="homophoneForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="homophoneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitHomophone">确定</el-button>
      </template>
    </el-dialog>

    <!-- 形似字对话框 -->
    <el-dialog
      v-model="similarCharDialogVisible"
      :title="similarCharDialogTitle"
      width="600px"
      @close="resetSimilarCharForm"
    >
      <el-form
        ref="similarCharFormRef"
        :model="similarCharForm"
        :rules="similarCharFormRules"
        label-width="100px"
      >
        <el-form-item label="原始字符" prop="originalChar">
          <el-input
            v-model="similarCharForm.originalChar"
            placeholder="请输入原始字符"
            maxlength="10"
          />
        </el-form-item>
        <el-form-item label="形似字" prop="similarChars">
          <el-input
            v-model="similarCharForm.similarChars"
            type="textarea"
            :rows="3"
            placeholder="请输入形似字，用逗号分隔，如：艹,屮"
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="similarCharForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="similarCharDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitSimilarChar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  listHomophones,
  addHomophone,
  updateHomophone,
  deleteHomophone,
  refreshHomophoneCache,
  listSimilarChars,
  addSimilarChar,
  updateSimilarChar,
  deleteSimilarChar,
  refreshSimilarCharCache
} from '@/api/sensitive'

// 标签页
const activeTab = ref('homophone')

// 同音字相关
const homophoneLoading = ref(false)
const homophoneDialogVisible = ref(false)
const homophoneDialogTitle = ref('')
const homophoneData = ref([])
const homophoneTotal = ref(0)
const homophoneFormRef = ref()

const homophoneQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  originalChar: '',
  status: null
})

const homophoneForm = reactive({
  id: null,
  originalChar: '',
  homophoneChars: '',
  status: 1
})

const homophoneFormRules = {
  originalChar: [
    { required: true, message: '请输入原始字符', trigger: 'blur' }
  ],
  homophoneChars: [
    { required: true, message: '请输入同音字', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 形似字相关
const similarCharLoading = ref(false)
const similarCharDialogVisible = ref(false)
const similarCharDialogTitle = ref('')
const similarCharData = ref([])
const similarCharTotal = ref(0)
const similarCharFormRef = ref()

const similarCharQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  originalChar: '',
  status: null
})

const similarCharForm = reactive({
  id: null,
  originalChar: '',
  similarChars: '',
  status: 1
})

const similarCharFormRules = {
  originalChar: [
    { required: true, message: '请输入原始字符', trigger: 'blur' }
  ],
  similarChars: [
    { required: true, message: '请输入形似字', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 同音字方法
const loadHomophones = async () => {
  homophoneLoading.value = true
  try {
    const response = await listHomophones(homophoneQuery)
    homophoneData.value = response.records
    homophoneTotal.value = response.total
  } catch (error) {
    ElMessage.error('查询同音字失败')
  } finally {
    homophoneLoading.value = false
  }
}

const handleAddHomophone = () => {
  homophoneDialogTitle.value = '新增同音字'
  homophoneDialogVisible.value = true
}

const handleEditHomophone = (row) => {
  homophoneDialogTitle.value = '编辑同音字'
  Object.assign(homophoneForm, row)
  homophoneDialogVisible.value = true
}

const handleSubmitHomophone = async () => {
  const valid = await homophoneFormRef.value.validate()
  if (!valid) return

  try {
    if (homophoneForm.id) {
      await updateHomophone(homophoneForm)
      ElMessage.success('更新成功')
    } else {
      await addHomophone(homophoneForm)
      ElMessage.success('新增成功')
    }
    homophoneDialogVisible.value = false
    loadHomophones()
  } catch (error) {
    ElMessage.error(homophoneForm.id ? '更新失败' : '新增失败')
  }
}

const handleDeleteHomophone = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个同音字映射吗？', '提示', {
      type: 'warning'
    })
    await deleteHomophone(row.id)
    ElMessage.success('删除成功')
    loadHomophones()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleRefreshHomophoneCache = async () => {
  try {
    await refreshHomophoneCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

const resetHomophoneForm = () => {
  homophoneForm.id = null
  homophoneForm.originalChar = ''
  homophoneForm.homophoneChars = ''
  homophoneForm.status = 1
  if (homophoneFormRef.value) {
    homophoneFormRef.value.resetFields()
  }
}

// 形似字方法
const loadSimilarChars = async () => {
  similarCharLoading.value = true
  try {
    const response = await listSimilarChars(similarCharQuery)
    similarCharData.value = response.records
    similarCharTotal.value = response.total
  } catch (error) {
    ElMessage.error('查询形似字失败')
  } finally {
    similarCharLoading.value = false
  }
}

const handleAddSimilarChar = () => {
  similarCharDialogTitle.value = '新增形似字'
  similarCharDialogVisible.value = true
}

const handleEditSimilarChar = (row) => {
  similarCharDialogTitle.value = '编辑形似字'
  Object.assign(similarCharForm, row)
  similarCharDialogVisible.value = true
}

const handleSubmitSimilarChar = async () => {
  const valid = await similarCharFormRef.value.validate()
  if (!valid) return

  try {
    if (similarCharForm.id) {
      await updateSimilarChar(similarCharForm)
      ElMessage.success('更新成功')
    } else {
      await addSimilarChar(similarCharForm)
      ElMessage.success('新增成功')
    }
    similarCharDialogVisible.value = false
    loadSimilarChars()
  } catch (error) {
    ElMessage.error(similarCharForm.id ? '更新失败' : '新增失败')
  }
}

const handleDeleteSimilarChar = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个形似字映射吗？', '提示', {
      type: 'warning'
    })
    await deleteSimilarChar(row.id)
    ElMessage.success('删除成功')
    loadSimilarChars()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleRefreshSimilarCharCache = async () => {
  try {
    await refreshSimilarCharCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

const resetSimilarCharForm = () => {
  similarCharForm.id = null
  similarCharForm.originalChar = ''
  similarCharForm.similarChars = ''
  similarCharForm.status = 1
  if (similarCharFormRef.value) {
    similarCharFormRef.value.resetFields()
  }
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'homophone') {
    loadHomophones()
  } else if (newTab === 'similarChar') {
    loadSimilarChars()
  }
})

// 生命周期
onMounted(() => {
  loadHomophones()
})
</script>

<style scoped>
.sensitive-config-page {
  padding: 20px;
}

.tab-content {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}
</style>
