<template>
  <div class="prize-config">
    <el-row :gutter="20" class="action-bar">
      <el-col :span="12">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增奖品
        </el-button>
        <el-button :icon="Refresh" @click="loadPrizeList">
          刷新
        </el-button>
        <el-button :icon="Tools" @click="normalizeDialog = true">
          概率归一化
        </el-button>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <el-button 
          type="success" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchEnable">
          批量启用
        </el-button>
        <el-button 
          type="warning" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDisable">
          批量禁用
        </el-button>
      </el-col>
    </el-row>

    <el-table 
      :data="prizeList" 
      stripe
      border
      v-loading="loading"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="prizeName" label="奖品名称" width="120" />
      <el-table-column label="奖品等级" width="100">
        <template #default="{ row }">
          <el-tag :type="getLevelType(row.prizeLevel)">
            {{ getLevelName(row.prizeLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="prizePoints" label="奖励积分" width="100" />
      <el-table-column label="基础概率" width="120">
        <template #default="{ row }">
          {{ (row.baseProbability * 100).toFixed(4) }}%
        </template>
      </el-table-column>
      <el-table-column label="当前概率" width="120">
        <template #default="{ row }">
          <span :class="{ 'probability-changed': row.currentProbability !== row.baseProbability }">
            {{ (row.currentProbability * 100).toFixed(4) }}%
          </span>
        </template>
      </el-table-column>
      <el-table-column label="库存" width="100">
        <template #default="{ row }">
          <span v-if="row.totalStock && row.totalStock > 0">
            {{ row.currentStock }} / {{ row.totalStock }}
          </span>
          <el-tag v-else type="info">无限制</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isSuspended === 1" type="danger">已暂停</el-tag>
          <el-tag v-else-if="row.isActive === 1" type="success">启用中</el-tag>
          <el-tag v-else type="info">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button 
            link 
            :type="row.isActive === 1 ? 'warning' : 'success'" 
            size="small" 
            @click="handleToggleStatus(row)">
            {{ row.isActive === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button 
            v-if="row.isSuspended === 1"
            link 
            type="success" 
            size="small" 
            @click="handleResume(row)">
            恢复
          </el-button>
          <el-button 
            v-else
            link 
            type="warning" 
            size="small" 
            @click="handleSuspend(row)">
            暂停
          </el-button>
          <el-button 
            link 
            type="primary" 
            size="small" 
            @click="handleAdjustProbability(row)">
            调整概率
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑奖品对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle"
      width="600px">
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="formRules"
        label-width="120px">
        <el-form-item label="奖品名称" prop="prizeName">
          <el-input v-model="formData.prizeName" placeholder="请输入奖品名称" />
        </el-form-item>
        <el-form-item label="奖品等级" prop="prizeLevel">
          <el-select v-model="formData.prizeLevel" placeholder="请选择奖品等级">
            <el-option label="特等奖" :value="1" />
            <el-option label="一等奖" :value="2" />
            <el-option label="二等奖" :value="3" />
            <el-option label="三等奖" :value="4" />
            <el-option label="四等奖" :value="5" />
            <el-option label="五等奖" :value="6" />
            <el-option label="六等奖" :value="7" />
            <el-option label="未中奖" :value="8" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励积分" prop="prizePoints">
          <el-input-number v-model="formData.prizePoints" :min="0" />
        </el-form-item>
        <el-form-item label="基础概率" prop="baseProbability">
          <el-input-number 
            v-model="formData.baseProbability" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4" />
          <span style="margin-left: 10px">{{ (formData.baseProbability * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="目标回报率" prop="targetReturnRate">
          <el-input-number 
            v-model="formData.targetReturnRate" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4" />
          <span style="margin-left: 10px">{{ (formData.targetReturnRate * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="最大回报率" prop="maxReturnRate">
          <el-input-number 
            v-model="formData.maxReturnRate" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4" />
          <span style="margin-left: 10px">{{ (formData.maxReturnRate * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="最小回报率" prop="minReturnRate">
          <el-input-number 
            v-model="formData.minReturnRate" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4" />
          <span style="margin-left: 10px">{{ (formData.minReturnRate * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="总库存">
          <el-input-number v-model="formData.totalStock" :min="-1" />
          <span style="margin-left: 10px; color: #909399;">-1表示无限制</span>
        </el-form-item>
        <el-form-item label="调整策略" prop="adjustStrategy">
          <el-select v-model="formData.adjustStrategy">
            <el-option label="自动调整" value="AUTO" />
            <el-option label="手动调整" value="MANUAL" />
            <el-option label="固定概率" value="FIXED" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖品描述">
          <el-input 
            v-model="formData.prizeDesc" 
            type="textarea" 
            :rows="3"
            placeholder="请输入奖品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 暂停对话框 -->
    <el-dialog v-model="suspendDialog" title="暂停奖品" width="400px">
      <el-form label-width="100px">
        <el-form-item label="暂停时长">
          <el-input-number v-model="suspendMinutes" :min="0" />
          <span style="margin-left: 10px">分钟（0表示手动恢复）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="suspendDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmSuspend">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 调整概率对话框 -->
    <el-dialog v-model="adjustDialog" title="调整概率" width="400px">
      <el-form label-width="100px">
        <el-form-item label="当前概率">
          <span>{{ (currentPrize?.currentProbability * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="新概率">
          <el-input-number 
            v-model="newProbability" 
            :min="0" 
            :max="1" 
            :step="0.0001" 
            :precision="4" />
          <span style="margin-left: 10px">{{ (newProbability * 100).toFixed(4) }}%</span>
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input 
            v-model="adjustReason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 归一化对话框 -->
    <el-dialog v-model="normalizeDialog" title="概率归一化" width="500px">
      <el-alert 
        title="归一化说明" 
        type="info" 
        description="将所有奖品的当前概率按比例调整，确保概率总和为1.0" 
        :closable="false"
        style="margin-bottom: 20px" />
      <el-button type="primary" @click="validateProbability" style="margin-bottom: 10px">
        验证当前概率
      </el-button>
      <div v-if="probabilitySum !== null" style="margin-bottom: 20px">
        <p>当前概率总和：<strong>{{ probabilitySum }}</strong></p>
        <p v-if="needsNormalization" style="color: #e6a23c">
          ⚠️ 需要归一化
        </p>
        <p v-else style="color: #67c23a">
          ✓ 概率正常
        </p>
      </div>
      <template #footer>
        <el-button @click="normalizeDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmNormalize"
          :disabled="!needsNormalization">
          执行归一化
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Tools } from '@element-plus/icons-vue'
import { lotteryAdminApi } from '@/api/lotteryAdmin'

const loading = ref(false)
const saving = ref(false)
const prizeList = ref([])
const selectedIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增奖品')
const formRef = ref(null)

const formData = reactive({
  id: null,
  prizeName: '',
  prizeLevel: 1,
  prizePoints: 0,
  baseProbability: 0.1,
  targetReturnRate: 0.01,
  maxReturnRate: 0.015,
  minReturnRate: 0.005,
  totalStock: -1,
  adjustStrategy: 'AUTO',
  prizeDesc: ''
})

const formRules = {
  prizeName: [{ required: true, message: '请输入奖品名称', trigger: 'blur' }],
  prizeLevel: [{ required: true, message: '请选择奖品等级', trigger: 'change' }],
  baseProbability: [{ required: true, message: '请输入基础概率', trigger: 'blur' }]
}

// 暂停相关
const suspendDialog = ref(false)
const suspendPrizeId = ref(null)
const suspendMinutes = ref(60)

// 调整概率相关
const adjustDialog = ref(false)
const currentPrize = ref(null)
const newProbability = ref(0)
const adjustReason = ref('')

// 归一化相关
const normalizeDialog = ref(false)
const probabilitySum = ref(null)
const needsNormalization = ref(false)

// 加载奖品列表
const loadPrizeList = async () => {
  loading.value = true
  try {
    prizeList.value = await lotteryAdminApi.getPrizeConfigList()
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增奖品'
  Object.assign(formData, {
    id: null,
    prizeName: '',
    prizeLevel: 1,
    prizePoints: 0,
    baseProbability: 0.1,
    targetReturnRate: 0.01,
    maxReturnRate: 0.015,
    minReturnRate: 0.005,
    totalStock: -1,
    adjustStrategy: 'AUTO',
    prizeDesc: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑奖品'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 保存
const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    await lotteryAdminApi.savePrizeConfig(formData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    const isActive = row.isActive === 1 ? false : true
    await lotteryAdminApi.togglePrizeStatus(row.id, isActive)
    ElMessage.success(isActive ? '启用成功' : '禁用成功')
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 暂停
const handleSuspend = (row) => {
  suspendPrizeId.value = row.id
  suspendMinutes.value = 60
  suspendDialog.value = true
}

// 确认暂停
const confirmSuspend = async () => {
  try {
    await lotteryAdminApi.suspendPrize(suspendPrizeId.value, suspendMinutes.value)
    ElMessage.success('暂停成功')
    suspendDialog.value = false
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 恢复
const handleResume = async (row) => {
  try {
    await ElMessageBox.confirm('确认要恢复该奖品吗？', '提示', {
      type: 'warning'
    })
    await lotteryAdminApi.suspendPrize(row.id, 0)
    ElMessage.success('恢复成功')
    loadPrizeList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 调整概率
const handleAdjustProbability = (row) => {
  currentPrize.value = row
  newProbability.value = row.currentProbability
  adjustReason.value = ''
  adjustDialog.value = true
}

// 确认调整
const confirmAdjust = async () => {
  if (!adjustReason.value) {
    ElMessage.warning('请输入调整原因')
    return
  }
  try {
    await lotteryAdminApi.adjustProbability({
      prizeId: currentPrize.value.id,
      newProbability: newProbability.value,
      reason: adjustReason.value
    })
    ElMessage.success('调整成功')
    adjustDialog.value = false
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '调整失败')
  }
}

// 验证概率
const validateProbability = async () => {
  try {
    const sum = await lotteryAdminApi.validateProbabilitySum()
    probabilitySum.value = sum
    needsNormalization.value = Math.abs(sum - 1.0) > 0.0001
  } catch (error) {
    ElMessage.error(error.message || '验证失败')
  }
}

// 确认归一化
const confirmNormalize = async () => {
  try {
    await lotteryAdminApi.normalizeAllProbabilities()
    ElMessage.success('归一化成功')
    normalizeDialog.value = false
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '归一化失败')
  }
}

// 批量操作
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchEnable = async () => {
  try {
    await lotteryAdminApi.batchToggleStatus({
      prizeIds: selectedIds.value,
      isActive: true
    })
    ElMessage.success('批量启用成功')
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleBatchDisable = async () => {
  try {
    await lotteryAdminApi.batchToggleStatus({
      prizeIds: selectedIds.value,
      isActive: false
    })
    ElMessage.success('批量禁用成功')
    loadPrizeList()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 辅助方法
const getLevelName = (level) => {
  const names = ['', '特等奖', '一等奖', '二等奖', '三等奖', '四等奖', '五等奖', '六等奖', '未中奖']
  return names[level] || '未知'
}

const getLevelType = (level) => {
  const types = ['', 'danger', 'warning', 'success', 'info', '', '', '', 'info']
  return types[level] || ''
}

onMounted(() => {
  loadPrizeList()
})
</script>

<style scoped lang="scss">
.prize-config {
  .action-bar {
    margin-bottom: 20px;
  }
  
  .probability-changed {
    color: #e6a23c;
    font-weight: bold;
  }
}
</style>

