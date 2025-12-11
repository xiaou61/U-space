import request from '@/utils/request'

export const flashcardApi = {
  // ============ 卡组管理 ============

  // 获取官方卡组列表
  getOfficialDecks() {
    return request.get('/admin/flashcard/decks')
  },

  // 获取卡组详情
  getDeckById(deckId) {
    return request.get(`/admin/flashcard/decks/${deckId}`)
  },

  // 创建官方卡组
  createDeck(data) {
    return request.post('/admin/flashcard/decks', data)
  },

  // 更新卡组
  updateDeck(deckId, data) {
    return request.put(`/admin/flashcard/decks/${deckId}`, data)
  },

  // 删除卡组
  deleteDeck(deckId) {
    return request.delete(`/admin/flashcard/decks/${deckId}`)
  },

  // 获取所有公开卡组
  getPublicDecks() {
    return request.get('/admin/flashcard/decks/public')
  },

  // ============ 闪卡管理 ============

  // 获取卡组下的闪卡列表
  getCardsByDeck(deckId) {
    return request.get(`/admin/flashcard/decks/${deckId}/cards`)
  },

  // 创建闪卡
  createCard(data) {
    return request.post('/admin/flashcard/cards', data)
  },

  // 更新闪卡
  updateCard(cardId, data) {
    return request.put(`/admin/flashcard/cards/${cardId}`, data)
  },

  // 删除闪卡
  deleteCard(cardId) {
    return request.delete(`/admin/flashcard/cards/${cardId}`)
  },

  // 从题库分类批量生成闪卡
  generateFromCategory(data) {
    return request.post('/admin/flashcard/generate/category', data)
  }
}

export default flashcardApi
