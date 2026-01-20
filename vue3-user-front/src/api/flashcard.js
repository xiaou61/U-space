import request from '@/utils/request'

export const flashcardApi = {
  // ============ 卡组管理 ============
  
  // 创建卡组
  createDeck(data) {
    return request.post('/flashcard/deck', data)
  },

  // 更新卡组
  updateDeck(data) {
    return request.put('/flashcard/deck', data)
  },

  // 删除卡组
  deleteDeck(id) {
    return request.delete(`/flashcard/deck/${id}`)
  },

  // 获取卡组详情
  getDeckById(id) {
    return request.get(`/flashcard/deck/${id}`)
  },

  // 获取我的卡组列表
  getMyDecks() {
    return request.get('/flashcard/deck/my')
  },

  // 复制(Fork)卡组
  forkDeck(id) {
    return request.post(`/flashcard/deck/${id}/fork`)
  },

  // ============ 闪卡管理 ============

  // 创建闪卡
  createCard(data) {
    return request.post('/flashcard/card', data)
  },

  // 批量创建闪卡
  batchCreateCards(data) {
    return request.post('/flashcard/card/batch', data)
  },

  // 更新闪卡
  updateCard(id, data) {
    return request.put(`/flashcard/card/${id}`, data)
  },

  // 删除闪卡
  deleteCard(id) {
    return request.delete(`/flashcard/card/${id}`)
  },

  // 获取卡组内的闪卡列表
  getCardsByDeckId(deckId) {
    return request.get(`/flashcard/card/deck/${deckId}`)
  },

  // 从面试题库导入闪卡
  importFromQuestionBank(data) {
    return request.post('/flashcard/card/import', data)
  },

  // ============ 学习 ============

  // 获取今日待学习卡片
  getTodayStudyCards(limit = 20) {
    return request.get('/flashcard/study/today', { params: { limit } })
  },

  // 获取指定卡组的下一张待学习卡片
  getNextCard(deckId) {
    return request.get(`/flashcard/study/deck/${deckId}/next`)
  },

  // 提交学习结果
  submitStudyResult(data) {
    return request.post('/flashcard/study/submit', data)
  },

  // 获取学习统计
  getStudyStats(deckId = null) {
    const params = {}
    if (deckId) params.deckId = deckId
    return request.get('/flashcard/study/stats', { params })
  },

  // 获取学习热力图数据
  getHeatmap(days = 365) {
    return request.get('/flashcard/study/heatmap', { params: { days } })
  },

  // ============ 公开接口 ============

  // 获取公开卡组列表
  getPublicDecks(keyword = '', tags = '') {
    const params = {}
    if (keyword) params.keyword = keyword
    if (tags) params.tags = tags
    return request.get('/pub/flashcard/deck/list', { params })
  },

  // 获取公开卡组详情
  getPublicDeckById(id) {
    return request.get(`/pub/flashcard/deck/${id}`)
  },

  // 获取公开卡组的闪卡列表
  getPublicDeckCards(id) {
    return request.get(`/pub/flashcard/deck/${id}/cards`)
  }
}
