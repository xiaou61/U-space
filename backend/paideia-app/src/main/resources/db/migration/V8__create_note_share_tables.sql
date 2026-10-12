-- 只读分享（WORK-006 TASK-011，范围由原 WORK-007 并入）。
--
-- 安全模型与 GET /api/v1/knowledge/files/{uuid} 同构：**id 不可枚举即凭据**。
-- 所以主键是 UUID 而不是自增：自增 id 可被顺序猜出来，那等于把所有人的分享都摊开。
--
-- 失效是**读取时判定**（revoked_at IS NULL AND expires_at > now），没有定时清理任务——
-- 线上是单实例、无调度器基设（见 design 的约束）。过期的行留着不碍事，注销时随用户一起删。
--
-- 与 V4..V7 一致：不建跨模块外键。

CREATE TABLE note_shares (
  -- UUID 就是凭据本身：不可枚举，也不含任何用户信息。
  share_id   CHAR(36)        NOT NULL,
  note_id    BIGINT UNSIGNED NOT NULL,
  -- 分享者。列表与撤回都按它过滤；但**它不会出现在免鉴权端点的产物里**。
  user_id    BIGINT UNSIGNED NOT NULL,
  expires_at DATETIME(3)     NOT NULL,
  -- 撤回与"管理员下架"写的是同一个字段：两者的效果都是"这条链接不再可读"。
  revoked_at DATETIME(3)     NULL,
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (share_id),
  KEY idx_note_share_owner (user_id, created_at),
  KEY idx_note_share_note (note_id)
) ENGINE = InnoDB;
