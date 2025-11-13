<template>
  <div class="tag-management">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>标签列表</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">
            新建标签
          </el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="tagName" label="标签名称" width="200">
          <template #default="{ row }">
            <el-tag>{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="tagDescription" label="标签描述" min-width="300">
          <template #default="{ row }">
            <span class="description-text">{{ row.tagDescription || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="useCount" label="使用次数" width="120" sortable />

        <el-table-column prop="createTime" label="创建时间" width="160" />

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="warning"
              size="small"
              icon="Connection"
              @click="handleMerge(row)"
            >
              合并
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑标签对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="标签名称" required>
          <el-input
            v-model="formData.tagName"
            placeholder="请输入标签名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签描述">
          <el-input
            v-model="formData.tagDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入标签描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 合并标签对话框 -->
    <el-dialog
      v-model="mergeDialogVisible"
      title="合并标签"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="合并说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>将源标签的所有作品合并到目标标签，然后删除源标签。</p>
      </el-alert>

      <el-form :model="mergeFormData" label-width="100px">
        <el-form-item label="源标签">
          <el-input :value="mergeFormData.sourceTag" readonly />
        </el-form-item>

        <el-form-item label="目标标签" required>
          <el-select
            v-model="mergeFormData.targetId"
            placeholder="请选择目标标签"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="tag in targetTags"
              :key="tag.id"
              :label="tag.tagName"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="mergeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMergeSubmit" :loading="merging">
          确定合并
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { codepenApi } from '@/api/codepen'

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 新建/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建标签')
const submitting = ref(false)
const formData = ref({
  id: null,
  tagName: '',
  tagDescription: ''
})

// 合并对话框
const mergeDialogVisible = ref(false)
const merging = ref(false)
const mergeFormData = ref({
  sourceId: null,
  sourceTag: '',
  targetId: null
})

// 目标标签列表（排除源标签）
const targetTags = computed(() => {
  return tableData.value.filter(tag => tag.id !== mergeFormData.value.sourceId)
})

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const result = await codepenApi.getTagList()
    tableData.value = result || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 新建
const handleAdd = () => {
  dialogTitle.value = '新建标签'
  formData.value = {
    id: null,
    tagName: '',
    tagDescription: ''
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑标签'
  formData.value = {
    id: row.id,
    tagName: row.tagName,
    tagDescription: row.tagDescription
  }
  dialogVisible.value = true
}

// 合并
const handleMerge = (row) => {
  mergeFormData.value = {
    sourceId: row.id,
    sourceTag: row.tagName,
    targetId: null
  }
  mergeDialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  if (row.useCount > 0) {
    ElMessage.warning('该标签正在使用中，无法删除。请先合并到其他标签或等待作品更新。')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定删除标签"${row.tagName}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await codepenApi.deleteTag(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formData.value.tagName) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    submitting.value = true
    
    if (formData.value.id) {
      await codepenApi.updateTag(formData.value)
      ElMessage.success('更新成功')
    } else {
      await codepenApi.createTag(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 合并提交
const handleMergeSubmit = async () => {
  if (!mergeFormData.value.targetId) {
    ElMessage.warning('请选择目标标签')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将标签"${mergeFormData.value.sourceTag}"合并到目标标签吗？此操作不可恢复。`,
      '确认合并',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    merging.value = true
    await codepenApi.mergeTags({
      sourceId: mergeFormData.value.sourceId,
      targetId: mergeFormData.value.targetId
    })
    
    ElMessage.success('合并成功')
    mergeDialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('合并失败:', error)
    }
  } finally {
    merging.value = false
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.tag-management {
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .description-text {
      color: #666;
      font-size: 14px;
    }
  }
}
</style>

