<template>
  <div class="editor-container">
    <el-card>
      <el-form :model="form" label-width="100px">
        <el-form-item label="文章标题">
          <el-input v-model="form.title" placeholder="请输入文章标题（1-200字符）" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="文章分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.categoryName"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="文章标签">
          <el-select v-model="form.tags" multiple placeholder="请选择标签（最多5个）">
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.tagName"
              :value="tag.tagName"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="文章封面">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>

        <el-form-item label="文章摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="3"
            placeholder="请输入文章摘要（不填写将自动提取，最多200字）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="文章内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="15"
            placeholder="请输入文章内容（支持Markdown格式，至少50字符）"
          />
        </el-form-item>

        <el-form-item label="是否原创">
          <el-radio-group v-model="form.isOriginal">
            <el-radio :label="1">原创</el-radio>
            <el-radio :label="0">转载</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="success" @click="handlePublish">发布文章（消耗20积分）</el-button>
          <el-button type="primary" @click="handleSaveDraft">保存草稿</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createArticle,
  publishArticle,
  updateArticle,
  getArticleDetail,
  getAllCategories,
  getAllTags
} from '@/api/blog'

const router = useRouter()
const route = useRoute()

const categories = ref([])
const tags = ref([])
const articleId = ref(null)

const form = reactive({
  title: '',
  categoryId: null,
  tags: [],
  coverImage: '',
  summary: '',
  content: '',
  isOriginal: 1
})

const loadCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const loadTags = async () => {
  try {
    tags.value = await getAllTags()
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

const loadArticle = async (id) => {
  try {
    const res = await getArticleDetail(id)
    Object.assign(form, {
      title: res.title,
      categoryId: res.categoryId,
      tags: res.tags || [],
      coverImage: res.coverImage,
      summary: res.summary,
      content: res.content,
      isOriginal: res.isOriginal
    })
    articleId.value = id
  } catch (error) {
    ElMessage.error(error.message || '加载文章失败')
  }
}

const validateForm = () => {
  if (!form.title) {
    ElMessage.warning('请输入文章标题')
    return false
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择文章分类')
    return false
  }
  if (!form.content || form.content.length < 50) {
    ElMessage.warning('文章内容不能少于50个字符')
    return false
  }
  if (form.tags.length > 5) {
    ElMessage.warning('文章标签最多5个')
    return false
  }
  return true
}

const handleSaveDraft = async () => {
  if (!form.title) {
    ElMessage.warning('请至少填写文章标题')
    return
  }

  try {
    if (articleId.value) {
      await updateArticle(articleId.value, form)
      ElMessage.success('草稿保存成功')
    } else {
      const id = await createArticle(form)
      articleId.value = id
      ElMessage.success('草稿保存成功')
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  }
}

const handlePublish = async () => {
  if (!validateForm()) {
    return
  }

  try {
    await ElMessageBox.confirm('发布文章需要消耗20积分，确定要发布吗？', '提示', {
      type: 'warning'
    })

    const data = { ...form }
    if (articleId.value) {
      data.id = articleId.value
    }

    await publishArticle(data)
    ElMessage.success('发布成功')
    router.push('/blog')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发布失败')
    }
  }
}

const handleCancel = () => {
  router.back()
}

onMounted(() => {
  loadCategories()
  loadTags()

  const id = route.params.id
  if (id) {
    loadArticle(id)
  }
})
</script>

<style scoped>
.editor-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}
</style>

