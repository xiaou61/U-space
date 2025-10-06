import request from '@/utils/request'

export const lotteryAdminApi = {
  // ========== 奖品配置管理 ==========
  // 保存奖品配置
  savePrizeConfig(data) {
    return request.post('/admin/lottery/prize/save', data)
  },
  
  // 获取奖品配置列表
  getPrizeConfigList() {
    return request.get('/admin/lottery/prize/list')
  },
  
  // 启用/禁用奖品
  togglePrizeStatus(prizeId, isActive) {
    return request.post('/admin/lottery/prize/toggle-status', null, {
      params: { prizeId, isActive }
    })
  },
  
  // 暂停/恢复奖品
  suspendPrize(prizeId, suspendMinutes) {
    return request.post('/admin/lottery/prize/suspend', null, {
      params: { prizeId, suspendMinutes }
    })
  },
  
  // 手动调整概率
  adjustProbability(data) {
    return request.post('/admin/lottery/prize/adjust-probability', data)
  },
  
  // 归一化所有奖品概率
  normalizeAllProbabilities() {
    return request.post('/admin/lottery/prize/normalize')
  },
  
  // 验证概率总和
  validateProbabilitySum() {
    return request.get('/admin/lottery/prize/validate-probability')
  },
  
  // 批量调整概率
  batchAdjustProbability(data) {
    return request.post('/admin/lottery/prize/batch-adjust', data)
  },
  
  // 批量启用/禁用奖品
  batchToggleStatus(data) {
    return request.post('/admin/lottery/prize/batch-toggle', data)
  },
  
  // ========== 监控管理 ==========
  // 获取实时监控数据
  getRealtimeMonitor() {
    return request.get('/admin/lottery/monitor/realtime')
  },
  
  // 获取单个奖品监控数据
  getPrizeMonitor(prizeId) {
    return request.get(`/admin/lottery/monitor/prize/${prizeId}`)
  },
  
  // 获取预警信息列表
  getAlerts() {
    return request.get('/admin/lottery/monitor/alerts')
  },
  
  // ========== 记录和统计 ==========
  // 获取所有抽奖记录
  getAllDrawRecords(data) {
    return request.post('/admin/lottery/records', data)
  },
  
  // 获取历史统计数据
  getHistoryStatistics(data) {
    return request.post('/admin/lottery/statistics/history', data)
  },
  
  // 获取概率调整历史
  getAdjustHistory(prizeId, page, size) {
    return request.get('/admin/lottery/adjust-history', {
      params: { prizeId, page, size }
    })
  },
  
  // 获取综合分析数据
  getComprehensiveAnalysis(startDate, endDate) {
    return request.get('/admin/lottery/analysis/comprehensive', {
      params: { startDate, endDate }
    })
  },
  
  // ========== 用户管理 ==========
  // 重置用户抽奖限制
  resetUserLimit(userId) {
    return request.post('/admin/lottery/user/reset-limit', null, {
      params: { userId }
    })
  },
  
  // 设置用户黑名单
  setUserBlacklist(userId, isBlacklist) {
    return request.post('/admin/lottery/user/blacklist', null, {
      params: { userId, isBlacklist }
    })
  },
  
  // 获取风险用户列表
  getRiskUserList(data) {
    return request.post('/admin/lottery/user/risk-list', data)
  },
  
  // 评估用户风险等级
  evaluateRiskLevel(userId) {
    return request.post('/admin/lottery/user/evaluate-risk', null, {
      params: { userId }
    })
  },
  
  // 检测异常行为
  detectAbnormalBehavior(userId) {
    return request.post('/admin/lottery/user/detect-abnormal', null, {
      params: { userId }
    })
  },
  
  // ========== 缓存管理 ==========
  // 刷新缓存
  refreshCache() {
    return request.post('/admin/lottery/cache/refresh')
  },
  
  // ========== 应急控制 ==========
  // 手动熔断
  manualCircuitBreak(reason) {
    return request.post('/admin/lottery/emergency/circuit-break', null, {
      params: { reason }
    })
  },
  
  // 恢复服务
  resumeService() {
    return request.post('/admin/lottery/emergency/resume')
  },
  
  // 启用降级模式
  enableDegradation() {
    return request.post('/admin/lottery/emergency/degradation/enable')
  },
  
  // 禁用降级模式
  disableDegradation() {
    return request.post('/admin/lottery/emergency/degradation/disable')
  }
}

