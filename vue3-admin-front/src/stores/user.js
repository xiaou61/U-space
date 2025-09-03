import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(Cookies.get('token') || '')
  const userInfo = ref(null)
  const tokenExpireTime = ref(null) // token过期时间
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // 检查token是否即将过期（30分钟内过期）
  const isTokenExpiringSoon = computed(() => {
    if (!tokenExpireTime.value) return false
    const now = Date.now()
    const expireTime = new Date(tokenExpireTime.value).getTime()
    return expireTime - now < 30 * 60 * 1000 // 30分钟
  })
  
  // Actions
  
  // 登录
  async function login(loginForm) {
    try {
      const response = await authApi.login(loginForm)
      
      // 保存token和过期时间
      setToken(response.accessToken, response.expireTime)
      
      // 保存用户信息
      setUserInfo(response.userInfo)
      
      // 广播登录事件到其他tab页
      broadcastAuthEvent('login')
      
      return response
    } catch (error) {
      throw error
    }
  }
  
  // 获取用户信息
  async function getUserInfo() {
    try {
      const response = await authApi.getUserInfo()
      setUserInfo(response)
      return response
    } catch (error) {
      // 如果获取用户信息失败，清除本地数据
      logout()
      throw error
    }
  }
  
  // 刷新Token
  async function refreshToken() {
    try {
      const newToken = await authApi.refreshToken()
      setToken(newToken)
      return newToken
    } catch (error) {
      // 刷新失败，登出用户
      logout()
      throw error
    }
  }
  
  // 登出
  async function logout() {
    try {
      // 调用后端登出接口
      if (token.value) {
        await authApi.logout()
      }
    } catch (error) {
      console.error('登出接口调用失败:', error)
    } finally {
      // 无论接口是否成功，都清除本地数据
      clearUserData()
      // 广播登出事件到其他tab页
      broadcastAuthEvent('logout')
    }
  }
  
  // 设置Token
  function setToken(newToken, expireTime = null) {
    token.value = newToken
    tokenExpireTime.value = expireTime
    
    // 设置cookie过期时间为7天或token过期时间（取较小值）
    const cookieExpireDays = expireTime ? 
      Math.min(7, Math.ceil((new Date(expireTime).getTime() - Date.now()) / (24 * 60 * 60 * 1000))) : 7
    
    Cookies.set('token', newToken, { expires: cookieExpireDays })
    
    if (expireTime) {
      localStorage.setItem('tokenExpireTime', expireTime)
    }
  }
  
  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }
  
  // 清除用户数据
  function clearUserData() {
    token.value = ''
    userInfo.value = null
    tokenExpireTime.value = null
    
    Cookies.remove('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('tokenExpireTime')
  }
  
  // 初始化用户数据（从本地存储恢复）
  function initUserData() {
    try {
      const savedUserInfo = localStorage.getItem('userInfo')
      const savedExpireTime = localStorage.getItem('tokenExpireTime')
      
      if (savedUserInfo) {
        const info = JSON.parse(savedUserInfo)
        setUserInfo(info)
      }
      
      if (savedExpireTime) {
        tokenExpireTime.value = savedExpireTime
        
        // 检查token是否已过期
        if (new Date(savedExpireTime).getTime() <= Date.now()) {
          clearUserData()
          return
        }
      }
    } catch (error) {
      console.error('恢复用户数据失败:', error)
      clearUserData()
    }
  }
  
  // 更新个人信息
  async function updateProfile(profileData) {
    try {
      await authApi.updateProfile(profileData)
      // 重新获取用户信息
      await getUserInfo()
      return true
    } catch (error) {
      throw error
    }
  }
  
  // 修改密码
  async function changePassword(passwordData) {
    try {
      await authApi.changePassword(passwordData)
      // 修改密码后自动登出
      await logout()
      return true
    } catch (error) {
      throw error
    }
  }
  
  // 广播认证事件到其他tab页
  function broadcastAuthEvent(type, data = null) {
    const event = new CustomEvent('authStateChange', {
      detail: { type, data, timestamp: Date.now() }
    })
    window.dispatchEvent(event)
  }
  
  // 监听其他tab页的认证事件
  function setupAuthEventListener() {
    window.addEventListener('authStateChange', (event) => {
      const { type, timestamp } = event.detail
      
      // 防止事件循环
      if (Date.now() - timestamp > 1000) return
      
      if (type === 'logout') {
        // 其他tab页登出，当前页面也清除数据
        clearUserData()
        // 如果当前页面不是登录页，则跳转到登录页
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    })
  }
  
  // 检查并刷新token
  async function checkAndRefreshToken() {
    if (!token.value || !isLoggedIn.value) return false
    
    try {
      // 如果token即将过期，尝试刷新
      if (isTokenExpiringSoon.value) {
        console.log('Token即将过期，尝试刷新...')
        await refreshToken()
        return true
      }
      return true
    } catch (error) {
      console.error('刷新token失败:', error)
      return false
    }
  }

  // 页面刷新时恢复数据
  if (token.value && !userInfo.value) {
    initUserData()
  }
  
  // 设置认证事件监听
  setupAuthEventListener()

  return {
    // State
    token,
    userInfo,
    tokenExpireTime,
    
    // Getters
    isLoggedIn,
    username,
    realName,
    avatar,
    roles,
    permissions,
    isTokenExpiringSoon,
    
    // Actions
    login,
    logout,
    getUserInfo,
    refreshToken,
    setToken,
    setUserInfo,
    clearUserData,
    initUserData,
    updateProfile,
    changePassword,
    checkAndRefreshToken,
    broadcastAuthEvent,
  }
}) 