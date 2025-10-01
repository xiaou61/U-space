import request from '@/utils/request'

export const pointsApi = {
  // 管理员发放积分
  grantPoints(data) {
    return request.post('/admin/points/grant', data)
  },
  
  // 批量发放积分
  batchGrantPoints(data) {
    return request.post('/admin/points/batch-grant', data)
  },
  
  // 获取所有用户积分明细列表
  getAllPointsDetailList(data) {
    return request.post('/admin/points/detail-list', data)
  },
  
  // 获取用户积分列表（用于积分排行和管理）
  getUserPointsList(data) {
    return request.post('/admin/points/user-list', data)
  },
  
  // 获取积分统计数据
  getPointsStatistics() {
    return request.get('/admin/points/statistics')
  },
  
  // 根据用户ID获取用户积分信息
  getUserPointsInfo(userId) {
    return request.get(`/admin/points/user-info/${userId}`)
  }
}
