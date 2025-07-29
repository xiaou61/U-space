<template>
  <div class="announcement-page">
    <el-card v-for="item in list" :key="item.id" class="ann-card" shadow="hover">
      <div class="pub-time">{{ formatTime(item.publishTime) }}</div>
      <div class="content">{{ item.content }}</div>
    </el-card>
    <el-empty v-if="!list.length && loaded" description="暂无公告" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { listAnnouncements } from '../api/announcement'
import dayjs from 'dayjs'

const list = ref([])
const loaded = ref(false)

onMounted(async () => {
  try {
    const res = await listAnnouncements()
    list.value = res.data || []
  } catch (e) {
    // global handler
  } finally {
    loaded.value = true
  }
})

const formatTime = (t) => (t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '')
</script>

<style scoped>
.announcement-page {
  padding: 16px;
}
.ann-card {
  margin-bottom: 16px;
}
.pub-time {
  font-size: 0.8rem;
  color: #909399;
  margin-bottom: 6px;
}
.content {
  font-size: 1rem;
  line-height: 1.4;
  white-space: pre-wrap;
}
</style> 