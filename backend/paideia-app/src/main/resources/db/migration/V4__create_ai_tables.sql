-- AI 精简：用户自带凭据（BYOK）与精简结果缓存。
--
-- 两张表都不建跨模块外键：entry_id / owner_user_id 指向别的模块的表，加 FK 会把模块间的
-- 数据耦合写进数据库。代价是删条目/删用户会留下不可达的行（与既有的孤儿附件同类），
-- 列为已知缺口。

CREATE TABLE ai_model_credentials (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL,
  base_url          VARCHAR(512)    NOT NULL,
  model             VARCHAR(128)    NOT NULL,
  -- 密文布局：iv(12) || ciphertext || tag(16)。不存明文，也不存哈希（哈希无法解密、对调用无用）。
  api_key_cipher    VARBINARY(1024) NOT NULL,
  -- 只为界面回显"已配置 ····ABCD"；有它才能让用户确认自己配的是哪一把 Key。
  api_key_last4     CHAR(4)         NOT NULL,
  created_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at        DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_credential_user (user_id)
) ENGINE = InnoDB;

CREATE TABLE ai_entry_summaries (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  entry_id       BIGINT UNSIGNED NOT NULL,
  -- 正文的 SHA-256（十六进制）。正文一改哈希即变，旧结果自然不再命中；比依赖
  -- "任何 UPDATE 都会刷 updated_at" 这个隐式前提更直接。
  content_hash   CHAR(64)        NOT NULL,
  model          VARCHAR(128)    NOT NULL,
  prompt_version VARCHAR(16)     NOT NULL,
  owner_user_id  BIGINT UNSIGNED NOT NULL,
  -- public | private：private 只有 owner 能读；这条由查询里的 WHERE 保证，前端偏好不参与。
  visibility     VARCHAR(16)     NOT NULL,
  -- 已通过字段校验的结构化卡片（{headline, sections:[{heading, body}]}）。
  -- 校验失败与空结果一律不落库，所以这里不会有"半成品"。
  card_json      JSON            NOT NULL,
  created_at     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_summary_identity (entry_id, content_hash, model, prompt_version, owner_user_id),
  KEY idx_ai_summary_read (entry_id, content_hash, model, prompt_version, visibility)
) ENGINE = InnoDB;
