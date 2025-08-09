import request from '../utils/request'

// 添加单词
export const addWord = (data) => request.post('/admin/word/add', data)

// 批量添加单词
export const addBatchWord = (data) => request.post('/admin/word/addBatch', data)

// Excel导入单词 - 已改为前端处理Excel，直接使用批量添加接口

// 下载导入模板 - 已改为前端生成，不再需要后端API

// 导出所有单词
export const exportAllWords = () => {
  return request.get('/admin/word/exportExcel', {
    responseType: 'blob'
  })
}

// 修改单词
export const updateWord = (id, data) => request.post(`/admin/word/update?id=${id}`, data)

// 删除单词
export const deleteWord = (id) => request.delete(`/admin/word/delete?id=${id}`)

// 分页获取单词列表
export const listWordsPage = (data) => request.post('/admin/word/page', data) 