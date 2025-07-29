import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import { initTheme } from './utils/theme'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')

initTheme()
