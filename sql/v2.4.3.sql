-- ----------------------------
-- U-Space 2.4.3 数据库升级脚本
-- 实时状态判断优化
-- ----------------------------

-- 优化活动查询索引，提升基于时间的查询性能
CREATE INDEX idx_activity_time_query ON u_activity(is_deleted, status, start_time, end_time, current_participants, max_participants);


-- 系统架构优化说明：
-- 1. 核心业务逻辑（参与活动、查询可参与活动）现在直接基于时间判断，实现零延迟
-- 2. 数据库status字段仍然保留，主要用于管理员操作状态（取消、禁用）和数据展示
-- 3. 添加了Redis缓存层以提升高频查询性能
-- 4. 定时任务频率降低至30分钟，主要用于数据展示一致性
-- 5. 查询索引优化以支持高效的时间范围查询 