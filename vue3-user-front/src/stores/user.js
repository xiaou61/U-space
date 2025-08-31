import { defineStore } from 'pinia'
import { ref } from 'vue'
import Cookies from 'js-cookie'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(Cookies.get('user_token') || '')
  const userInfo = ref(null)

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      Cookies.set('user_token', newToken, { expires: 7 }) // 7天过期
    } else {
      Cookies.remove('user_token')
    }
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
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

  // 获取器
  const isLogin = () => {
    return !!token.value
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    login,
    logout,
    isLogin
  }
}) 