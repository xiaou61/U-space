-- ----------------------------
-- U-Space 2.4.2 数据库升级脚本
-- 活动状态管理优化
-- ----------------------------

-- 扩展活动状态字段的含义
-- 0: 待开始 - 当前时间 < 开始时间
-- 1: 进行中 - 开始时间 <= 当前时间 < 结束时间  
-- 2: 已结束 - 当前时间 >= 结束时间
-- 3: 已取消 - 管理员永久取消，不可恢复
-- 4: 已禁用 - 管理员暂时禁用，可重新启用

-- 更新活动状态字段注释
ALTER TABLE u_activity MODIFY COLUMN status TINYINT COMMENT '活动状态：0-待开始，1-进行中，2-已结束，3-已取消，4-已禁用';

-- 为状态更新查询添加索引
CREATE INDEX idx_activity_status_update ON u_activity(is_deleted, status, start_time, end_time);

-- 清理和更新现有数据：根据时间重新计算所有有效活动的状态
UPDATE u_activity 
SET status = CASE 
    WHEN status IN (3, 4) THEN status  -- 保持已取消和已禁用状态不变
    WHEN NOW() < start_time THEN 0     -- 待开始
    WHEN NOW() < end_time THEN 1       -- 进行中
    ELSE 2                             -- 已结束
END,
update_time = NOW()
WHERE is_deleted = 0 
  AND start_time IS NOT NULL 
  AND end_time IS NOT NULL;


-- 注意事项：
-- 1. 活动状态现在主要由系统根据时间自动计算
-- 2. 管理员可以启用(enable)、禁用(disable)、取消(cancel)活动
-- 3. 系统会定时自动更新活动状态
-- 4. 前端需要相应调整状态显示和操作逻辑 