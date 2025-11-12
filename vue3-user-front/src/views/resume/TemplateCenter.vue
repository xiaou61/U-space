<template>
  <div class="resume-template-page">
    <el-card class="filter-card" shadow="never">
      <div class="filter-header">
        <div>
          <h2>简历模板中心</h2>
          <p class="sub-title">挑选适合的模板开始创作你的专属简历</p>
        </div>
        <el-button type="primary" @click="goMyResumes">
          <el-icon><Briefcase /></el-icon>
          我的简历
        </el-button>
      </div>
      <el-form :inline="true" :model="filters" class="filter-form" label-width="80px">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" placeholder="输入模板名称或标签" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" placeholder="全部分类" clearable>
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="经验">
          <el-select v-model="filters.experienceLevel" placeholder="经验层级" clearable>
            <el-option
              v-for="item in experienceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="template-grid" v-loading="loading">
      <el-empty v-if="!templates.length && !loading" description="暂无模板，稍后再来~" />
      <template v-else>
        <el-card
          v-for="template in templates"
          :key="template.id"
          class="template-card"
          shadow="hover"
        >
          <div class="cover" @click="handleUseTemplate(template)">
            <img :src="template.coverUrl || defaultCover" alt="模板封面" />
            <div class="cover-mask">
              <el-button type="primary" size="small">使用模板</el-button>
            </div>
          </div>
          <div class="card-body">
            <div class="title-row">
              <h3>{{ template.name }}</h3>
              <el-tag type="success" size="small">{{ formatExperience(template.experienceLevel) }}</el-tag>
            </div>
            <p class="desc">{{ template.description || '暂无描述' }}</p>
            <div class="meta-row">
              <div class="meta-item">
                <el-icon><CollectionTag /></el-icon>
                {{ template.category || '通用' }}
              </div>
              <div class="meta-item">
                <el-rate
                  v-model="template.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </div>
            </div>
            <div class="tags" v-if="template.tags">
              <el-tag
                v-for="tag in parseTags(template.tags)"
                :key="tag"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div class="card-actions">
              <el-button link type="primary" @click="previewTemplate(template)">查看详情</el-button>
              <el-button type="primary" @click="handleUseTemplate(template)">
                <el-icon><EditPen /></el-icon>
                立即使用
              </el-button>
            </div>
          </div>
        </el-card>
      </template>
    </div>

    <div class="pagination-wrapper" v-if="pagination.total > pagination.size">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[8, 12, 16, 24]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="previewVisible" title="模板说明" width="520px">
      <div v-if="currentTemplate">
        <div class="preview-cover">
          <img :src="currentTemplate.previewUrl || currentTemplate.coverUrl || defaultCover" alt="模板预览" />
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="模板名称">{{ currentTemplate.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentTemplate.category || '通用' }}</el-descriptions-item>
          <el-descriptions-item label="经验层级">{{ formatExperience(currentTemplate.experienceLevel) }}</el-descriptions-item>
          <el-descriptions-item label="适用技术栈">
            {{ currentTemplate.techStack || '未标注' }}
          </el-descriptions-item>
          <el-descriptions-item label="简介">
            {{ currentTemplate.description || '暂无介绍' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleUseTemplate(currentTemplate)">立即使用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Briefcase, CollectionTag, EditPen } from '@element-plus/icons-vue'
import { resumeApi } from '@/api/resume'

const router = useRouter()
const loading = ref(false)
const templates = ref([])
const currentTemplate = ref(null)
const previewVisible = ref(false)

const pagination = reactive({
  page: 1,
  size: 8,
  total: 0
})

const filters = reactive({
  keyword: '',
  category: '',
  experienceLevel: ''
})

const categoryOptions = [
  { label: '前端', value: '前端' },
  { label: '后端', value: '后端' },
  { label: '全栈', value: '全栈' },
  { label: '算法', value: '算法' },
  { label: '移动端', value: '移动端' },
  { label: '测试', value: '测试' }
]

const experienceOptions = [
  { label: '应届生/实习', value: 1 },
  { label: '初级工程师', value: 2 },
  { label: '中级工程师', value: 3 },
  { label: '高级工程师', value: 4 },
  { label: '专家/管理', value: 5 }
]

const defaultCover = 'https://cdn.xiaou.tech/static/resume-placeholder.png'

const fetchTemplates = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      experienceLevel: filters.experienceLevel || undefined,
      status: 1
    }
    const result = await resumeApi.getTemplates(params)
    templates.value = result?.records || []
    pagination.total = result?.total || templates.value.length
  } catch (error) {
    ElMessage.error('加载模板失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchTemplates()
}

const handleReset = () => {
  filters.keyword = ''
  filters.category = ''
  filters.experienceLevel = ''
  pagination.page = 1
  fetchTemplates()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchTemplates()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchTemplates()
}

const parseTags = (tags) => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(Boolean)
}

const formatExperience = (level) => {
  const map = {
    1: '应届/实习',
    2: '初级',
    3: '中级',
    4: '高级',
    5: '专家'
  }
  return map[level] || '通用'
}

const handleUseTemplate = (template) => {
  if (!template?.id) {
    ElMessage.warning('模板信息异常')
    return
  }
  router.push({
    path: '/resume/editor',
    query: {
      templateId: template.id
    }
  })
}

const previewTemplate = (template) => {
  currentTemplate.value = template
  previewVisible.value = true
}

const goMyResumes = () => {
  router.push('/resume')
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.resume-template-page {
  padding: 24px 24px 48px;
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 24px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2d3d;
}

.sub-title {
  margin: 4px 0 0;
  color: #909399;
  font-size: 14px;
}

.filter-form {
  margin-top: 8px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.template-card {
  display: flex;
  flex-direction: column;
}

.cover {
  position: relative;
  height: 180px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.cover:hover img {
  transform: scale(1.05);
}

.cover-mask {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cover:hover .cover-mask {
  opacity: 1;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 12px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.title-row h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2d3d;
}

.desc {
  flex: none;
  margin: 0 0 8px;
  color: #606266;
  font-size: 13px;
  min-height: 40px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.preview-cover {
  width: 100%;
  height: 260px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
  background: #f5f7fa;
}

.preview-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .filter-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .filter-form {
    flex-direction: column;
  }
  
  .template-grid {
    grid-template-columns: 1fr;
  }
}
</style>
