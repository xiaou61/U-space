import request from '@/utils/request'

/**
 * 闪卡记忆系统相关API
 */
export const flashcardApi = {
  // ============ 卡组相关 ============

  // 获取我的卡组列表
  getMyDecks() {
    return request.get('/flashcard/decks')
  },

  // 获取官方卡组列表
  getOfficialDecks() {
    return request.get('/flashcard/decks/official')
  },

  // 创建卡组
  createDeck(data) {
    return request.post('/flashcard/decks', data)
  },

  // 更新卡组
  updateDeck(deckId, data) {
    return request.put(`/flashcard/decks/${deckId}`, data)
  },

  // 删除卡组
  deleteDeck(deckId) {
    return request.delete(`/flashcard/decks/${deckId}`)
  },

  // 获取卡组详情
  getDeckDetail(deckId) {
    return request.get(`/flashcard/decks/${deckId}`)
  },

  // ============ 闪卡相关 ============

  // 获取卡组下的闪卡列表
  getCardsByDeck(deckId) {
    return request.get(`/flashcard/cards/deck/${deckId}`)
  },

  // 创建闪卡
  createCard(data) {
    return request.post('/flashcard/cards', data)
  },

  // 从面试题生成闪卡
  generateFromInterview(data) {
    return request.post('/flashcard/cards/generate', data)
  },

  // 更新闪卡
  updateCard(cardId, data) {
    return request.put(`/flashcard/cards/${cardId}`, data)
  },

  // 删除闪卡
  deleteCard(cardId) {
    return request.delete(`/flashcard/cards/${cardId}`)
  },

  // ============ 学习相关 ============

  // 获取今日学习任务
  getTodayStudy() {
    return request.get('/flashcard/study/today')
  },

  // 开始学习
  startStudy(data) {
    return request.post('/flashcard/study/start', data)
  },

  // 获取下一张待学习卡片
  getNextCard(deckId, mode = 'daily') {
    return request.get('/flashcard/study/next', { params: { deckId, mode } })
  },

  // 提交复习结果
  submitReview(data) {
    return request.post('/flashcard/study/review', data)
  },

  // ============ 统计相关 ============

  // 获取学习统计概览
  getStatsOverview() {
    return request.get('/flashcard/stats/overview')
  },

  // 获取每日统计趋势
  getDailyTrend(days = 7) {
    return request.get('/flashcard/stats/daily', { params: { days } })
  },

  // 获取卡组学习进度
  getDeckProgress(deckId) {
    return request.get(`/flashcard/stats/deck/${deckId}`)
  }
}

export default flashcardApi
