<template>
  <div class="resume-editor-page" v-loading="loading">
    <el-page-header @back="router.back()" :content="pageTitle" class="page-header">
      <template #extra>
        <el-button @click="router.push('/resume')">返回列表</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-page-header>

    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card shadow="never">
          <h3 class="card-title">基础信息</h3>
          <el-form :model="form" label-width="90px" class="base-form">
            <el-form-item label="简历名称" required>
              <el-input v-model="form.resumeName" placeholder="如：张三-前端工程师" />
            </el-form-item>
            <el-form-item label="模板" required>
              <el-select v-model="form.templateId" placeholder="请选择模板">
                <el-option
                  v-for="tpl in templates"
                  :key="tpl.id"
                  :label="tpl.name"
                  :value="tpl.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="可见性">
              <el-radio-group v-model="form.visibility">
                <el-radio :value="0">私密</el-radio>
                <el-radio :value="1">公开</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">草稿</el-radio>
                <el-radio :value="1">已发布</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="个人概述">
              <el-input
                v-model="form.summary"
                type="textarea"
                rows="4"
                placeholder="写点定位或亮点，帮助HR快速了解你"
              />
            </el-form-item>
          </el-form>

          <div class="template-info" v-if="currentTemplate">
            <div class="info-header">
              <h4>模板概览</h4>
              <el-tag size="small">{{ currentTemplate.category || '通用' }}</el-tag>
            </div>
            <p class="info-name">{{ currentTemplate.name }}</p>
            <p class="info-desc">{{ currentTemplate.description || '暂无描述' }}</p>
            <p class="info-meta">适用栈：{{ currentTemplate.techStack || '未标注' }}</p>
            <p class="info-meta">经验：{{ formatExperience(currentTemplate.experienceLevel) }}</p>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16">
        <el-card shadow="never">
          <div class="section-header">
            <h3 class="card-title">内容模块</h3>
            <el-dropdown trigger="click">
              <el-button type="primary">
                <el-icon><Plus /></el-icon>
                添加模块
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in sectionTypeOptions"
                    :key="item.value"
                    @click="addSection(item)"
                  >
                    {{ item.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <el-empty description="还没有模块，先添加一个吧" v-if="!form.sections.length" />

          <transition-group name="fade" tag="div">
            <el-card
              v-for="(section, index) in form.sections"
              :key="section.uid"
              class="section-card"
              shadow="hover"
            >
              <div class="section-card-header">
                <div>
                  <el-tag size="small" effect="dark">{{ section.label }}</el-tag>
                  <span class="section-title">{{ section.title || '未命名模块' }}</span>
                </div>
                <div class="section-actions">
                  <el-button-group>
                    <el-button
                      circle
                      :disabled="index === 0"
                      @click="moveSection(index, -1)"
                      title="上移"
                    >
                      <el-icon><ArrowUp /></el-icon>
                    </el-button>
                    <el-button
                      circle
                      :disabled="index === form.sections.length - 1"
                      @click="moveSection(index, 1)"
                      title="下移"
                    >
                      <el-icon><ArrowDown /></el-icon>
                    </el-button>
                  </el-button-group>
                  <el-button type="danger" text @click="removeSection(index)">删除</el-button>
                </div>
              </div>
              <el-form label-width="80px">
                <el-form-item label="模块标题">
                  <el-input v-model="section.title" placeholder="如：教育背景 / 项目经验" />
                </el-form-item>
                <el-form-item label="内容">
                  <el-input
                    v-model="section.content"
                    type="textarea"
                    :rows="6"
                    placeholder="支持分段，建议使用要点式描述"
                  />
                </el-form-item>
              </el-form>
            </el-card>
          </transition-group>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { resumeApi } from '@/api/resume'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const saving = ref(false)
const templates = ref([])
const form = reactive({
  resumeName: '',
  summary: '',
  templateId: null,
  visibility: 0,
  status: 0,
  sections: []
})

const sectionTypeOptions = [
  { label: '个人信息', value: 'PROFILE' },
  { label: '教育背景', value: 'EDUCATION' },
  { label: '工作经验', value: 'WORK' },
  { label: '项目经验', value: 'PROJECT' },
  { label: '技能特长', value: 'SKILL' },
  { label: '荣誉证书', value: 'CERT' },
  { label: '自定义模块', value: 'CUSTOM' }
]

const resumeId = computed(() => route.params.id)
const pageTitle = computed(() => (resumeId.value ? '编辑简历' : '新建简历'))

const currentTemplate = computed(() => {
  return templates.value.find((tpl) => tpl.id === form.templateId)
})

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

const loadTemplates = async () => {
  try {
    const result = await resumeApi.getTemplates({ page: 1, size: 50, status: 1 })
    templates.value = result?.records || []
    if (!form.templateId && route.query.templateId) {
      form.templateId = Number(route.query.templateId)
    }
  } catch (error) {
    ElMessage.error('加载模板失败')
  }
}

const createUid = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`

const loadResume = async () => {
  if (!resumeId.value) return
  loading.value = true
  try {
    const preview = await resumeApi.previewResume(resumeId.value)
    const { resume, sections } = preview
    form.resumeName = resume.resumeName
    form.summary = resume.summary
    form.templateId = resume.templateId
    form.visibility = resume.visibility
    form.status = resume.status
    form.sections = (sections || []).map((item) => ({
      uid: createUid(),
      sectionType: item.sectionType,
      label: getSectionLabel(item.sectionType),
      title: item.title,
      content: item.content
    }))
  } catch (error) {
    ElMessage.error('加载简历失败')
  } finally {
    loading.value = false
  }
}

const addSection = (option) => {
  form.sections.push({
    uid: createUid(),
    sectionType: option.value,
    label: option.label,
    title: option.label,
    content: ''
  })
}

const removeSection = (index) => {
  form.sections.splice(index, 1)
}

const moveSection = (index, offset) => {
  const targetIndex = index + offset
  if (targetIndex < 0 || targetIndex >= form.sections.length) return
  const [item] = form.sections.splice(index, 1)
  form.sections.splice(targetIndex, 0, item)
}

const getSectionLabel = (type) => {
  return sectionTypeOptions.find((item) => item.value === type)?.label || '自定义模块'
}

const validateForm = () => {
  if (!form.resumeName.trim()) {
    ElMessage.warning('请输入简历名称')
    return false
  }
  if (!form.templateId) {
    ElMessage.warning('请选择模板')
    return false
  }
  if (!form.sections.length) {
    ElMessage.warning('请至少添加一个模块')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) return
  saving.value = true
  try {
    const payload = {
      resumeName: form.resumeName.trim(),
      summary: form.summary,
      templateId: form.templateId,
      visibility: form.visibility,
      status: form.status,
      sections: form.sections.map((section, index) => ({
        sectionType: section.sectionType,
        title: section.title,
        content: section.content,
        sortOrder: index + 1,
        status: 1
      }))
    }
    if (resumeId.value) {
      await resumeApi.updateResume(resumeId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await resumeApi.createResume(payload)
      ElMessage.success('创建成功')
    }
    router.push('/resume')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadTemplates()
  await loadResume()
  if (!form.sections.length) {
    // 提供默认模块
    addSection(sectionTypeOptions[0])
    addSection(sectionTypeOptions[2])
    addSection(sectionTypeOptions[4])
  }
})
</script>

<style scoped>
.resume-editor-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.card-title {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.base-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.template-info {
  margin-top: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-name {
  margin: 12px 0 4px;
  font-weight: 600;
}

.info-desc {
  margin: 0 0 6px;
  color: #606266;
  font-size: 13px;
}

.info-meta {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.section-card {
  margin-bottom: 16px;
}

.section-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.section-title {
  margin-left: 8px;
  font-weight: 600;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 768px) {
  .resume-editor-page {
    padding: 16px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
