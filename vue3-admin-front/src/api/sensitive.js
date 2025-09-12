import request from '@/utils/request'

// 分页查询敏感词列表
export function listSensitiveWords(data) {
  return request.post('/admin/sensitive/words/list', data)
}

// 根据ID查询敏感词
export function getSensitiveWordById(id) {
  return request.post(`/admin/sensitive/words/${id}`)
}

// 新增敏感词
export function addSensitiveWord(data) {
  return request.post('/admin/sensitive/words', data)
}

// 更新敏感词
export function updateSensitiveWord(data) {
  return request.post('/admin/sensitive/words/update', data)
}

// 删除敏感词
export function deleteSensitiveWord(id) {
  return request.post(`/admin/sensitive/words/delete/${id}`)
}

// 批量删除敏感词
export function deleteSensitiveWords(ids) {
  return request.post('/admin/sensitive/words/delete/batch', ids)
}

// 批量导入敏感词
export function importSensitiveWords(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/sensitive/words/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 刷新敏感词库
export function refreshWordLibrary() {
  return request.post('/admin/sensitive/refresh')
}

// 查询敏感词分类列表
export function listSensitiveCategories() {
  return request.post('/admin/sensitive/categories')
} 