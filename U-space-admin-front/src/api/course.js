import request from '../utils/request'

// 创建课程
export const createCourse = (data) => request.post('/admin/subject/create', data)
// 更新课程
export const updateCourse = (id, data) => request.post('/admin/subject/update', data, { params: { id } })
// 删除课程
export const deleteCourse = (id) => request.post('/admin/subject/delete', null, { params: { id } })
// 分页查询课程
export const pageCourse = (data) => request.post('/admin/subject/list', data)
// 添加课程与班级关联
export const addClassCourse = (courseId, classId) => request.post('/admin/subject/add-class-course', null, { params: { courseId, classId } }) 