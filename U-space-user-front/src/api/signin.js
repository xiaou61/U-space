import http from '../utils/request'

// 查看需要签到的列表
export const listSignins = (groupId) => http.post('/student/signin/list', null, { params: { groupId } })

// 提交签到记录
export const addSignin = (data) => http.post('/student/signin/add', data) 