<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBuildings, listRooms, listBeds, addBed, removeBed } from '../api/dorm'

const buildings = ref([])
const rooms = ref([])
const selectedBuilding = ref('')
const selectedRoom = ref('')

const loading = ref(false)
const tableData = ref([])

const fetchBuildings = async () => {
  const res = await listBuildings()
  buildings.value = res.data || []
  if (!selectedBuilding.value && buildings.value.length) {
    selectedBuilding.value = buildings.value[0].id
  }
}

const fetchRooms = async () => {
  if (!selectedBuilding.value) { rooms.value=[]; return }
  const res = await listRooms(selectedBuilding.value)
  rooms.value = res.data || []
  if (!selectedRoom.value && rooms.value.length) {
    selectedRoom.value = rooms.value[0].id
  }
}

const fetchBeds = async () => {
  if (!selectedRoom.value) { tableData.value=[]; return }
  loading.value = true
  try {
    const res = await listBeds(selectedRoom.value)
    tableData.value = res.data || []
  } finally { loading.value = false }
}

onMounted(async () => {
  await fetchBuildings()
  await fetchRooms()
  await fetchBeds()
})

watch(selectedBuilding, async () => {
  selectedRoom.value = ''
  await fetchRooms()
  await fetchBeds()
})
watch(selectedRoom, fetchBeds)

// add dialog
const dialogVisible = ref(false)
const form = reactive({ roomId: '', bedNumber: '' })

const openAdd = () => {
  if (!rooms.value.length) { ElMessage.warning('暂无宿舍房间'); return }
  Object.assign(form, { roomId: selectedRoom.value || rooms.value[0].id, bedNumber: '' })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.bedNumber || !form.roomId) { ElMessage.warning('请输入完整信息'); return }
  try {
    await addBed({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchBeds()
  } catch (e) {}
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该床位吗?', '提示', { type: 'warning' }).then(async () => {
    await removeBed(row.id)
    ElMessage.success('删除成功')
    fetchBeds()
  }).catch(()=>{})
}
</script>

<template>
  <div class="dorm-bed-page">
    <div class="toolbar">
      <el-select v-model="selectedBuilding" placeholder="宿舍楼" style="width:200px">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-select v-model="selectedRoom" placeholder="宿舍房间" style="width:200px">
        <el-option v-for="r in rooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
      </el-select>
      <el-button type="primary" @click="openAdd">新增床位</el-button>
    </div>

    <el-table :data="tableData" border v-loading="loading" style="width:100%">
      <el-table-column prop="bedNumber" label="床位号" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">{{ row.status===1?'已占用':'空闲' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增床位" v-model="dialogVisible" width="400px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="宿舍房间">
          <el-select v-model="form.roomId" style="width:100%">
            <el-option v-for="r in rooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位号"><el-input v-model="form.bedNumber" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dorm-bed-page{padding:24px;}
.toolbar{margin-bottom:16px;display:flex;gap:12px;align-items:center;}
</style> 