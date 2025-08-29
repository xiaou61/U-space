import request from '../utils/request'

// ===== 积分管理接口 =====

/**
 * 批量发放积分
 * @param {Object} data - 积分发放数据
 * @param {Array} data.userIds - 用户ID列表
 * @param {number} data.pointsTypeId - 积分类型ID
 * @param {number} data.pointsAmount - 积分数量
 * @param {string} data.remark - 备注信息
 */
export const grantPointsManually = (data) => request.post('/admin/points/grant', data)

/**
 * 撤销积分发放
 * @param {number} recordId - 积分记录ID
 */
export const revokePointsRecord = (recordId) => request.delete(`/admin/points/revoke/${recordId}`)

/**
 * 为活动参与者批量发放积分
 * @param {number} activityId - 活动ID
 */
export const batchGrantPointsForActivity = (activityId) => request.post(`/admin/points/activity/${activityId}/batch-grant`)

// ===== 积分类型管理接口 =====

/**
 * 获取积分类型列表
 */
export const getPointsTypes = () => request.post('/admin/points/types/list')

/**
 * 创建积分类型
 * @param {Object} data - 积分类型数据
 * @param {string} data.typeName - 积分类型名称
 * @param {string} data.typeCode - 积分类型编码
 * @param {string} data.description - 积分类型描述
 * @param {string} data.iconUrl - 积分图标URL
 * @param {number} data.sortOrder - 排序顺序
 */
export const createPointsType = (data) => request.post('/admin/points/types', data)

/**
 * 更新积分类型
 * @param {number} id - 积分类型ID
 * @param {Object} data - 积分类型更新数据
 */
export const updatePointsType = (id, data) => request.put(`/admin/points/types/${id}`, data)

/**
 * 删除积分类型
 * @param {number} id - 积分类型ID
 */
export const deletePointsType = (id) => request.delete(`/admin/points/types/${id}`)

/**
 * 启用/禁用积分类型
 * @param {number} id - 积分类型ID
 * @param {number} isActive - 是否启用 (0:禁用 1:启用)
 */
export const togglePointsTypeStatus = (id, isActive) => request.put(`/admin/points/types/${id}/status`, { isActive })

// ===== 积分记录查询接口 =====

/**
 * 分页查询积分记录
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 页大小
 * @param {string} params.userId - 用户ID（可选）
 * @param {number} params.pointsTypeId - 积分类型ID（可选）
 * @param {number} params.activityId - 活动ID（可选）
 * @param {number} params.operationType - 操作类型（可选，1:获得 2:扣除）
 * @param {number} params.status - 发放状态（可选，0:待发放 1:已发放 2:发放失败 3:已撤销）
 */
export const getPointsRecords = (params) => request.post('/admin/points/records', params)

/**
 * 获取用户积分余额
 * @param {string} userId - 用户ID
 */
export const getUserPointsBalance = (userId) => request.get(`/admin/points/balance/${userId}`) 