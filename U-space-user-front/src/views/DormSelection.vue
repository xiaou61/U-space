<template>
  <div class="dorm-selection-page">
    <!-- Loading state -->
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="rect" style="height: 120px;" />
        <el-skeleton-item variant="rect" style="height: 120px; margin-top: 16px;" />
      </template>
      <template #default>
        <!-- 如果需要填写信息 -->
        <div v-if="needInput" class="input-form">
          <h3 class="section-title">完善信息</h3>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-form>
          <el-button type="primary" class="submit-btn" @click="submitInfo">提交</el-button>
        </div>

        <!-- 显示宿舍列表 -->
        <div v-else>
          <h3 class="section-title">可选宿舍</h3>
          <el-empty v-if="dorms.length === 0" description="暂无可选宿舍" />
          <div v-else class="dorm-list">
            <el-card
              v-for="room in dorms"
              :key="room.id"
              class="dorm-card"
              shadow="hover"
              @click="openBeds(room)"
            >
              <p class="room-title">{{ room.buildingName }} {{ room.roomNumber }}</p>
              <p>容量：{{ room.capacity }} 人</p>
              <p>剩余：{{ room.available }} 床</p>
            </el-card>
          </div>
        </div>
      </template>
    </el-skeleton>

    <!-- 床位详情弹窗 -->
    <el-dialog v-model="bedsDialogVisible" :title="(selectedRoom?.buildingName || '') + ' ' + (selectedRoom?.roomNumber || '')" width="400px">
      <div class="beds-grid" v-if="selectedRoom">
        <div
          v-for="bed in selectedRoom.dormBeds"
          :key="bed.bedNumber"
          class="bed-item"
          :class="{ occupied: bed.status !== 0 }"
        >
          {{ bed.bedNumber }}
        </div>
      </div>
      <template #footer>
        <el-button @click="bedsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { isNeedInputInfo, inputInfo, listDorms } from '../api/dorm'

const loading = ref(true)
const needInput = ref(false)
const dorms = ref([])

const selectedRoom = ref(null)
const bedsDialogVisible = ref(false)

const form = reactive({
  gender: null,
  idCard: '',
})

const rules = {
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      pattern: /^(\d{15}|\d{17}[0-9Xx])$/,
      message: '身份证号格式不正确',
      trigger: 'blur',
    },
  ],
}

const formRef = ref(null)

async function fetchState() {
  loading.value = true
  try {
    const res = await isNeedInputInfo()
    // 后端返回数据位于 res.data，根据统一响应结构 { code, data }
    needInput.value = res.data === true
    if (!needInput.value) {
      await fetchDorms()
    }
  } catch (e) {
    console.error('检查输入信息需求失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchDorms() {
  try {
    const res = await listDorms()
    dorms.value = res.data || []
  } catch (e) {
    console.error('获取宿舍列表失败', e)
  }
}

function openBeds(room) {
  selectedRoom.value = room
  bedsDialogVisible.value = true
}

function submitInfo() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await inputInfo({ ...form })
      ElMessage.success('信息已提交')
      needInput.value = false
      await fetchDorms()
    } catch (e) {
      console.error('提交信息失败', e)
    }
  })
}

onMounted(fetchState)
</script>

<style scoped>
.dorm-selection-page {
  padding: 16px;
  padding-bottom: 72px; /* 留出底部导航空间 */
}
.section-title {
  margin: 0 0 12px;
  font-size: 1rem;
  font-weight: 600;
}
.input-form {
  max-width: 480px;
  margin: 0 auto;
}
.submit-btn {
  width: 100%;
}
.dorm-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}
.dorm-card {
  flex: 1 1 calc(50% - 8px);
  min-width: 140px;
}
.room-title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>

<style scoped>
.beds-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 8px;
}
.bed-item {
  flex: 1 1 30%;
  padding: 8px 0;
  text-align: center;
  border-radius: 6px;
  background: #f5f6fa;
}
.bed-item.occupied {
  background: #f4cccc;
  color: #999;
}
</style> 