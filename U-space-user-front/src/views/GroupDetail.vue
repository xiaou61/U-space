<template>
  <div class="group-detail-page">
    <h2>{{ groupName }}</h2>
    <p class="desc">{{ groupDesc }}</p>

    <!-- 操作按钮行 -->
    <div class="action-row">
      <el-button class="action-btn" type="primary" size="large" @click="toggleSigninPanel">我的签到</el-button>
      <el-button class="action-btn" type="success" size="large" @click="toggleHomeworkPanel">我的作业</el-button>
      <el-button class="action-btn" type="info" size="large" @click="toggleMaterialPanel">我的资料</el-button>
    </div>

    <!-- 签到面板 -->
    <div v-if="signinPanelVisible">
      <el-skeleton v-if="!signinLoaded" rows="3" animated />
      <el-card v-for="s in signins" :key="s.id" class="signin-card" shadow="never">
        <div class="s-info">
          <span>截止：{{ formatTime(s.endTime) }}</span>
          <span class="type-tag">{{ s.type === 1 ? '密码签到' : '普通签到' }}</span>
        </div>
        <el-button type="primary" size="small" @click="doSignin(s)">签到</el-button>
      </el-card>
      <el-empty v-if="signinLoaded && !signins.length" description="暂无签到任务" />
    </div>

    <!-- 作业面板 -->
    <div v-if="homeworkPanelVisible">
      <el-skeleton v-if="!homeworkLoaded" rows="3" animated />
      <el-card v-for="h in homeworks" :key="h.id" class="signin-card" shadow="never">
        <div class="s-info">
          <span class="hw-title">{{ h.title }}</span>
          <span>截止：{{ formatTime(h.deadline) }}</span>
        </div>
        <el-button type="primary" size="small" @click="openHwDialog(h, false)">提交</el-button>
        <el-button type="warning" size="small" @click="openHwDialog(h, true)">修改</el-button>
        <el-button type="info" size="small" @click="viewGrade(h)">查看评分</el-button>
      </el-card>
      <el-empty v-if="homeworkLoaded && !homeworks.length" description="暂无作业" />
    </div>

    <!-- 资料面板 -->
    <div v-if="materialPanelVisible">
      <el-skeleton v-if="!materialLoaded" rows="3" animated />
      <el-card
        v-for="m in materials"
        :key="m.id"
        class="signin-card"
        shadow="never"
      >
        <div class="s-info">
          <span class="hw-title">{{ m.title }}</span>
          <span>{{ m.description }}</span>
        </div>
        <div v-if="m.fileUrls && m.fileUrls.length" class="file-list">
          <el-link
            v-for="(url, idx) in m.fileUrls"
            :key="idx"
            :href="url"
            target="_blank"
            type="primary"
            class="file-link"
            >附件{{ idx + 1 }}</el-link
          >
        </div>
      </el-card>
      <el-empty v-if="materialLoaded && !materials.length" description="暂无资料" />
    </div>

    <!-- 密码签到弹窗 -->
    <el-dialog v-model="pwdDialog.visible" title="密码签到" width="80%" :close-on-click-modal="false">
      <el-input v-model="pwdDialog.password" placeholder="请输入签到密码" />
      <template #footer>
        <el-button @click="pwdDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitPwdSignin">签到</el-button>
      </template>
    </el-dialog>

    <!-- 提交作业弹窗 -->
    <el-dialog v-model="hwDialog.visible" :title="hwDialog.isUpdate ? '修改作业' : '提交作业'" width="90%" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="内容">
          <el-input type="textarea" v-model="hwDialog.content" placeholder="请输入作业内容" rows="4" />
        </el-form-item>
        <el-form-item label="附件上传">
          <el-upload
            class="upload-area"
            :show-file-list="true"
            :on-remove="handleRemove"
            :before-upload="handleFileUpload"
            multiple
            accept="*"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hwDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitHomework">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listSignins, addSignin } from '../api/signin'
import { listHomeworks, submitHomework as submitHomeworkApi, uploadAttachment, updateHomework, getHomeworkGrade } from '../api/homework'
import { listMaterials } from '../api/material'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const route = useRoute()
const groupId = computed(() => route.query.id || '')
const groupName = computed(() => route.query.name || '班级详情')
const groupDesc = computed(() => route.query.desc || '')

// --------- 签到 ---------
const signins = ref([])
const signinLoaded = ref(false)
const signinPanelVisible = ref(false)

const fetchSignins = async () => {
  if (!groupId.value) return
  try {
    const res = await listSignins(groupId.value)
    signins.value = res.data || []
  } catch (e) {
    // handled globally
  } finally {
    signinLoaded.value = true
  }
}

const toggleSigninPanel = () => {
  if (!groupId.value) {
    ElMessage.error('缺少群组ID，无法加载签到信息')
    return
  }
  signinPanelVisible.value = !signinPanelVisible.value
  if (signinPanelVisible.value && !signinLoaded.value) {
    fetchSignins()
  }
}

const formatTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '')

const pwdDialog = ref({ visible: false, password: '', current: null })

const doSignin = (s) => {
  if (s.type === 1) {
    pwdDialog.value.visible = true
    pwdDialog.value.current = s
  } else {
    submitSignin({ signinId: s.id, type: 0 })
  }
}

const submitPwdSignin = () => {
  if (!pwdDialog.value.password) return
  submitSignin({
    signinId: pwdDialog.value.current.id,
    type: 1,
    password: pwdDialog.value.password,
  })
  pwdDialog.value.visible = false
  pwdDialog.value.password = ''
}

const submitSignin = async (data) => {
  try {
    await addSignin(data)
    ElMessage.success('签到成功')
    fetchSignins()
  } catch (e) {
    // handled globally
  }
}

// --------- 作业 ---------
const homeworks = ref([])
const homeworkLoaded = ref(false)
const homeworkPanelVisible = ref(false)

const fetchHomeworks = async () => {
  if (!groupId.value) return
  try {
    const res = await listHomeworks(groupId.value)
    homeworks.value = res.data || []
  } catch (e) {
    // handled globally
  } finally {
    homeworkLoaded.value = true
  }
}

const toggleHomeworkPanel = () => {
  if (!groupId.value) {
    ElMessage.error('缺少群组ID，无法加载作业信息')
    return
  }
  homeworkPanelVisible.value = !homeworkPanelVisible.value
  if (homeworkPanelVisible.value && !homeworkLoaded.value) {
    fetchHomeworks()
  }
}

const hwDialog = ref({ visible: false, current: null, content: '', attachments: [], isUpdate: false })

const openHwDialog = (h, isUpdate) => {
  hwDialog.value.visible = true
  hwDialog.value.current = h
  hwDialog.value.content = ''
  hwDialog.value.attachments = []
  hwDialog.value.isUpdate = isUpdate
}

const viewGrade = async (h) => {
  try {
    const res = await getHomeworkGrade(h.id)
    const info = res.data || {}
    const msg = `分数：${info.grade ?? '未评分'}\n教师反馈：${info.feedback || '暂未点评'}`
    ElMessageBox.alert(msg.replace(/\n/g, '<br/>'), '作业点评', {
      confirmButtonText: '知道了',
      dangerouslyUseHTMLString: true,
    })
  } catch (e) {
    ElMessage.error('获取评分失败')
  }
}

const handleFileUpload = async (file) => {
  try {
    const res = await uploadAttachment(file)
    const url = res.data
    if (url) hwDialog.value.attachments.push(url)
  } catch (e) {
    ElMessage.error('上传失败')
  }
  return false // prevent auto upload
}

const handleRemove = (file) => {
  // remove by name url matching
}

const submitHomework = async () => {
  if (!hwDialog.value.current) return
  try {
    const data = {
      content: hwDialog.value.content,
      attachmentUrls: hwDialog.value.attachments,
    }
    if (hwDialog.value.isUpdate) {
      await updateHomework(hwDialog.value.current.id, data)
      ElMessage.success('修改成功')
    } else {
      await submitHomeworkApi(hwDialog.value.current.id, data)
      ElMessage.success('提交成功')
    }
    hwDialog.value.visible = false
  } catch (e) {
    // handle
  }
}

// --------- 资料 ---------
const materials = ref([])
const materialLoaded = ref(false)
const materialPanelVisible = ref(false)

const fetchMaterials = async () => {
  if (!groupId.value) return
  try {
    const res = await listMaterials(groupId.value)
    materials.value = res.data || []
  } catch (e) {
    // handled globally
  } finally {
    materialLoaded.value = true
  }
}

const toggleMaterialPanel = () => {
  if (!groupId.value) {
    ElMessage.error('缺少群组ID，无法加载资料')
    return
  }
  materialPanelVisible.value = !materialPanelVisible.value
  if (materialPanelVisible.value && !materialLoaded.value) {
    fetchMaterials()
  }
}
</script>

<style scoped>
.group-detail-page {
  padding: 16px;
}
.desc {
  color: #606266;
  margin-top: 8px;
}
.action-row {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}
.action-btn {
  flex: 1;
  height: 48px;
  font-size: 1rem;
  border-radius: 12px;
}
.signin-card {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.s-info {
  display: flex;
  flex-direction: column;
}
.type-tag {
  color: #409eff;
  font-size: 0.8rem;
}
.hw-title {
  font-weight: 600;
}
.file-list {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.file-link {
  font-size: 0.85rem;
}
</style> 