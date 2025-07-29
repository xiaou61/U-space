import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/global.css'
import { connectSSE } from './utils/sse'

const app = createApp(App)

app.use(router)
app.use(ElementPlus)

app.mount('#app')

// 尝试建立 SSE 连接（登录后刷新即触发）
connectSSE()

// 每次路由切换尝试（解决登录后跳转）
router.afterEach(() => {
  connectSSE()
}) 