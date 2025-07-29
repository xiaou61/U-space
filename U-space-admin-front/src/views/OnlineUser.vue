<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { listOnlineUsers, offlineUser, getOnlineCount } from '../api/online'
import { ElMessage, ElMessageBox } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GaugeChart } from 'echarts/charts'
import { TooltipComponent, TitleComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, GaugeChart, TooltipComponent, TitleComponent])

const loading = ref(false)
const users = ref([])
const timer = ref(null)
const gaugeOption = reactive({
  title:{ text:'在线用户', left:'center', top:'10', textStyle:{color:'#fff'} },
  tooltip:{ formatter: '{c} 个' },
  series:[{
    type:'gauge',
    startAngle: 180,
    endAngle: 0,
    min:0,
    max:100,
    progress:{ show:true, width:18 },
    axisLine:{ lineStyle:{ width:18 } },
    axisTick:{ show:false }, axisLabel:{ show:false }, splitLine:{ show:false },
    detail:{ valueAnimation:true, formatter:'{value}个', color:'#409eff', fontSize:24 },
    data:[{ value:0 }]
  }]
})

const fetchData = async () => {
  loading.value = true
  try {
    const [listRes, countRes] = await Promise.all([listOnlineUsers(), getOnlineCount()])
    users.value = listRes.data || []
    const val = countRes.data ?? users.value.length
    gaugeOption.series[0].data[0].value = val
    gaugeOption.series[0].max = Math.max(100, val + 10)
  } finally { loading.value = false }
}

onMounted(()=>{
  fetchData()
  timer.value = setInterval(fetchData, 5000)
})

onBeforeUnmount(()=>{ clearInterval(timer.value) })

const handleOffline = (uid) => {
  ElMessageBox.confirm(`确认将用户 ${uid} 下线?`, '提示', { type:'warning' }).then(async ()=>{
    await offlineUser(uid)
    ElMessage.success('已下线')
    fetchData()
  }).catch(()=>{})
}
</script>

<template>
  <div class="online-page">
    <v-chart class="gauge" :option="gaugeOption" autoresize />
    <el-table :data="users" border style="width:600px" max-height="400" v-loading="loading">
      <el-table-column prop="" label="用户ID">
        <template #default="{row}">{{ row }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{row}">
          <el-button type="danger" size="small" @click="handleOffline(row)">下线</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.online-page{
  display:flex;
  flex-direction:column;
  align-items:center;
  padding:24px;
}
.gauge{ width:400px;height:260px;margin-bottom:24px; }
</style> 