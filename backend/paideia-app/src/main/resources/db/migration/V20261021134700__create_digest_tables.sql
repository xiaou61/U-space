-- 每日技术速递（WORK-027）的两张表。
--
-- **不建第三张**：日报按 `edition_date` 从 digest_items 直接取，成刊状态由「当天有没有行」推出。
--
-- **fingerprint 是全局唯一**（与 WORK-025 的「同分类内判重」不同）：新闻的重复是**跨源**的
-- （同一条被两个门户转），不是分类内的。全局唯一直接挡住；并发两个实例同时跑也由它兜底。
--
-- **published_at 用 BIGINT 存 epoch 毫秒**而不是 DATETIME：源给的就是 epoch 秒/毫秒，
-- 转来转去容易错时区。存原始值，展示时由前端按用户本地时区格式化。
--
-- **时间列由应用写入**（UTC），不用 DEFAULT CURRENT_TIMESTAMP——项目纪律。
--
-- 只建结构、不插业务数据（MigrationSeedOverlapTest 守着）。

CREATE TABLE digest_items (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 归属哪天：按 UTC 日切，与 edition 查询的索引一致
  edition_date  DATE            NOT NULL,
  -- news | github | community | hackernews；决定前端分组
  group_key     VARCHAR(16)     NOT NULL,
  -- 源标识，如 qbitai / ithome / infoq / juejin / oschina / github-trending / hackernews
  source_key    VARCHAR(32)     NOT NULL,
  source_name   VARCHAR(64)     NOT NULL,
  title         VARCHAR(512)    NOT NULL,
  -- 我们自己写的一句话摘要；AI 失败时为 NULL（降级态）
  summary       VARCHAR(1024)   NULL,
  url           VARCHAR(1024)   NOT NULL,
  -- 规范化 URL 的 SHA-256，去重靠它
  fingerprint   CHAR(64)        NOT NULL,
  -- 源自身的信号：HN 点数、GitHub 当日新增 star；其它源为 NULL
  heat          INT             NULL,
  -- 一条副信息：语言、作者、评论数等，直接给人看
  meta          VARCHAR(255)    NULL,
  -- 原文发布时间（UTC 毫秒）；源不提供时为抓取时间
  published_at  BIGINT          NULL,
  -- ready | degraded（AI 没写成摘要）
  summary_state VARCHAR(16)     NOT NULL DEFAULT 'ready',
  created_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_digest_items_fingerprint (fingerprint),
  KEY idx_digest_items_edition (edition_date, group_key, heat)
) ENGINE = InnoDB;

CREATE TABLE digest_weeklies (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 该周周一的 UTC 日期
  week_start   DATE         NOT NULL,
  body         MEDIUMTEXT   NOT NULL,
  -- 生成时用了多少条日报素材，便于回看质量
  item_count   INT          NOT NULL,
  created_at   DATETIME(3)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_digest_weeklies_week (week_start)
) ENGINE = InnoDB;
