import request from '@/utils/request'

export const resumeApi = {
  // 模板
  getTemplates(params) {
    return request.get('/resume/templates', { params })
  },
  getTemplateDetail(id) {
    return request.get(`/resume/templates/${id}`)
  },

  // 简历
  getMyResumes(params) {
    return request.get('/resume', { params })
  },
  createResume(data) {
    return request.post('/resume', data)
  },
  updateResume(id, data) {
    return request.put(`/resume/${id}`, data)
  },
  deleteResume(id) {
    return request.delete(`/resume/${id}`)
  },
  previewResume(id) {
    return request.get(`/resume/${id}/preview`)
  },
  exportResume(id, data) {
    return request.post(`/resume/${id}/export`, data)
  },
  createShareLink(id) {
    return request.post(`/resume/${id}/share`)
  },
  getAnalytics(id) {
    return request.get(`/resume/${id}/analytics`)
  }
}
