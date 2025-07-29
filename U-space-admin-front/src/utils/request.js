import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/uapi', // 根据需要修改
  timeout: 10000
})

// 请求拦截器：携带 token
service.interceptors.request.use(
  config => {
    const tokenName = localStorage.getItem('tokenName')
    const tokenValue = localStorage.getItem('tokenValue')
    if (tokenName && tokenValue) {
      config.headers[tokenName] = tokenValue
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理业务码 & 未登录
service.interceptors.response.use(
  response => {
    const res = response.data
    // 以后端 R 对象为准：成功 code==200
    if (res.code && res.code !== 200) {
      ElMessage.error(res.msg || '请求错误')
      // 未登录 / token 失效
      if (res.code === 401 || res.code === 40100) {
        localStorage.removeItem('tokenName')
        localStorage.removeItem('tokenValue')
        router.push('/login')
      }
      return Promise.reject(res)
    }
    return res
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('tokenName')
      localStorage.removeItem('tokenValue')
      router.push('/login')
    }
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service 