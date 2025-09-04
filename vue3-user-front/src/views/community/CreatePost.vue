<template>
  <div class="create-post-page">
    <!-- 头部导航 -->
    <div class="header">
      <div class="header-content">
        <div class="header-left">
          <h2>创作帖子</h2>
          <p>使用Markdown格式，让您的内容更加丰富多彩</p>
        </div>
        <div class="header-right">
          <el-button @click="goBack" :icon="Back">
            返回社区
          </el-button>
          <el-button type="primary" @click="handlePublish" :loading="publishLoading" :icon="Edit">
            发表帖子
          </el-button>
        </div>
      </div>
    </div>

    <!-- 标题和分类输入 -->
    <div class="title-section">
      <el-card shadow="never" class="title-card">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-input 
              v-model="postForm.title" 
              placeholder="请输入帖子标题..." 
              size="large"
              maxlength="200"
              show-word-limit
              clearable
            />
          </el-col>
          <el-col :span="8">
            <el-select 
              v-model="postForm.categoryId" 
              placeholder="选择分类（可选）"
              size="large"
              clearable
              style="width: 100%"
            >
              <template #prefix>
                <el-icon><Flag /></el-icon>
              </template>
              <el-option
                v-for="category in categoryList"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              >
                <span>{{ category.name }}</span>
                <span style="color: #8492a6; font-size: 13px; margin-left: 8px">{{ category.description }}</span>
              </el-option>
            </el-select>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 编辑器主体 -->
    <div class="editor-section">
      <el-card shadow="never" class="editor-card">
        <div class="editor-container">
          <!-- 左侧编辑区域 -->
          <div class="editor-panel">
            <div class="editor-header">
              <h3>Markdown编辑器</h3>
              <div class="editor-tools">
                <el-button size="small" @click="insertMarkdown('**', '**')" :icon="EditPen">
                  加粗
                </el-button>
                <el-button size="small" @click="insertMarkdown('*', '*')" :icon="More">
                  斜体
                </el-button>
                <el-button size="small" @click="insertMarkdown('`', '`')" :icon="Flag">
                  代码
                </el-button>
                <el-button size="small" @click="insertMarkdown('### ', '')" :icon="Menu">
                  标题
                </el-button>
              </div>
            </div>
            <el-input
              v-model="postForm.content"
              type="textarea"
              placeholder="在这里使用Markdown格式编写您的帖子内容...

示例：
# 一级标题
## 二级标题
### 三级标题

**粗体文本**
*斜体文本*
`行内代码`

```javascript
// 代码块
console.log('Hello World!');
```

- 无序列表项1
- 无序列表项2

1. 有序列表项1
2. 有序列表项2

> 引用文本

[链接文本](https://example.com)

| 表头1 | 表头2 |
|-------|-------|
| 单元格1 | 单元格2 |
"
              class="markdown-editor"
              :rows="25"
              maxlength="5000"
              show-word-limit
              @input="updatePreview"
            />
          </div>

          <!-- 右侧预览区域 -->
          <div class="preview-panel">
            <div class="preview-header">
              <h3>实时预览</h3>
              <el-tag size="small" type="info">{{ previewWordCount }} 字符</el-tag>
            </div>
            <div class="preview-content markdown-content" v-html="previewHtml"></div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Back, Edit, EditPen, More, Flag, Menu
} from '@element-plus/icons-vue'
import { communityApi } from '@/api/community'
import { renderMarkdown } from '@/utils/markdown'

const router = useRouter()

// 响应式数据
const publishLoading = ref(false)

// 表单数据
const postForm = reactive({
  title: '',
  content: '',
  categoryId: null
})

// 分类列表
const categoryList = ref([])

// 计算属性 - 预览内容
const previewHtml = computed(() => {
  if (!postForm.content) {
    return '<div class="empty-preview">在左侧编辑器中输入内容，右侧将显示实时预览...</div>'
  }
  return renderMarkdown(postForm.content)
})

// 计算属性 - 预览字符数
const previewWordCount = computed(() => {
  return postForm.content.length
})

// 更新预览 (这个函数现在由computed自动处理，保留是为了未来扩展)
const updatePreview = () => {
  // 由于使用了computed，这里可以做一些其他处理
  // 比如同步滚动等功能
}

// 插入Markdown标记
const insertMarkdown = (before, after) => {
  const textarea = document.querySelector('.markdown-editor textarea')
  if (!textarea) return
  
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = postForm.content.substring(start, end)
  
  const replacement = before + selectedText + after
  postForm.content = postForm.content.substring(0, start) + replacement + postForm.content.substring(end)
  
  // 重新设置光标位置
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + before.length, start + before.length + selectedText.length)
  })
}

// 发表帖子
const handlePublish = async () => {
  // 验证表单
  if (!postForm.title.trim()) {
    ElMessage.error('请输入帖子标题')
    return
  }
  if (postForm.title.length < 5 || postForm.title.length > 200) {
    ElMessage.error('标题长度应为5-200个字符')
    return
  }

  if (!postForm.content.trim()) {
    ElMessage.error('请输入帖子内容')
    return
  }
  if (postForm.content.length < 10 || postForm.content.length > 5000) {
    ElMessage.error('内容长度应为10-5000个字符')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认发表这篇帖子吗？发表后将在社区中展示。',
      '确认发表',
      {
        confirmButtonText: '确认发表',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    publishLoading.value = true
    
    await communityApi.createPost(postForm)
    ElMessage.success('发表成功！')
    
    // 跳转到社区首页
    router.push('/community')
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发表失败')
    }
  } finally {
    publishLoading.value = false
  }
}

// 返回社区
const goBack = () => {
  if (postForm.title.trim() || postForm.content.trim() || postForm.categoryId) {
    ElMessageBox.confirm(
      '您有未保存的内容，确定要离开吗？',
      '确认离开',
      {
        confirmButtonText: '确认离开',
        cancelButtonText: '继续编辑',
        type: 'warning'
      }
    ).then(() => {
      router.push('/community')
    }).catch(() => {
      // 用户取消，什么都不做
    })
  } else {
    router.push('/community')
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await communityApi.getEnabledCategories()
    categoryList.value = response || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.create-post-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #00b894 0%, #00a085 100%);
  color: white;
  padding: 20px 30px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 185, 148, 0.3);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.header-left p {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.title-section {
  margin-bottom: 20px;
}

.title-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.editor-section {
  margin-bottom: 20px;
}

.editor-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 200px);
}

.editor-container {
  display: flex;
  gap: 20px;
  height: 100%;
}

.editor-panel,
.preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-header,
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 16px;
}

.editor-header h3,
.preview-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.editor-tools {
  display: flex;
  gap: 8px;
}

:deep(.markdown-editor .el-textarea__inner) {
  min-height: 600px !important;
  font-family: 'Consolas', 'Monaco', 'SF Mono', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  resize: none;
}

:deep(.markdown-editor .el-textarea__inner:focus) {
  border-color: #00b894;
  box-shadow: 0 0 0 2px rgba(0, 185, 148, 0.2);
}

.preview-content {
  min-height: 600px;
  max-height: 600px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: white;
  padding: 0; /* markdown-content class already has padding */
}

.empty-preview {
  color: #909399;
  text-align: center;
  padding: 50px 20px;
  font-style: italic;
}

/* 自定义滚动条 */
.preview-content::-webkit-scrollbar {
  width: 8px;
}

.preview-content::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 4px;
}

.preview-content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.preview-content::-webkit-scrollbar-thumb:hover {
  background: #a8abb2;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .editor-container {
    flex-direction: column;
  }
  
  .editor-panel,
  .preview-panel {
    flex: none;
  }
  
  :deep(.markdown-editor .el-textarea__inner) {
    min-height: 300px !important;
  }
  
  .preview-content {
    min-height: 300px;
    max-height: 400px;
  }
}

@media (max-width: 768px) {
  .create-post-page {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .editor-tools {
    flex-wrap: wrap;
    gap: 6px;
  }
}
</style> 