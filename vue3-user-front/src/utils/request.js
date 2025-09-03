import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'
import NProgress from 'nprogress'

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    NProgress.start()
    
    // 从store中获取token
    const userStore = useUserStore()
    const token = userStore.token
    
    // 如果有token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      
      // 检查Token是否即将过期（在过期前1天自动刷新）
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        const exp = payload.exp * 1000 // 转换为毫秒
        const now = Date.now()
        const oneDayInMs = 24 * 60 * 60 * 1000 // 1天的毫秒数
        
        // 如果Token在1天内过期，且不是刷新Token的请求，则尝试刷新
        if (exp - now < oneDayInMs && !config.url.includes('/auth/refresh')) {
          console.log('Token即将过期，准备自动刷新')
          // 这里不阻塞当前请求，让刷新在后台进行
          refreshTokenInBackground()
        }
      } catch (error) {
        console.warn('解析Token过期时间失败:', error)
      }
    }
    
    return config
  },
  (error) => {
    NProgress.done()
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 后台刷新Token（防止重复刷新）
let isRefreshing = false
let refreshPromise = null

function refreshTokenInBackground() {
  if (isRefreshing) {
    return refreshPromise
  }
  
  isRefreshing = true
  refreshPromise = service.post('/user/auth/refresh')
    .then(response => {
      const userStore = useUserStore()
      userStore.setToken(response.accessToken)
      console.log('Token自动刷新成功')
      return response.accessToken
    })
    .catch(error => {
      console.warn('Token自动刷新失败:', error)
      // 刷新失败时不做处理，让正常的错误处理机制处理
    })
    .finally(() => {
      isRefreshing = false
      refreshPromise = null
    })
  
  return refreshPromise
}

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    NProgress.done()
    
    const { data, status } = response
    
    // HTTP状态码检查
    if (status !== 200) {
      ElMessage.error(`HTTP错误: ${status}`)
      return Promise.reject(new Error(`HTTP Error: ${status}`))
    }
    
    // 业务状态码检查
    if (data.code !== undefined) {
      const { code, message, data: responseData } = data
      
      // 成功响应
      if (code === 200) {
        return responseData
      }
      
      // Token相关错误
      if (code === 701 || code === 702) {
        handleTokenError(message)
        return Promise.reject(new Error(message))
      }
      
      // 账户被禁用
      if (code === 704) {
        ElMessage.error(message)
        handleLogout()
        return Promise.reject(new Error(message))
      }
      
      // 其他业务错误
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
    
    return data
  },
  (error) => {
    NProgress.done()
    
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          handleTokenError('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络连接异常，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

// 处理Token错误
function handleTokenError(message) {
  ElMessageBox.alert(message, '提示', {
    confirmButtonText: '重新登录',
    type: 'warning',
    showClose: false,
  }).then(() => {
    handleLogout()
  })
}

// 处理登出
function handleLogout() {
  const userStore = useUserStore()
  userStore.logout()
  router.push('/login')
}

// 封装常用的请求方法
const request = {
  get(url, params = {}, config = {}) {
    return service({
      method: 'GET',
      url,
      params,
      ...config,
    })
  },
  
  post(url, data = {}, config = {}) {
    return service({
      method: 'POST',
      url,
      data,
      ...config,
    })
  },
  
  put(url, data = {}, config = {}) {
    return service({
      method: 'PUT',
      url,
      data,
      ...config,
    })
  },
  
  delete(url, params = {}, config = {}) {
    return service({
      method: 'DELETE',
      url,
      params,
      ...config,
    })
  },
}

export default request 