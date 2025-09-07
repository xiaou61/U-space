import request from '@/utils/request'

export const monitorApi = {
  // ============ SQL调用树监控 ============
  
  // 获取当前线程的SQL调用树
  getCurrentTree() {
    return request.get('/sql-tree/current')
  },
  
  // 获取所有会话的SQL调用树
  getAllSessions() {
    return request.get('/sql-tree/sessions')
  },
  
  // 获取指定会话的SQL调用树
  getSessionTree(sessionKey) {
    return request.get(`/sql-tree/session/${sessionKey}`)
  },
  
  // ============ 分析功能 ============
  
  // 分析慢SQL
  getSlowSqlAnalysis() {
    return request.get('/sql-tree/analysis/slow-sql')
  },
  
  // 分析错误SQL
  getErrorSqlAnalysis() {
    return request.get('/sql-tree/analysis/error-sql')
  },
  
  // ============ 统计信息 ============
  
  // 获取统计信息
  getStatistics() {
    return request.get('/sql-tree/statistics')
  },
  
  // 系统健康检查
  healthCheck() {
    return request.get('/sql-tree/health')
  },
  
  // ============ 配置管理 ============
  
  // 获取配置
  getConfig() {
    return request.get('/sql-tree/config')
  },
  
  // 更新配置
  updateConfig(config) {
    return request.post('/sql-tree/config', config)
  },
  
  // 获取系统信息
  getSystemInfo() {
    return request.get('/sql-tree/system-info')
  },
  
  // ============ 数据管理 ============
  
  // 清理所有调用树数据
  clearAllTrees() {
    return request.post('/sql-tree/clear')
  },
  
  // 清理过期会话
  cleanupExpiredSessions(params = {}) {
    return request.post('/sql-tree/cleanup', params)
  },
  
  // 强制完成当前调用树
  forceCompleteCurrentTree() {
    return request.post('/sql-tree/force-complete')
  },
  
  // 重置统计信息
  resetStatistics() {
    return request.post('/sql-tree/reset-statistics')
  },
  
  // 导出数据
  exportData() {
    return request.get('/sql-tree/export')
  }
} 