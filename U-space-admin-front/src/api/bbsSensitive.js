import request from '../utils/request'

// 获取全部敏感词（返回 Map<String, Integer>）
export const listSensitiveWords = () => request.post('/bbs/sensitive/list')

// 新增敏感词 { word, level }
export const addSensitiveWord = (data) => request.post('/bbs/sensitive/add', data)

// 删除敏感词，后端要求原始字符串作为请求体
export const deleteSensitiveWord = (word) => request.post('/bbs/sensitive/delete', word, { headers: { 'Content-Type': 'text/plain' } }) 