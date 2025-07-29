import http from '../utils/request'

export const listCategories = () => http.get('/admin/post-categeteory/list')

export const listPosts = (categoryId, pageNum = 1, pageSize = 10) => {
  const url = categoryId ? '/post/list' : '/post/listAll'
  const params = categoryId ? { params: { categoryId } } : {}
  return http.post(url, { pageNum, pageSize }, params)
}

export const addPost = (data) => http.post('/post/add', data)

// 上传帖子图片
export const uploadPostImage = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/post/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 点赞帖子
export const likePost = (id) => http.post('/post/like', null, { params: { id } }) 

export const listComments = (postId, pageNum=1,pageSize=10)=> http.post('/post/commment/list', {pageNum,pageSize},{ params:{ postId } })
export const addComment = (data)=> http.post('/post/comment/add', data)
export const replyComment = (data)=> http.post('/post/comment/reply', data)
export const deleteComment = (id)=> http.post('/post/comment/delete', null, { params: { id } }) 

// 删除回复
export const deleteReply = (id)=> http.post('/post/comment/deleteReply', null, { params: { id } }) 
// 获取帖子一级评论分页
export const getCommentList = (postId, pageNum = 1, pageSize = 10) =>
  http.post('/post/comment/getCommentList', { pageNum, pageSize }, { params: { postId } })

// 获取某条评论回复分页
export const getReplyList = (commentId, pageNum = 1, pageSize = 10) =>
  http.post('/post/comment/getReplyList', { pageNum, pageSize }, { params: { commentId } }) 

// 点赞评论
export const likeComment = (id) => http.post('/post/comment/like', null, { params: { id } })

// 点赞回复
export const likeReply = (id) => http.post('/post/comment/likeReply', null, { params: { id } }) 

// 浏览帖子
export const viewPost = (id) => http.post('/post/view', null, { params: { id } }) 

// 用户消息通知
export const listUserNotify = (pageNum = 1, pageSize = 20) =>
  http.post('/post/notify/list', { pageNum, pageSize })

export const readUserNotify = () => http.post('/post/notify/read') 

// 推荐帖子
export const listRecommendPosts = () => http.get('/post/recommend') 