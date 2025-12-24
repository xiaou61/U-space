import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { interviewApi } from '@/api/interview'
import { cachedRequest, invalidateCache } from '@/utils/cache'

export const useInterviewStore = defineStore('interview', () => {
  // ================== 状态管理 ==================
  
  // 分类数据
  const categories = ref([])
  const categoriesLoading = ref(false)
  
  // 题单数据
  const questionSets = ref([])
  const questionSetsLoading = ref(false)
  const questionSetsTotal = ref(0)
  
  // 当前题单详情
  const currentQuestionSet = ref({})
  const currentQuestionSetLoading = ref(false)
  
  // 题目列表
  const questions = ref([])
  const questionsLoading = ref(false)
  
  // 当前题目详情
  const currentQuestion = ref({})
  const currentQuestionLoading = ref(false)
  
  // 收藏状态缓存
  const favoriteStatus = ref(new Map())
  
  // ================== 计算属性 ==================
  
  // 根据分类ID获取分类信息
  const getCategoryById = computed(() => {
    return (id) => categories.value.find(cat => cat.id === id)
  })
  
  // 根据ID获取题单信息
  const getQuestionSetById = computed(() => {
    return (id) => questionSets.value.find(qs => qs.id === id) || currentQuestionSet.value
  })
  
  // 根据ID获取题目信息
  const getQuestionById = computed(() => {
    return (id) => questions.value.find(q => q.id === id) || currentQuestion.value
  })
  
  // ================== 分类相关方法 ==================
  
  /**
   * 获取分类列表
   */
  const fetchCategories = async (options = {}) => {
    const cacheKey = 'interview/categories'
    
    categoriesLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.getEnabledCategories(),
        cacheKey,
        { ttl: 10 * 60 * 1000, ...options } // 分类缓存10分钟
      )
      
      categories.value = data || []
      return data
    } catch (error) {
      console.error('获取分类列表失败:', error)
      throw error
    } finally {
      categoriesLoading.value = false
    }
  }
  
  // ================== 题单相关方法 ==================
  
  /**
   * 获取公开题单列表
   */
const fetchPublicQuestionSets = async (params = {}, options = {}) => {
    const cacheKey = `interview/question-sets/public?${JSON.stringify(params)}`
    
    questionSetsLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.getPublicQuestionSets(params),
        cacheKey,
        { ttl: 3 * 60 * 1000, force: options.forceRefresh, ...options } // 题单列表缓存3分钟
      )
      
      if (params.page === 1) {
        questionSets.value = data?.records || []
      } else {
        questionSets.value.push(...(data?.records || []))
      }
      questionSetsTotal.value = data?.total || 0
      
      return data
    } catch (error) {
      console.error('获取题单列表失败:', error)
      throw error
    } finally {
      questionSetsLoading.value = false
    }
  }
  
  /**
   * 搜索题单
   */
const searchQuestions = async (params = {}, options = {}) => {
    const cacheKey = `interview/questions/search?${JSON.stringify(params)}`
    
    questionSetsLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.searchQuestions(params),
        cacheKey,
        { ttl: 2 * 60 * 1000, force: options.forceRefresh, ...options } // 搜索结果缓存2分钟
      )
      
      // 将搜索到的题目转换为题单格式显示
      const questionSetsFromSearch = data?.records?.map(question => ({
        id: `q-${question.id}`,
        title: question.title,
        description: question.answer ? question.answer.substring(0, 100) + '...' : '暂无答案',
        questionCount: 1,
        viewCount: 0,
        type: 1,
        categoryName: '搜索结果',
        originalQuestion: question
      })) || []
      
      questionSets.value = questionSetsFromSearch
      questionSetsTotal.value = data?.total || 0
      
      return data
    } catch (error) {
      console.error('搜索题目失败:', error)
      throw error
    } finally {
      questionSetsLoading.value = false
    }
  }
  
  /**
   * 获取题单详情
   */
  const fetchQuestionSetById = async (id, options = {}) => {
    const cacheKey = `interview/question-set/${id}`
    
    currentQuestionSetLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.getQuestionSetById(id),
        cacheKey,
        { ttl: 5 * 60 * 1000, ...options } // 题单详情缓存5分钟
      )
      
      currentQuestionSet.value = data || {}
      
      // 同时更新列表中的数据
      const index = questionSets.value.findIndex(qs => qs.id === id)
      if (index !== -1) {
        questionSets.value[index] = { ...questionSets.value[index], ...data }
      }
      
      return data
    } catch (error) {
      console.error('获取题单详情失败:', error)
      throw error
    } finally {
      currentQuestionSetLoading.value = false
    }
  }
  
  // ================== 题目相关方法 ==================
  
  /**
   * 获取题目列表
   */
  const fetchQuestionsBySetId = async (setId, options = {}) => {
    const cacheKey = `interview/questions/set/${setId}`
    
    questionsLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.getQuestionsBySetId(setId),
        cacheKey,
        { ttl: 5 * 60 * 1000, ...options } // 题目列表缓存5分钟
      )
      
      questions.value = data || []
      return data
    } catch (error) {
      console.error('获取题目列表失败:', error)
      throw error
    } finally {
      questionsLoading.value = false
    }
  }
  
  /**
   * 获取题目详情
   */
  const fetchQuestionById = async (setId, questionId, options = {}) => {
    const cacheKey = `interview/question/${setId}/${questionId}`
    
    currentQuestionLoading.value = true
    try {
      const data = await cachedRequest(
        () => interviewApi.getQuestionById(setId, questionId),
        cacheKey,
        { ttl: 10 * 60 * 1000, ...options } // 题目详情缓存10分钟
      )
      
      currentQuestion.value = data || {}
      
      // 同时更新列表中的数据
      const index = questions.value.findIndex(q => q.id === questionId)
      if (index !== -1) {
        questions.value[index] = { ...questions.value[index], ...data }
      }
      
      return data
    } catch (error) {
      console.error('获取题目详情失败:', error)
      throw error
    } finally {
      currentQuestionLoading.value = false
    }
  }
  
  /**
   * 获取上一题
   */
  const fetchPrevQuestion = async (setId, questionId, options = {}) => {
    const cacheKey = `interview/question/${setId}/${questionId}/prev`
    
    try {
      return await cachedRequest(
        () => interviewApi.getPrevQuestion(setId, questionId),
        cacheKey,
        { ttl: 5 * 60 * 1000, ...options }
      )
    } catch (error) {
      console.error('获取上一题失败:', error)
      throw error
    }
  }
  
  /**
   * 获取下一题
   */
  const fetchNextQuestion = async (setId, questionId, options = {}) => {
    const cacheKey = `interview/question/${setId}/${questionId}/next`
    
    try {
      return await cachedRequest(
        () => interviewApi.getNextQuestion(setId, questionId),
        cacheKey,
        { ttl: 5 * 60 * 1000, ...options }
      )
    } catch (error) {
      console.error('获取下一题失败:', error)
      throw error
    }
  }
  
  // ================== 收藏相关方法 ==================
  
  /**
   * 检查收藏状态
   */
  const checkFavoriteStatus = async (targetType, targetId, options = {}) => {
    const cacheKey = `interview/favorite/${targetType}/${targetId}`
    const statusKey = `${targetType}-${targetId}`
    
    try {
      const favorited = await cachedRequest(
        () => interviewApi.isFavorited(targetType, targetId),
        cacheKey,
        { ttl: 2 * 60 * 1000, ...options } // 收藏状态缓存2分钟
      )
      
      favoriteStatus.value.set(statusKey, favorited)
      return favorited
    } catch (error) {
      console.error('检查收藏状态失败:', error)
      return false
    }
  }
  
  /**
   * 添加收藏
   */
  const addFavorite = async (targetType, targetId) => {
    try {
      await interviewApi.addFavorite(targetType, targetId)
      
      // 更新本地状态
      const statusKey = `${targetType}-${targetId}`
      favoriteStatus.value.set(statusKey, true)
      
      // 清理相关缓存
      invalidateCache([
        `interview/favorite/${targetType}/${targetId}`,
        'interview/favorites'
      ])
      
      return true
    } catch (error) {
      console.error('添加收藏失败:', error)
      throw error
    }
  }
  
  /**
   * 取消收藏
   */
  const removeFavorite = async (targetType, targetId) => {
    try {
      await interviewApi.removeFavorite(targetType, targetId)
      
      // 更新本地状态
      const statusKey = `${targetType}-${targetId}`
      favoriteStatus.value.set(statusKey, false)
      
      // 清理相关缓存
      invalidateCache([
        `interview/favorite/${targetType}/${targetId}`,
        'interview/favorites'
      ])
      
      return true
    } catch (error) {
      console.error('取消收藏失败:', error)
      throw error
    }
  }
  
  /**
   * 获取收藏列表
   */
  const fetchFavorites = async (params = {}, options = {}) => {
    const cacheKey = `interview/favorites?${JSON.stringify(params)}`
    
    try {
      return await cachedRequest(
        () => interviewApi.getFavorites(params),
        cacheKey,
        { ttl: 1 * 60 * 1000, ...options } // 收藏列表缓存1分钟
      )
    } catch (error) {
      console.error('获取收藏列表失败:', error)
      throw error
    }
  }
  
  // ================== 缓存管理方法 ==================
  
  /**
   * 清理指定缓存
   */
  const clearCache = (patterns) => {
    invalidateCache(patterns)
  }
  
  /**
   * 预加载相关数据
   */
  const preloadData = async () => {
    try {
      // 预加载分类列表
      await fetchCategories()
    } catch (error) {
      console.error('预加载数据失败:', error)
    }
  }
  
  /**
   * 重置所有状态
   */
  const resetState = () => {
    categories.value = []
    questionSets.value = []
    questions.value = []
    currentQuestionSet.value = {}
    currentQuestion.value = {}
    favoriteStatus.value.clear()
    questionSetsTotal.value = 0
  }
  
  return {
    // 状态
    categories,
    categoriesLoading,
    questionSets,
    questionSetsLoading,
    questionSetsTotal,
    currentQuestionSet,
    currentQuestionSetLoading,
    questions,
    questionsLoading,
    currentQuestion,
    currentQuestionLoading,
    favoriteStatus,
    
    // 计算属性
    getCategoryById,
    getQuestionSetById,
    getQuestionById,
    
    // 方法
    fetchCategories,
    fetchPublicQuestionSets,
    searchQuestions,
    fetchQuestionSetById,
    fetchQuestionsBySetId,
    fetchQuestionById,
    fetchPrevQuestion,
    fetchNextQuestion,
    checkFavoriteStatus,
    addFavorite,
    removeFavorite,
    fetchFavorites,
    clearCache,
    preloadData,
    resetState
  }
}) 