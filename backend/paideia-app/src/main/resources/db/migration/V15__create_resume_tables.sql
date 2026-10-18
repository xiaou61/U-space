-- 简历识别与优化（WORK-018）。
--
-- 四张表，各自的"为什么不是另一种做法"：
--
-- ① 主简历与岗位版本**分开**：一岗一版是产品形态，主简历是用户在编辑器里持续改的那一份；
--    合在一起会让"改一次简历"把所有历史版本一起改掉。
-- ② 编辑快照单独一张：回滚需要历史。塞进主表只能变成一条不断变长的 JSON，
--    而那既没法按时间裁剪，也没法只取某一版。
-- ③ 提案单独一张、一行一条：**采纳状态必须是数据**（刷新、换设备都要保持），
--    并且"未采纳的也保留"意味着它不能随定稿一起消失。
-- ④ `plain_text` 存的是**抽取出的原文**，它是事实闸门与关键词三桶的比对基准。
--    只存结构化 JSON 的话，闸门就失去了"这句话在原件里到底有没有"这个唯一的凭据。
--
-- 与 V1/V4/V6 一致：**不建跨模块外键**，注销级联由应用层表清单负责（见 AiUserDeletionListener）。
-- `updated_at` 由应用按 UTC 写，不用库默认值：库会话时区是 +08:00，默认值会让同一列出现两种时区。

CREATE TABLE resume_documents (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  title       VARCHAR(120)    NOT NULL,
  -- 同一用户有且只有一份默认简历；"设默认"由应用层先清后设，不靠数据库约束
  is_default  TINYINT(1)      NOT NULL DEFAULT 0,
  -- 乐观锁：客户端保存时带回来，命中 0 行就是"别人改过了"，不静默覆盖
  revision    INT             NOT NULL DEFAULT 1,
  -- 结构化简历（本项唯一真源）。字段集合与校验见 ResumeJson
  data_json   JSON            NOT NULL,
  -- 抽取出的原文。事实闸门与三桶分类都以它为基准
  plain_text  MEDIUMTEXT      NOT NULL,
  created_at  DATETIME(3)     NOT NULL,
  updated_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_resume_doc_user (user_id, updated_at)
) ENGINE = InnoDB;

CREATE TABLE resume_edit_history (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  document_id BIGINT UNSIGNED NOT NULL,
  -- 冗余 user_id：让归属校验一条 SQL 说完，不必 join 主表
  user_id     BIGINT UNSIGNED NOT NULL,
  data_json   JSON            NOT NULL,
  created_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_resume_history_doc (document_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE resume_versions (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  document_id   BIGINT UNSIGNED NOT NULL,
  user_id       BIGINT UNSIGNED NOT NULL,
  company       VARCHAR(120)    NULL,
  role          VARCHAR(120)    NULL,
  -- JD 原文整段留存：报告与版本回看都要能对回"当时那份 JD"
  jd_text       MEDIUMTEXT      NOT NULL,
  -- draft：还在挑提案；finalized：已过闸门、可打印
  status        VARCHAR(16)     NOT NULL,
  -- 三桶结果与缺口清单（代码算出来的，不重跑模型也能回看）
  analysis_json JSON            NULL,
  final_json    JSON            NULL,
  created_at    DATETIME(3)     NOT NULL,
  updated_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_resume_version_doc (document_id, created_at),
  KEY idx_resume_version_user (user_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE resume_proposals (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  version_id      BIGINT UNSIGNED NOT NULL,
  user_id         BIGINT UNSIGNED NOT NULL,
  -- 界面顺序。不靠 id 排序：重新生成会先删后插，id 会跳
  seq             INT             NOT NULL,
  -- 指向主简历里的位置，如 work[0].bullets[2]；闸门靠它回查原文
  entry_ref       VARCHAR(64)     NOT NULL,
  field           VARCHAR(32)     NOT NULL,
  original_text   TEXT            NOT NULL,
  proposed_text   TEXT            NOT NULL,
  -- rewrite | reorder | add | remove
  change_type     VARCHAR(16)     NOT NULL,
  rationale       TEXT            NOT NULL,
  covers_keywords JSON            NULL,
  -- 默认 0：用户主动勾选才进定稿（防止"一键全采纳"变成"一键全信模型"）
  accepted        TINYINT(1)      NOT NULL DEFAULT 0,
  -- pending | passed | failed；编辑改后文本会重置为 pending 并重跑
  gate_status     VARCHAR(16)     NOT NULL DEFAULT 'pending',
  gate_violations JSON            NULL,
  created_at      DATETIME(3)     NOT NULL,
  updated_at      DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_resume_proposal_version (version_id, seq)
) ENGINE = InnoDB;
