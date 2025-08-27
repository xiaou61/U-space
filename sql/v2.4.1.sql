-- ----------------------------
-- U-Space 2.4.1 数据库升级脚本
-- 添加活动积分发放状态字段
-- ----------------------------

-- 为活动表添加积分发放状态字段
ALTER TABLE u_activity ADD COLUMN points_granted TINYINT DEFAULT 0 COMMENT '积分是否已发放：0-未发放，1-已发放';

-- 为现有的已结束活动设置默认值
UPDATE u_activity 
SET points_granted = 0 
WHERE points_granted IS NULL;

-- 为已经结束很久的活动（超过7天）设置为已发放状态，避免重复处理历史数据
UPDATE u_activity 
SET points_granted = 1 
WHERE status = 2 
  AND end_time < DATE_SUB(NOW(), INTERVAL 7 DAY)
  AND points_granted = 0;

-- 添加索引以提高查询性能
CREATE INDEX idx_activity_points_status ON u_activity(status, points_granted, end_time);

-- 添加数据库升级记录
INSERT INTO u_system_version (version, description, update_time) 
VALUES ('2.4.1', '添加活动积分自动发放功能：增加points_granted字段和相关索引', NOW())
ON DUPLICATE KEY UPDATE 
description = '添加活动积分自动发放功能：增加points_granted字段和相关索引',
update_time = NOW(); 