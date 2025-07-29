<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { detailHomework, listSubmissions, approveSubmission, getSubmission } from '../api/homework'
import { ElMessage, ElMessageBox } from 'element-plus'
import JSZip from 'jszip'
import { saveAs } from 'file-saver'

const route = useRoute()
const id = route.params.id

const info = ref({})
const loadingInfo = ref(false)
const submissions = ref([])
const loadingSubs = ref(false)

const fetchInfo = async () => {
  loadingInfo.value = true
  try {
    const res = await detailHomework(id)
    info.value = res.data || {}
  } finally { loadingInfo.value = false }
}

const fetchSubs = async () => {
  loadingSubs.value = true
  try {
    const res = await listSubmissions(id)
    submissions.value = res.data || []
  } finally { loadingSubs.value = false }
}

onMounted(async () => {
  await fetchInfo()
  await fetchSubs()
})

// approve dialog
const approveDialog = ref(false)
const approveForm = ref({ grade:'', feedback:'' })
const currentSubmissionId = ref(null)

const openApprove = (row) => {
  currentSubmissionId.value = row.id ?? row.submissionId ?? row.homeworkId
  approveForm.value = { grade: row.grade || '', feedback: row.feedback || '' }
  approveDialog.value = true
}

const submitApprove = async () => {
  try {
    await approveSubmission(currentSubmissionId.value, approveForm.value)
    ElMessage.success('已提交')
    approveDialog.value = false
    fetchSubs()
  } catch(e) {}
}

const submissionDialog = ref(false)
const submissionLoading = ref(false)
const submissionData = ref({})

const viewContent = async (row) => {
  submissionLoading.value = true
  try {
    const res = await getSubmission(row.id ?? row.submissionId)
    submissionData.value = res.data || {}
    submissionDialog.value = true
  } catch(e) {}
  submissionLoading.value = false
}

const downloadAll = async () => {
  const urls = submissionData.value.attachmentUrls || []
  if (!urls.length) { ElMessage.warning('无附件'); return }
  if (urls.length === 1) {
    window.open(urls[0])
    return
  }
  const zip = new JSZip()
  const folder = zip.folder('attachments')
  const promises = urls.map(async (url) => {
    const resp = await fetch(url)
    const blob = await resp.blob()
    const name = url.split('/').pop()
    folder.file(name, blob)
  })
  await Promise.all(promises)
  const content = await zip.generateAsync({ type: 'blob' })
  saveAs(content, 'attachments.zip')
}
</script>

<template>
  <div class="hw-detail-page">
    <el-card v-loading="loadingInfo" class="mb16">
      <template #header>{{ info.title }}</template>
      <p>{{ info.description }}</p>
      <p>截止时间：{{ info.deadline }}</p>
    </el-card>

    <el-table :data="submissions" v-loading="loadingSubs" border style="width:100%">
      <el-table-column prop="studentName" label="学生" />
      <el-table-column prop="submitTime" label="提交时间" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="grade" label="评分" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="viewContent(row)">查看</el-button>
          <el-button type="primary" size="small" @click="openApprove(row)">审批</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog title="提交内容" v-model="submissionDialog" width="600px">
    <div v-if="submissionLoading" style="text-align:center;padding:20px">Loading...</div>
    <div v-else>
      <p>学生：{{ submissionData.studentName }}</p>
      <p>提交时间：{{ submissionData.submitTime }}</p>
      <p>内容：</p>
      <el-input type="textarea" :rows="4" v-model="submissionData.content" disabled />
      <p style="margin-top:8px">附件：</p>
      <ul>
        <li v-for="url in submissionData.attachmentUrls || []" :key="url">
          <el-link :href="url" target="_blank">{{ url.split('/').pop() }}</el-link>
        </li>
      </ul>
      <el-button v-if="(submissionData.attachmentUrls || []).length" @click="downloadAll">下载附件</el-button>
    </div>
    <template #footer>
      <el-button @click="submissionDialog=false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog title="审批作业" v-model="approveDialog" width="400px">
    <el-form :model="approveForm" label-width="80px">
      <el-form-item label="评分"><el-input v-model="approveForm.grade" /></el-form-item>
      <el-form-item label="反馈"><el-input type="textarea" v-model="approveForm.feedback" rows="3" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="approveDialog=false">取消</el-button>
      <el-button type="primary" @click="submitApprove">确定</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.mb16{margin-bottom:16px;}
.hw-detail-page{padding:24px;}
</style> 