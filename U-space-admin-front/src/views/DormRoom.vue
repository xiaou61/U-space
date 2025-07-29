<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBuildings, listRooms, addRoom, removeRoom } from '../api/dorm'

const buildings = ref([])
const selectedBuilding = ref('')

const loading = ref(false)
const tableData = ref([])

const fetchBuildings = async () => {
  try {
    const res = await listBuildings()
    buildings.value = res.data || []
    if (!selectedBuilding.value && buildings.value.length) {
      selectedBuilding.value = buildings.value[0].id
    }
  } catch (e) {}
}

const fetchRooms = async () => {
  if (!selectedBuilding.value) { tableData.value = []; return }
  loading.value = true
  try {
    const res = await listRooms(selectedBuilding.value)
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(async () => {
  await fetchBuildings()
  await fetchRooms()
})

watch(selectedBuilding, () => fetchRooms())

// add dialog
const dialogVisible = ref(false)
const form = reactive({ buildingId: '', roomNumber: '', capacity: '', gender: 1 })

const openAdd = () => {
  if (!buildings.value.length) { ElMessage.warning('暂无宿舍楼'); return }
  Object.assign(form, { buildingId: selectedBuilding.value || buildings.value[0].id, roomNumber: '', capacity: '', gender: 1 })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.roomNumber || !form.buildingId) { ElMessage.warning('请输入完整信息'); return }
  try {
    await addRoom({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchRooms()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该宿舍吗?', '提示', { type: 'warning' }).then(async () => {
    await removeRoom(row.id)
    ElMessage.success('删除成功')
    fetchRooms()
  }).catch(() => {})
}
</script>

<template>
  <div class="dorm-room-page">
    <div class="toolbar">
      <el-select v-model="selectedBuilding" placeholder="选择宿舍楼" style="width:200px">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-button type="primary" @click="openAdd">新增宿舍</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="roomNumber" label="房间号" />
      <el-table-column prop="capacity" label="总床位" />
      <el-table-column prop="available" label="剩余床位" />
      <el-table-column prop="gender" label="性别">
        <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增宿舍" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="宿舍楼">
          <el-select v-model="form.buildingId" style="width:100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号"><el-input v-model="form.roomNumber" /></el-form-item>
        <el-form-item label="容量"><el-input v-model="form.capacity" type="number" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :label="1">男生宿舍</el-radio>
            <el-radio :label="2">女生宿舍</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dorm-room-page{padding:24px;}
.toolbar{margin-bottom:16px;display:flex;gap:12px;align-items:center;}
</style> 