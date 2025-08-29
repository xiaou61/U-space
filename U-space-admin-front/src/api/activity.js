import request from '../utils/request'

// ===== 活动管理接口 =====

/**
 * 创建活动
 * @param {Object} data - 活动创建数据
 * @param {string} data.title - 活动标题
 * @param {string} data.description - 活动描述  
 * @param {string} data.coverImage - 活动封面图片URL
 * @param {number} data.maxParticipants - 最大参与人数
 * @param {number} data.pointsTypeId - 积分类型ID
 * @param {number} data.pointsAmount - 奖励积分数量
 * @param {number} data.activityType - 活动类型(1:抢夺型 2:签到型 3:任务型)
 * @param {string} data.startTime - 活动开始时间 (yyyy-MM-dd HH:mm:ss)
 * @param {string} data.endTime - 活动结束时间 (yyyy-MM-dd HH:mm:ss)
 * @param {string} data.rules - 活动规则说明
 */
export const createActivity = (data) => request.post('/admin/activity', data)

/**
 * 更新活动
 * @param {number} id - 活动ID
 * @param {Object} data - 活动更新数据
 */
export const updateActivity = (id, data) => request.put(`/admin/activity/${id}`, data)

/**
 * 删除活动
 * @param {number} id - 活动ID
 */
export const deleteActivity = (id) => request.delete(`/admin/activity/${id}`)

/**
 * 获取活动详情
 * @param {number} id - 活动ID
 */
export const getActivity = (id) => request.get(`/admin/activity/${id}`)

/**
 * 分页查询活动列表
 * @param {Object} params - 分页参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 页大小
 */
export const getActivityPage = (params) => request.post('/admin/activity/page', params)

/**
 * 启用活动（重新计算状态）
 * @param {number} id - 活动ID
 */
export const enableActivity = (id) => request.put(`/admin/activity/${id}/enable`)

/**
 * 禁用活动（暂停活动）
 * @param {number} id - 活动ID
 */
export const disableActivity = (id) => request.put(`/admin/activity/${id}/disable`)

/**
 * 取消活动（永久取消）
 * @param {number} id - 活动ID
 */
export const cancelActivity = (id) => request.put(`/admin/activity/${id}/cancel`)

/**
 * 获取活动参与者列表
 * @param {number} id - 活动ID
 * @param {Object} params - 分页参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 页大小
 */
export const getActivityParticipants = (id, params) => request.post(`/admin/activity/${id}/participants`, params)

/**
 * 活动结束后自动发放积分
 * @param {number} id - 活动ID
 */
export const autoGrantPoints = (id) => request.post(`/admin/activity/${id}/auto-grant`)

/**
 * 批量处理已结束活动的积分发放（手动触发定时任务）
 */
export const batchGrantPoints = () => request.post('/admin/activity/batch-grant-points')

/**
 * 批量更新活动状态（手动触发定时任务）
 */
export const batchUpdateStatus = () => request.post('/admin/activity/batch-update-status')

// ===== 积分类型接口 =====

/**
 * 获取积分类型列表（用于活动创建时选择）
 */
export const getPointsTypeList = () => request.post('/admin/points/types/list') 