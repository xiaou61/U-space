<template>
  <div class="home-page">
    <section class="hero">
      <div v-if="loading" class="hero-skel"><el-skeleton animated :rows="4" /></div>
      <template v-else>
        <img v-if="info.logoUrl" :src="info.logoUrl" alt="logo" class="hero-logo" />
        <h1 class="hero-name">{{ info.name }}</h1>
        <p class="hero-desc">{{ info.description }}</p>
        <div class="hero-links">
          <el-link v-if="info.websiteUrl" :href="info.websiteUrl" target="_blank" type="primary"
            >官网</el-link
          >
          <el-link
            v-if="info.address"
            :underline="false"
            @click="openMap"
            class="address-link"
          >
            <el-icon><Location /></el-icon>
            <span>{{ info.address }}</span>
          </el-link>
        </div>
      </template>
    </section>
    <!-- 公告轮播 -->
    <div v-if="annLoading" class="announce-section"><el-skeleton animated :rows="2" /></div>
    <div v-else-if="announcements.length" class="announce-section">
      <h3 class="ann-title">最新公告</h3>
      <el-carousel height="110px" indicator-position="outside" :autoplay="true" interval="4000">
        <el-carousel-item v-for="item in announcements" :key="item.id">
          <div class="ann-item">
            <p class="ann-content">{{ item.content }}</p>
            <span class="ann-time">{{ formatTime(item.publishTime) }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div class="more-link">
        <el-link type="primary" :underline="false" @click="goAnnouncements">查看更多</el-link>
      </div>
    </div>
    <el-empty v-else description="暂无公告" class="announce-section" />

    <div v-if="weatherLoading" class="weather-section"><el-skeleton animated :rows="2" /></div>

    <div v-else class="weather-section" v-if="weather">
      <h3 class="weather-title">当前天气 · {{ weather.city }}</h3>
      <div class="weather-main">
        <div class="temp">{{ weather.wendu }}°C</div>
        <div class="type">{{ weather.type }}</div>
      </div>
      <div class="weather-meta">
        <span>最高 {{ weather.high }}</span>
        <span>最低 {{ weather.low }}</span>
        <span>湿度 {{ weather.shidu }}</span>
        <span>空气 {{ weather.quality }}</span>
      </div>
      <div class="weather-extra">
        <span>风向 {{ weather.fx }}</span>
        <span>风速 {{ weather.fl }}</span>
        <span>日出 {{ weather.sunrise }}</span>
        <span>日落 {{ weather.sunset }}</span>
      </div>
      <p class="notice" v-if="weather.notice">{{ weather.notice }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSchoolInfo } from '../api/school'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getWeather } from '../api/weather'
import { listAnnouncements } from '../api/announcement'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const info = ref({})
const loading = ref(true)
const weather = ref(null)
const weatherLoading = ref(true)
const announcements = ref([])
const annLoading = ref(true)

const router = useRouter()

const fetchInfo = async () => {
  try {
    const res = await getSchoolInfo()
    info.value = res.data || {}
  } catch (e) {
    ElMessage.error('加载学校信息失败')
  } finally {
    loading.value = false
  }
}

const fetchWeather = async () => {
  try {
    const res = await getWeather()
    const list = res.data || []
    weather.value = list[0] || null
  } catch (e) {
    console.error(e)
  } finally {
    weatherLoading.value = false
  }
}

const fetchAnnouncements = async () => {
  try {
    const res = await listAnnouncements()
    const list = res.data || []
    // sort by publishTime desc and take first 3
    announcements.value = list
      .sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime))
      .slice(0, 3)
  } catch (e) {
    console.error(e)
  } finally {
    annLoading.value = false
  }
}

function goAnnouncements() {
  router.push('/announcements')
}

function formatTime(t) {
  return t ? dayjs(t).format('MM-DD HH:mm') : ''
}

onMounted(() => {
  fetchInfo()
  fetchWeather()
  fetchAnnouncements()
})

function openMap() {
  if (!info.value.latitude || !info.value.longitude) return
  const lat = info.value.latitude
  const lng = info.value.longitude
  const name = encodeURIComponent(info.value.name || '目的地')
  // 使用高德 URI scheme，callnative=1 可调起客户端
  const url = `https://uri.amap.com/navigation?from=${lng},${lat},${name}&mode=car&callnative=1`
  window.open(url, '_blank')
}
</script>

<style scoped>
.home-page {
  padding: 0 16px 16px;
  min-height: calc(100vh - 56px);
  background: #f5f6f8;
}
.hero {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  padding: 60px 20px 80px;
  text-align: center;
  color: #fff;
  border-bottom-left-radius: 40% 10%;
  border-bottom-right-radius: 40% 10%;
}
.hero-logo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.hero-name {
  margin: 16px 0 8px;
  font-size: 1.8rem;
  font-weight: 700;
}
.hero-desc {
  margin: 0 auto;
  max-width: 480px;
  line-height: 1.5;
  font-size: 1rem;
  opacity: 0.9;
}
.hero-links {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}
.address-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #fff;
}

/* card container redefined */
.card {
  max-width: 520px;
  margin: -60px auto 0; /* pull up under hero curve */
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  padding: 24px 18px;
}

.announce-section,
.weather-section {
  @apply card;
  margin-top: 24px;
}
.weather-title {
  margin: 0 0 12px 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
  text-align: center;
}
.weather-main {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}
.temp {
  font-size: 2.2rem;
  font-weight: 600;
  color: #ff7f50;
}
.type {
  font-size: 1.2rem;
  color: #409eff;
}
.weather-meta,
.weather-extra {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.85rem;
  color: #555;
}
.notice {
  margin-top: 8px;
  font-size: 0.85rem;
  color: #f56c6c;
}
.ann-title {
  margin: 0 0 12px 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  text-align: center;
}
.ann-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.ann-content {
  margin: 0;
  font-size: 0.95rem;
  color: #333;
  text-align: center;
  line-height: 1.4;
  white-space: pre-wrap;
}
.ann-time {
  margin-top: 6px;
  font-size: 0.75rem;
  color: #909399;
}
.more-link {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
/* ensure skeleton containers */
.hero-skel {
  max-width: 520px;
  margin: 0 auto;
}
.logo {display:none} /* remove old logo style */
</style> 