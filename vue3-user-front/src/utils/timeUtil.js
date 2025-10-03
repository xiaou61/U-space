/**
 * 时间工具类
 * 相对时间显示
 */

/**
 * 将时间转换为相对时间
 * @param {String} dateTimeStr 时间字符串 格式：yyyy-MM-dd HH:mm:ss
 * @returns {String} 相对时间 如：刚刚、3分钟前、2小时前
 */
export function formatRelativeTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  
  try {
    const time = new Date(dateTimeStr.replace(/-/g, '/')) // 兼容iOS
    const now = new Date()
    const diff = now - time // 毫秒差
    
    const seconds = Math.floor(diff / 1000)
    
    // 1分钟内：刚刚
    if (seconds < 60) {
      return '刚刚'
    }
    
    // 60分钟内：X分钟前
    const minutes = Math.floor(seconds / 60)
    if (minutes < 60) {
      return `${minutes}分钟前`
    }
    
    // 24小时内：X小时前
    const hours = Math.floor(minutes / 60)
    if (hours < 24) {
      return `${hours}小时前`
    }
    
    // 7天内：X天前
    const days = Math.floor(hours / 24)
    if (days < 7) {
      return `${days}天前`
    }
    
    // 超过7天但在同一年：显示月-日
    if (time.getFullYear() === now.getFullYear()) {
      const month = String(time.getMonth() + 1).padStart(2, '0')
      const day = String(time.getDate()).padStart(2, '0')
      return `${month}-${day}`
    }
    
    // 超过1年：显示年-月-日
    const year = time.getFullYear()
    const month = String(time.getMonth() + 1).padStart(2, '0')
    const day = String(time.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (error) {
    console.error('时间格式化失败:', error)
    return dateTimeStr
  }
}

/**
 * 格式化完整时间
 * @param {String} dateTimeStr 时间字符串
 * @returns {String} 格式化后的时间
 */
export function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
  
  try {
    const time = new Date(dateTimeStr.replace(/-/g, '/'))
    const year = time.getFullYear()
    const month = String(time.getMonth() + 1).padStart(2, '0')
    const day = String(time.getDate()).padStart(2, '0')
    const hour = String(time.getHours()).padStart(2, '0')
    const minute = String(time.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hour}:${minute}`
  } catch (error) {
    return dateTimeStr
  }
}

