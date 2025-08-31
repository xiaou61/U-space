import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Cookies from 'js-cookie'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(Cookies.get('token') || '')
  const userInfo = ref(null)
  
  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const roles = computed(() => userInfo.value?.roles || [])
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // Actions
  
  // 登录
  async function login(loginForm) {
    try {
      const response = await authApi.login(loginForm)
      
      // 保存token
      setToken(response.accessToken)
      
      // 保存用户信息
      setUserInfo(response.userInfo)
      
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
    }
  }
  
  // 设置Token
  function setToken(newToken) {
    token.value = newToken
    Cookies.set('token', newToken, { expires: 7 })
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
    
    Cookies.remove('token')
    localStorage.removeItem('userInfo')
  }
  
  // 初始化用户数据（从本地存储恢复）
  function initUserData() {
    try {
      const savedUserInfo = localStorage.getItem('userInfo')
      if (savedUserInfo) {
        const info = JSON.parse(savedUserInfo)
        setUserInfo(info)
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
  
  // 页面刷新时恢复数据
  if (token.value && !userInfo.value) {
    initUserData()
  }
  
  return {
    // State
    token,
    userInfo,
    
    // Getters
    isLoggedIn,
    username,
    realName,
    avatar,
    roles,
    permissions,
    
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
  }
}) 