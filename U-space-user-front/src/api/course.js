import http from '../utils/request'

// 分页查看所有课程
export const getCourseList = (data) => {
  return http.post('/subject/list', data)
}

// 抢课接口
export const grabCourse = (data) => {
  return http.post('/subject/grab', data)
}

// 退课接口
export const dropCourse = (courseId) => {
  return http.post('/subject/drop', null, { params: { courseId } })
}

// 查看我的选课列表
export const getMySelectedCourses = () => {
  return http.get('/subject/my-courses')
} 