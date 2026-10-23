-- 第三方登录（WORK-037）。
--
-- 两件事：
-- 1. 放宽 users 的两列——第三方注册的用户可能没有邮箱（GitHub 邮箱可能未公开），
--    也可能没有密码（用户从没设过）。**username 保持 NOT NULL**：它仍是站内唯一标识
--    与界面显示名，第三方建号时由服务端生成。放开它只会让"没有名字的用户"出现在每一处展示里。
--    email 的唯一索引保留：MySQL 允许多个 NULL，因此多个无邮箱账号可以共存，正是需要的语义。
-- 2. 建 user_identities：一个账号可以绑多种登录方式。
--
-- 回滚注意：改回 NOT NULL 前必须先确认没有 email/password_hash 为空的行，否则 ALTER 直接失败。
-- 这一点无法在 SQL 里自动保证，所以把它写在这里而不是留给人去踩。
--
-- 时间列走列默认值（审计时间），与 V1 的账号表一致。

ALTER TABLE users
  MODIFY COLUMN email         VARCHAR(254) NULL,
  MODIFY COLUMN password_hash VARCHAR(100) NULL;

CREATE TABLE user_identities (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED NOT NULL,
  -- 平台标识（github / gitee）。新增平台只加取值，不改表。
  provider         VARCHAR(32)     NOT NULL,
  -- 平台内的稳定用户 id，**不是**昵称也不是邮箱：后两者都会变，拿它们做唯一键等于把账号
  -- 绑在一个会漂移的值上。
  provider_user_id VARCHAR(128)    NOT NULL,
  -- 首次授权时的昵称，只作账号设置页的展示，不参与任何判定、登录后不再回写。
  display_name     VARCHAR(128)    NULL,
  created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_identity_provider_subject (provider, provider_user_id),
  KEY idx_identity_user (user_id),
  CONSTRAINT fk_identity_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
