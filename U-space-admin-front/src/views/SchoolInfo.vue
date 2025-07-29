<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSchoolInfo, updateSchoolInfo } from '../api/school'
import { uploadFile } from '../api/file'
import { AMAP_KEY } from '../config'

// 已移除地点搜索，无需 vueRef

const form = reactive({
  name: '',
  description: '',
  logoUrl: '',
  websiteUrl: '',
  address: '',
  latitude: '',
  longitude: ''
})

const loading = ref(false)
let mapInstance = null
let marker = null
// 移除地点搜索相关变量

const loadMapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.async = true
    script.onload = () => resolve(window.AMap)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const initMap = async () => {
  const AMap = await loadMapScript()
  // 默认中心点（北京天安门）
  const center = [Number(form.longitude) || 116.397428, Number(form.latitude) || 39.90923]
  mapInstance = new AMap.Map('mapContainer', {
    zoom: 12,
    center
  })
  marker = new AMap.Marker({ position: center, draggable: true })
  mapInstance.add(marker)

  // 已移除自动完成插件加载

  // 同步 marker 拖拽结果
  marker.on('dragend', () => {
    const pos = marker.getPosition()
    form.longitude = pos.lng
    form.latitude = pos.lat
  })

  // 点击地图改变 marker 位置
  mapInstance.on('click', (e) => {
    marker.setPosition(e.lnglat)
    form.longitude = e.lnglat.lng
    form.latitude = e.lnglat.lat
  })
}

// 已移除地点搜索函数

const fetchInfo = async () => {
  loading.value = true
  try {
    const res = await getSchoolInfo()
    Object.assign(form, res.data || {})
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  try {
    await updateSchoolInfo({ ...form })
    ElMessage.success('保存成功')
  } catch (e) {}
}

// 上传 Logo
const handleLogoUploadRequest = async ({ file, onSuccess, onError }) => {
  const fd = new window.FormData()
  fd.append('file', file)
  try {
    const res = await uploadFile(fd)
    // 后端返回 URL 在 data/msg
    form.logoUrl = res.data || res.msg || ''
    ElMessage.success('上传成功')
    onSuccess()
  } catch (e) {
    ElMessage.error('上传失败')
    onError(e)
  }
}

onMounted(async () => {
  await fetchInfo()
  await initMap()
})
</script>

<template>
  <div class="school-page">
    <el-form :model="form" label-width="120px" style="max-width:600px">
      <el-form-item label="学校名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="学校简介"><el-input type="textarea" :rows="3" v-model="form.description" /></el-form-item>
      <el-form-item label="Logo">
        <div style="display:flex;gap:12px;align-items:center;">
          <el-upload :show-file-list="false" :http-request="handleLogoUploadRequest" accept="image/*">
            <el-button type="primary">上传 Logo</el-button>
          </el-upload>
          <div v-if="form.logoUrl">
            <img :src="form.logoUrl" alt="logo" style="height:50px;border:1px solid #eee;padding:2px" />
          </div>
        </div>
      </el-form-item>
      <el-form-item label="官网链接"><el-input v-model="form.websiteUrl" /></el-form-item>
      <el-form-item label="详细地址"><el-input v-model="form.address" /></el-form-item>
      <!-- 地点搜索已移除 -->
      <el-form-item label="纬度"><el-input v-model="form.latitude" /></el-form-item>
      <el-form-item label="经度"><el-input v-model="form.longitude" /></el-form-item>
      <el-form-item label="地图选点">
        <div id="mapContainer" class="map-box"></div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.school-page {
  padding: 24px;
}
.map-box {
  width: 100%;
  height: 400px;
}
</style> 