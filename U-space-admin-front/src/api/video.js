import request from '../utils/request'

// 添加入学必看视频
export const addVideo = (data) => request.post('/admin/video/add', data)

// 删除入学必看视频
export const deleteVideo = (id) => request.post('/admin/video/delete', null, { params: { id } })

// 获取入学必看视频列表
export const listVideos = () => request.post('/admin/video/list') 