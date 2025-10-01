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
      
      // Sa-Token 自动续签机制
      // Sa-Token 在后端配置了 activity-timeout（30分钟无操作过期）和 timeout（7天总过期时间）
      // 每次请求都会自动续签，无需前端手动刷新 Token
      // 如果需要主动刷新，可以调用 /user/auth/refresh 接口
    }
    
    return config
  },
  (error) => {
    NProgress.done()
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
// 注意：Sa-Token 已经在后端实现了自动续签机制
// activity-timeout: 30分钟内有操作就自动续签
// timeout: 7天总过期时间
// 前端无需手动刷新 Token，Sa-Token 会自动处理
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