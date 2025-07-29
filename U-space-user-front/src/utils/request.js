import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// baseURL is read from VITE_API_BASE environment variable or defaults to backend path
const baseURL = import.meta.env.VITE_API_BASE || '/uapi'

const http = axios.create({
  baseURL,
  timeout: 10000,
})

// Request interceptor: attach token if present in localStorage
http.interceptors.request.use(
  (config) => {
    const tokenName = localStorage.getItem('tokenName')
    const tokenValue = localStorage.getItem('tokenValue')
    console.log('Request interceptor:', config.url, 'Token:', tokenName, !!tokenValue)
    if (tokenName && tokenValue) {
      // Sa-Token 约定：将 tokenName 作为 header 名称，tokenValue 作为值
      config.headers[tokenName] = tokenValue
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor: unified business code & login invalidation handling
http.interceptors.response.use(
  (response) => {
    const res = response.data
    console.log('Response interceptor:', response.config.url, 'Code:', res?.code)
    
    // 特殊处理登录接口，仍然需要校验业务 code，避免后端 500 时前端误判成功
    if (response.config.url.includes('/student/auth/login')) {
      if (res && typeof res.code !== 'undefined' && res.code !== 200) {
        ElMessage.error(res.msg || '登录失败')
        return Promise.reject(res)
      }
      return res
    }
    
    // 假设后端统一返回 R 对象 { code, data, msg }
    if (res && typeof res.code !== 'undefined' && res.code !== 200) {
      console.warn('业务错误码:', res.code, res.msg)
      ElMessage.error(res.msg || '请求错误')
      if (res.code === 401 || res.code === 40100) {
        console.error('检测到401未授权，清除token并跳转登录页')
        localStorage.removeItem('tokenName')
        localStorage.removeItem('tokenValue')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    console.error('Request error:', error.config?.url, error.message)
    if (error.response && error.response.status === 401) {
      console.warn('401 Unauthorized, clearing token and redirecting to login')
      localStorage.removeItem('tokenName')
      localStorage.removeItem('tokenValue')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default http 