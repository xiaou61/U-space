<template>
  <div class="draw-records">
    <el-card shadow="hover">
      <!-- 查询表单 -->
      <el-form :model="queryForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="奖品ID">
          <el-input v-model="queryForm.prizeId" placeholder="请输入奖品ID" clearable />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="queryForm.startDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="queryForm.endDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="recordList" stripe border v-loading="loading">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="prizeName" label="奖品名称" width="120" />
        <el-table-column prop="prizePoints" label="获得积分" width="100" align="center" />
        <el-table-column label="奖品等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.prizeLevel)" size="small">
              {{ getLevelName(row.prizeLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="strategyType" label="抽奖策略" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="device" label="设备信息" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="抽奖时间" width="180" />
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { lotteryAdminApi } from '@/api/lotteryAdmin'

const loading = ref(false)
const recordList = ref([])
const total = ref(0)

const queryForm = reactive({
  userId: null,
  prizeId: null,
  startDate: '',
  endDate: '',
  page: 1,
  size: 20
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await lotteryAdminApi.getAllDrawRecords(queryForm)
    recordList.value = res.records
    total.value = res.total
  } catch (error) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  Object.assign(queryForm, {
    userId: null,
    prizeId: null,
    startDate: '',
    endDate: '',
    page: 1,
    size: 20
  })
  handleQuery()
}

const getLevelName = (level) => {
  const names = ['', '特等奖', '一等奖', '二等奖', '三等奖', '四等奖', '五等奖', '六等奖', '未中奖']
  return names[level] || '未知'
}

const getLevelType = (level) => {
  if (level === 1) return 'danger'
  if (level <= 3) return 'warning'
  if (level <= 7) return 'success'
  return 'info'
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.draw-records {
  :deep(.el-pagination) {
    display: flex;
  }
}
</style>

