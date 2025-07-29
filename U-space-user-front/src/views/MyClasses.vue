<template>
  <div class="classes-page">
    <el-button type="primary" icon="plus" class="join-btn" @click="joinDialog = true">加入班级</el-button>

    <el-card
      v-for="g in groups"
      :key="g.id"
      class="group-card"
      shadow="never"
      @click="goDetail(g)"
    >
      <div class="g-name">{{ g.name }}</div>
      <div class="g-desc">{{ g.description }}</div>
    </el-card>

    <el-empty v-if="!groups.length && loaded" description="暂无班级" />

    <el-dialog v-model="joinDialog" title="加入班级" width="85%" :close-on-click-modal="false">
      <el-input v-model="joinId" placeholder="请输入班级ID" />
      <template #footer>
        <el-button @click="joinDialog = false">取消</el-button>
        <el-button type="primary" @click="submitJoin">加入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listGroups, joinGroup } from '../api/group'
import { ElMessage } from 'element-plus'

const groups = ref([])
const loaded = ref(false)
const joinDialog = ref(false)
const joinId = ref('')
const router = useRouter()

const fetchList = async () => {
  try {
    const res = await listGroups()
    groups.value = res.data || []
  } catch (e) {
    // global handler
  } finally {
    loaded.value = true
  }
}

onMounted(fetchList)

const submitJoin = async () => {
  if (!joinId.value) return
  try {
    await joinGroup(joinId.value.trim())
    ElMessage.success('加入成功')
    joinDialog.value = false
    joinId.value = ''
    fetchList()
  } catch (e) {
    // handled globally
  }
}

const goDetail = (g) => {
  router.push({ name: 'GroupDetail', query: { id: g.id, name: g.name, desc: g.description } })
}
</script>

<style scoped>
.classes-page {
  padding: 16px;
}
.join-btn {
  margin-bottom: 16px;
}
.group-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.g-name {
  font-weight: 600;
  font-size: 1.1rem;
}
.g-desc {
  color: #606266;
  margin-top: 4px;
}
</style> 