import http from '../utils/request'

// 查看群组作业列表
export const listHomeworks = (groupId) => http.post('/student/homework/list', null, { params: { groupId } })

// 提交作业
export const submitHomework = (id, data) => http.post('/student/homework/homework', data, { params: { id } })

// 查看作业详情
export const getHomeworkDetail = (id) => http.post('/student/homework/homework-detail', null, { params: { id } })

// 上传附件
export const uploadAttachment = (file) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/student/homework/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 更新已提交作业
export const updateHomework = (id, data) => http.post('/student/homework/update', data, { params: { id } })

// 查看作业评分
export const getHomeworkGrade = (id) => http.post('/student/homework/homeworkgrade', null, { params: { id } }) 