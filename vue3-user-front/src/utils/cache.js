/**
 * API缓存工具类
 * 支持内存缓存、过期时间、LRU清理
 */
class ApiCache {
  constructor(options = {}) {
    this.maxSize = options.maxSize || 100 // 最大缓存条目数
    this.defaultTtl = options.defaultTtl || 5 * 60 * 1000 // 默认5分钟过期
    this.cache = new Map()
    this.accessOrder = new Map() // 用于LRU
    this.timers = new Map() // 定时器清理
  }

  /**
   * 生成缓存key
   */
  generateKey(url, params = {}) {
    const sortedParams = Object.keys(params)
      .sort()
      .reduce((result, key) => {
        result[key] = params[key]
        return result
      }, {})
    
    return `${url}?${JSON.stringify(sortedParams)}`
  }

  /**
   * 设置缓存
   */
  set(key, value, ttl = this.defaultTtl) {
    // 如果缓存已满，删除最少使用的
    if (this.cache.size >= this.maxSize) {
      this.evictLru()
    }

    // 清除可能存在的定时器
    if (this.timers.has(key)) {
      clearTimeout(this.timers.get(key))
    }

    // 设置缓存项
    const cacheItem = {
      value,
      timestamp: Date.now(),
      ttl,
      hit: 0
    }

    this.cache.set(key, cacheItem)
    this.accessOrder.set(key, Date.now())

    // 设置过期定时器
    if (ttl > 0) {
      const timer = setTimeout(() => {
        this.delete(key)
      }, ttl)
      this.timers.set(key, timer)
    }

    return value
  }

  /**
   * 获取缓存
   */
  get(key) {
    const item = this.cache.get(key)
    
    if (!item) {
      return null
    }

    // 检查是否过期
    if (item.ttl > 0 && Date.now() - item.timestamp > item.ttl) {
      this.delete(key)
      return null
    }

    // 更新访问记录
    item.hit++
    this.accessOrder.set(key, Date.now())

    return item.value
  }

  /**
   * 删除缓存
   */
  delete(key) {
    if (this.timers.has(key)) {
      clearTimeout(this.timers.get(key))
      this.timers.delete(key)
    }
    
    this.cache.delete(key)
    this.accessOrder.delete(key)
  }

  /**
   * 清空缓存
   */
  clear() {
    // 清除所有定时器
    this.timers.forEach(timer => clearTimeout(timer))
    this.timers.clear()
    this.cache.clear()
    this.accessOrder.clear()
  }

  /**
   * LRU淘汰
   */
  evictLru() {
    let oldestKey = null
    let oldestTime = Infinity

    for (const [key, accessTime] of this.accessOrder) {
      if (accessTime < oldestTime) {
        oldestTime = accessTime
        oldestKey = key
      }
    }

    if (oldestKey) {
      this.delete(oldestKey)
    }
  }

  /**
   * 根据前缀清理缓存
   */
  clearByPrefix(prefix) {
    const keysToDelete = []
    
    for (const key of this.cache.keys()) {
      if (key.startsWith(prefix)) {
        keysToDelete.push(key)
      }
    }
    
    keysToDelete.forEach(key => this.delete(key))
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    const items = Array.from(this.cache.values())
    return {
      size: this.cache.size,
      maxSize: this.maxSize,
      hitCount: items.reduce((sum, item) => sum + item.hit, 0),
      oldestTimestamp: Math.min(...items.map(item => item.timestamp)),
      newestTimestamp: Math.max(...items.map(item => item.timestamp))
    }
  }
}

// 创建全局缓存实例
const apiCache = new ApiCache({
  maxSize: 200,
  defaultTtl: 5 * 60 * 1000 // 5分钟
})

// 请求去重 Map
const pendingRequests = new Map()

/**
 * 带缓存的API请求函数
 */
export async function cachedRequest(requestFn, cacheKey, options = {}) {
  const {
    ttl = 5 * 60 * 1000, // 缓存时间
    force = false, // 是否强制刷新
    dedupe = true // 是否去重
  } = options

  // 强制刷新时清除缓存
  if (force) {
    apiCache.delete(cacheKey)
  }

  // 先尝试从缓存获取
  const cached = apiCache.get(cacheKey)
  if (cached && !force) {
    return cached
  }

  // 请求去重处理
  if (dedupe && pendingRequests.has(cacheKey)) {
    return pendingRequests.get(cacheKey)
  }

  // 执行请求
  const requestPromise = requestFn()
    .then(result => {
      // 缓存结果
      apiCache.set(cacheKey, result, ttl)
      return result
    })
    .finally(() => {
      // 清除pending状态
      pendingRequests.delete(cacheKey)
    })

  // 记录pending请求
  if (dedupe) {
    pendingRequests.set(cacheKey, requestPromise)
  }

  return requestPromise
}

/**
 * 清理相关缓存
 */
export function invalidateCache(patterns) {
  if (typeof patterns === 'string') {
    patterns = [patterns]
  }
  
  patterns.forEach(pattern => {
    apiCache.clearByPrefix(pattern)
  })
}

/**
 * 手动设置缓存
 */
export function setCache(key, value, ttl) {
  return apiCache.set(key, value, ttl)
}

/**
 * 手动获取缓存
 */
export function getCache(key) {
  return apiCache.get(key)
}

/**
 * 清空所有缓存
 */
export function clearAllCache() {
  apiCache.clear()
}

/**
 * 获取缓存统计
 */
export function getCacheStats() {
  return apiCache.getStats()
}

export default {
  cachedRequest,
  invalidateCache,
  setCache,
  getCache,
  clearAllCache,
  getCacheStats
} 