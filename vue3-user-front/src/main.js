import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'nprogress/nprogress.css'

import App from './App.vue'
import router from './router'
import './styles/index.scss'
import './styles/markdown.scss'

// 预加载面试数据
import { useInterviewStore } from '@/stores/interview'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')

// 预加载面试数据
router.isReady().then(() => {
  const interviewStore = useInterviewStore()
  interviewStore.preloadData()
}) 