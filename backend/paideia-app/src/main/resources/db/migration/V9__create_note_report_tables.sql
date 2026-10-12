-- 分享的举报与下架（WORK-006 TASK-013，范围由原 WORK-007 并入）。
--
-- 「举报 → 管理员下架」是免鉴权分享端点的**唯一治理手段**：链接谁都能看，所以必须有一条
-- 让内容能被撤下来的路。下架写的是 note_shares.revoked_at（与用户自己撤回同一个字段）——
-- 两者的效果都是"这条链接不再可读"。
--
-- 与 V4..V8 一致：不建跨模块外键。

CREATE TABLE note_reports (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  share_id         CHAR(36)        NOT NULL,
  reporter_user_id BIGINT UNSIGNED NOT NULL,
  reason           VARCHAR(500)    NOT NULL,
  -- pending | taken_down。留成字符串而不是布尔：将来可能要多一个"驳回"，
  -- 那时加一个取值即可，不必迁移列类型。
  status           VARCHAR(16)     NOT NULL DEFAULT 'pending',
  created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  handled_at       DATETIME(3)     NULL,
  PRIMARY KEY (id),
  -- 管理端按"待处理优先、早的在前"取
  KEY idx_note_report_status (status, created_at),
  -- 同一个人对同一条分享只留一条：重复提交是"再用一次"，不该堆成一屏重复记录
  UNIQUE KEY uk_note_report_once (share_id, reporter_user_id)
) ENGINE = InnoDB;
