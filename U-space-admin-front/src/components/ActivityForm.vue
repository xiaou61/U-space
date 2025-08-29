<template>
  <div class="activity-form">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      label-position="right"
    >
      <el-row :gutter="20">
        <!-- 左侧列 -->
        <el-col :span="12">
          <el-form-item label="活动标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入活动标题"
              maxlength="100"
              show-word-limit
              clearable
            />
          </el-form-item>

          <el-form-item label="活动描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              placeholder="请输入活动描述"
              :rows="3"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="活动类型" prop="activityType">
            <el-select
              v-model="form.activityType"
              placeholder="请选择活动类型"
              style="width: 100%"
            >
              <el-option label="抢夺型" :value="1">
                <div>
                  <span style="font-weight: bold;">抢夺型</span>
                  <div style="font-size: 12px; color: #999;">用户需要抢夺参与名额</div>
                </div>
              </el-option>
              <el-option label="签到型" :value="2">
                <div>
                  <span style="font-weight: bold;">签到型</span>
                  <div style="font-size: 12px; color: #999;">用户签到参与活动</div>
                </div>
              </el-option>
              <el-option label="任务型" :value="3">
                <div>
                  <span style="font-weight: bold;">任务型</span>
                  <div style="font-size: 12px; color: #999;">用户完成任务参与活动</div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="最大参与人数" prop="maxParticipants">
            <el-input-number
              v-model="form.maxParticipants"
              :min="1"
              :max="10000"
              placeholder="请输入最大参与人数"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="积分类型" prop="pointsTypeId">
            <el-select
              v-model="form.pointsTypeId"
              placeholder="请选择积分类型"
              style="width: 100%"
              loading="pointsTypesLoading"
            >
              <el-option
                v-for="item in pointsTypes"
                :key="item.id"
                :label="item.typeName"
                :value="item.id"
              >
                <div>
                  <span style="font-weight: bold;">{{ item.typeName }}</span>
                  <div style="font-size: 12px; color: #999;">{{ item.description }}</div>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="奖励积分" prop="pointsAmount">
            <el-input-number
              v-model="form.pointsAmount"
              :min="0"
              :max="10000"
              placeholder="请输入奖励积分数量"
              style="width: 100%"
            />
            <div style="font-size: 12px; color: #999; margin-top: 4px;">
              设置为0表示无积分奖励
            </div>
          </el-form-item>
        </el-col>

        <!-- 右侧列 -->
        <el-col :span="12">
          <el-form-item label="封面图片" prop="coverImage">
            <div class="image-upload">
              <el-input
                v-model="form.coverImage"
                placeholder="请输入图片URL或上传图片"
                clearable
              />
              <el-upload
                class="upload-demo"
                action="/uapi/file/upload"
                :show-file-list="false"
                :on-success="handleImageUploadSuccess"
                :before-upload="beforeImageUpload"
                accept="image/*"
                style="margin-top: 8px;"
              >
                <el-button size="small" type="primary">上传图片</el-button>
              </el-upload>
              <div v-if="form.coverImage" class="image-preview">
                <img :src="form.coverImage" alt="封面预览" @error="handleImageError" />
              </div>
            </div>
          </el-form-item>

          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="请选择活动开始时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              :disabled-date="disabledStartDate"
            />
          </el-form-item>

          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="请选择活动结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              :disabled-date="disabledEndDate"
            />
          </el-form-item>

          <el-form-item label="活动规则" prop="rules">
            <el-input
              v-model="form.rules"
              type="textarea"
              placeholder="请输入活动规则说明"
              :rows="6"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 操作按钮 -->
    <div class="form-footer">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        {{ isEdit ? '更新活动' : '创建活动' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPointsTypeList } from '../api/activity'

// Props
const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['submit', 'cancel'])

// 响应式数据
const formRef = ref()
const submitLoading = ref(false)
const pointsTypesLoading = ref(false)
const pointsTypes = ref([])

// 表单数据
const form = reactive({
  title: '',
  description: '',
  coverImage: '',
  maxParticipants: 100,
  pointsTypeId: null,
  pointsAmount: 0,
  activityType: 1,
  startTime: '',
  endTime: '',
  rules: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '活动标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '活动描述不能超过 500 个字符', trigger: 'blur' }
  ],
  activityType: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '参与人数范围 1-10000', trigger: 'blur' }
  ],
  pointsTypeId: [
    { required: true, message: '请选择积分类型', trigger: 'change' }
  ],
  pointsAmount: [
    { required: true, message: '请输入奖励积分数量', trigger: 'blur' },
    { type: 'number', min: 0, max: 10000, message: '积分数量范围 0-10000', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择活动开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' }
  ],
  rules: [
    { max: 1000, message: '活动规则不能超过 1000 个字符', trigger: 'blur' }
  ],
  coverImage: [
    { pattern: /^https?:\/\/.+/, message: '请输入有效的图片URL', trigger: 'blur' }
  ]
}

// 监听 props 变化，更新表单数据
watch(() => props.formData, (newData) => {
  if (newData) {
    Object.assign(form, newData)
  }
}, { immediate: true, deep: true })

// 获取积分类型列表
const fetchPointsTypes = async () => {
  pointsTypesLoading.value = true
  try {
    const res = await getPointsTypeList()
    pointsTypes.value = res.data || []
  } catch (error) {
    console.error('获取积分类型失败:', error)
    ElMessage.error('获取积分类型失败')
  } finally {
    pointsTypesLoading.value = false
  }
}

onMounted(fetchPointsTypes)

// 时间选择限制
const disabledStartDate = (time) => {
  // 开始时间不能早于当前时间（编辑时可以）
  return !props.isEdit && time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

const disabledEndDate = (time) => {
  // 结束时间不能早于开始时间
  if (form.startTime) {
    return time.getTime() < new Date(form.startTime).getTime()
  }
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 图片上传处理
const beforeImageUpload = (file) => {
  const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGorPNG) {
    ElMessage.error('上传图片只能是 JPG/PNG/GIF 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
  }
  return isJPGorPNG && isLt2M
}

const handleImageUploadSuccess = (response) => {
  if (response.code === 200) {
    form.coverImage = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.msg || '图片上传失败')
  }
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
}

// 表单提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 验证时间逻辑
    if (form.startTime && form.endTime) {
      if (new Date(form.startTime) >= new Date(form.endTime)) {
        ElMessage.error('活动开始时间必须早于结束时间')
        return
      }
    }
    
    submitLoading.value = true
    emit('submit', { ...form })
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.activity-form {
  padding: 20px 0;
}

.image-upload {
  width: 100%;
}

.image-preview {
  margin-top: 12px;
  width: 100%;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.form-footer {
  text-align: right;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.el-form-item {
  margin-bottom: 20px;
}

.upload-demo {
  display: inline-block;
}
</style> 