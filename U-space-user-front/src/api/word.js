import request from '../utils/request'

// 分页查看单词
export function getWordPage(data) {
  return request({
    url: '/word/page',
    method: 'post',
    data
  })
}

// 用户对单词选择认识or不认识
export function selectWord(data) {
  return request({
    url: '/word/select',
    method: 'post',
    data
  })
}

// 分页查看用户对单词的操作记录
export function getUserWordPage(data) {
  return request({
    url: '/word/pageUserSelect',
    method: 'post',
    data
  })
}

// 分页查看用户倾向认识的单词记录
export function getUserWordPageKnown(data) {
  return request({
    url: '/word/pageUserSelectKnown',
    method: 'post',
    data
  })
}

// 分页查看用户倾向不认识的单词记录
export function getUserWordPageUnknown(data) {
  return request({
    url: '/word/pageUserSelectUnknown',
    method: 'post',
    data
  })
} 