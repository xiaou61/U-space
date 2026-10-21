-- AI 训练营（WORK-029）：五张表。
--
-- 形态是「简历 → 出卷 → 作答 → 薄弱点 → 计划 → 复测」，所以按一次营的生命周期拆：
--   camp_programs  —— 营本身（天数、题数、两个交卷时刻、当前计划）
--   camp_items     —— 题目（诊断卷与复测卷同表，`form` 区分）
--   camp_attempts  —— 每题一次选择（判定由服务端写，客户端传不了 correct）
--   camp_plans     —— 一次计划生成（保留历史：旧计划的勾选状态是那一次的事实）
--   camp_tasks     —— 计划项（day_no + seq 定位；done_at 单独一行表示完成）
--
-- 三条刻意的取舍：
-- 1. **不建到 knowledge 表的外键**（与 V1/V4/V15 一致）：`camp_items.concept_id` 与
--    `camp_tasks.entry_id` 指向另一个模块。代价是删条目后计划里可能有悬空 id，
--    由「读计划时 join 已发布条目，join 不到降级为缺口项」兜住。
-- 2. **`user_id` 冗余到每张表**：归属校验一条 SQL 说完，不必 join 主表；漏写条件时
--    也只是查不到而不是查到别人的——但真正的保证仍是每条 SQL 都带 `user_id`。
-- 3. **时间列由应用传 UTC**，不用 `DEFAULT CURRENT_TIMESTAMP`：库会话时区是 +08:00，
--    默认值会让同一列出现两种时区（本项目已踩过）。
--
-- 注销级联由应用层表清单负责（`AiUserDeletionListener`），顺序：任务 → 计划 → 作答 → 题目 → 营。

CREATE TABLE camp_programs (
  id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id                 BIGINT UNSIGNED NOT NULL,
  resume_document_id      BIGINT UNSIGNED NOT NULL,
  -- 开营时的标题快照：简历后来改标题不影响已开的营
  resume_title            VARCHAR(200)    NOT NULL,
  days                    TINYINT         NOT NULL,
  item_count              INT             NOT NULL,
  dropped_count           INT             NOT NULL DEFAULT 0,
  diagnostic_submitted_at DATETIME(3)     NULL,
  retest_submitted_at     DATETIME(3)     NULL,
  active_plan_id          BIGINT UNSIGNED NULL,
  created_at              DATETIME(3)     NOT NULL,
  updated_at              DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_camp_user (user_id, created_at)
) ENGINE = InnoDB;

CREATE TABLE camp_items (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  program_id    BIGINT UNSIGNED NOT NULL,
  user_id       BIGINT UNSIGNED NOT NULL,
  -- diagnostic | retest
  form          VARCHAR(16)     NOT NULL,
  seq           INT             NOT NULL,
  topic         VARCHAR(120)    NOT NULL,
  concept_id    BIGINT UNSIGNED NULL,
  -- easy | medium | hard
  difficulty    VARCHAR(8)      NOT NULL,
  stem          VARCHAR(500)    NOT NULL,
  options_json  JSON            NOT NULL,
  answer_index  TINYINT         NOT NULL,
  explanation   VARCHAR(1200)   NOT NULL,
  fingerprint   CHAR(64)        NOT NULL,
  created_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  -- 同一卷的序号唯一：重出卷是新建一个营，不会在同卷里撞号
  UNIQUE KEY uk_camp_item_seq (program_id, form, seq),
  -- 同卷题干不重复。指纹由服务端算，客户端的题干进不了这一列
  UNIQUE KEY uk_camp_item_fp (program_id, form, fingerprint),
  KEY idx_camp_item_user (user_id),
  KEY idx_camp_item_program (program_id, form)
) ENGINE = InnoDB;

CREATE TABLE camp_attempts (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  program_id  BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  item_id     BIGINT UNSIGNED NOT NULL,
  choice      TINYINT         NOT NULL,
  -- 服务端比对结果。**请求体里没有这个字段**，伪造不进任何分支
  correct     TINYINT         NOT NULL,
  created_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  -- 一题只能交一次：重复提交流程在服务层拦 409，这条是最后的兜底
  UNIQUE KEY uk_camp_attempt (program_id, item_id),
  KEY idx_camp_attempt_user (user_id),
  KEY idx_camp_attempt_item (item_id)
) ENGINE = InnoDB;

CREATE TABLE camp_plans (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  program_id        BIGINT UNSIGNED NOT NULL,
  user_id           BIGINT UNSIGNED NOT NULL,
  days              TINYINT         NOT NULL,
  budget_minutes    INT             NOT NULL,
  -- 生成这份计划时的弱项快照与排不下的项：回答「当时为什么这样排」
  weak_json         JSON            NOT NULL,
  unscheduled_json  JSON            NOT NULL,
  created_at        DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_camp_plan_program (program_id, created_at),
  KEY idx_camp_plan_user (user_id)
) ENGINE = InnoDB;

CREATE TABLE camp_tasks (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  plan_id     BIGINT UNSIGNED NOT NULL,
  program_id  BIGINT UNSIGNED NOT NULL,
  user_id     BIGINT UNSIGNED NOT NULL,
  day_no      TINYINT         NOT NULL,
  seq         INT             NOT NULL,
  -- read | do | gap | review
  kind        VARCHAR(8)      NOT NULL,
  entry_id    BIGINT UNSIGNED NULL,
  concept_id  BIGINT UNSIGNED NULL,
  topic       VARCHAR(120)    NOT NULL,
  title       VARCHAR(200)    NOT NULL,
  minutes     INT             NOT NULL,
  why         VARCHAR(400)    NOT NULL,
  done_at     DATETIME(3)     NULL,
  created_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_camp_task_seq (plan_id, day_no, seq),
  KEY idx_camp_task_user (user_id),
  KEY idx_camp_task_plan (plan_id, day_no, seq)
) ENGINE = InnoDB;
