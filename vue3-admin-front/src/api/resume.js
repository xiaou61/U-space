import request from '@/utils/request'

export const resumeApi = {
  getTemplates(params) {
    return request.get('/admin/resume/templates', { params })
  },
  getTemplateDetail(id) {
    return request.get(`/admin/resume/templates/${id}`)
  },
  createTemplate(data) {
    return request.post('/admin/resume/templates', data)
  },
  updateTemplate(id, data) {
    return request.put(`/admin/resume/templates/${id}`, data)
  },
  deleteTemplate(id) {
    return request.delete(`/admin/resume/templates/${id}`)
  },
  getAnalytics() {
    return request.get('/admin/resume/analytics')
  },
  getHealthReports() {
    return request.get('/admin/resume/reports')
  }
}
