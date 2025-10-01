import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(Cookies.get('user_token') || '')
  const userInfo = ref(null)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      Cookies.set('user_token', newToken, { expires: 7 })
      localStorage.setItem('user_token', newToken)
    } else {
      Cookies.remove('user_token')
      localStorage.removeItem('user_token')
    }
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem('user_info', JSON.stringify(info))
    } else {
      localStorage.removeItem('user_info')
    }
  }

  // 登录
  const login = (tokenData, userData) => {
    setToken(tokenData)
    setUserInfo(userData)
  }

  // 登出
  const logout = () => {
    setToken('')
    setUserInfo(null)
  }

  // 初始化：从 localStorage 恢复数据
  const initUserData = () => {
    try {
      const savedToken = localStorage.getItem('user_token')
      const savedUserInfo = localStorage.getItem('user_info')
      
      if (savedToken && savedUserInfo && savedUserInfo !== 'undefined' && savedUserInfo !== 'null') {
        token.value = savedToken
        userInfo.value = JSON.parse(savedUserInfo)
        // 同步到 Cookie
        Cookies.set('user_token', savedToken, { expires: 7 })
      }
    } catch (error) {
      console.error('恢复用户数据失败:', error)
      logout()
    }
  }

  // 页面加载时恢复数据
  if (!userInfo.value) {
    initUserData()
  }

  // 获取器（为了兼容性保留函数形式）
  const isLogin = () => {
    return !!token.value && !!userInfo.value
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    login,
    logout,
    isLogin,
    initUserData
  }
}) 