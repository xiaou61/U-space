-- SQL 数据集的三源治理（WORK-035 T-07）。
--
-- 三种来源：builtin（classpath，受信任，不进这张表）、private（仅本人）、community（所有人）。
-- **内置数据集刻意不在这张表里**：它们永远在 classpath，不依赖对象存储；塞进表会让
-- "对象存储挂了"连累到内置集，而内置集是课程能继续做题的底线。
--
-- 内容不进数据库：只存 `storage_id`（FileStorage 的标识），字节在对象存储或本地磁盘。
-- 把 SQL 文件塞成 blob 会让备份与迁移变重，而它本来就是一个文件。

CREATE TABLE sql_datasets (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 对外标识。**不用自增 id 当对外 key**：`/sandbox/run` 的 dataset 参数用它，
  -- 自增数可枚举——扫一遍就能发现别人的私人数据集 key。UUID 与 FileStorage 的 id 同形。
  dataset_key   CHAR(36)        NOT NULL,
  name          VARCHAR(100)    NOT NULL,
  -- builtin 不落表；这里只会有 private / community
  source        VARCHAR(16)     NOT NULL,
  -- 作者。社区数据集在作者注销时**不删**，改为置 NULL（别人可能正在引用）
  owner_user_id BIGINT UNSIGNED NULL,
  -- FileStorage 的标识；内容由它读
  storage_id    VARCHAR(64)     NOT NULL,
  -- 入库时抓的结构快照（JSON）。运行时只读它，不再起容器查 information_schema
  schema_json   LONGTEXT        NULL,
  table_count   INT             NOT NULL DEFAULT 0,
  row_count     INT             NOT NULL DEFAULT 0,
  size_bytes    INT             NOT NULL DEFAULT 0,
  -- draft | published | taken_down
  status        VARCHAR(16)     NOT NULL DEFAULT 'draft',
  created_at    DATETIME(3)     NOT NULL,
  updated_at    DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sql_dataset_key (dataset_key),
  -- 选择器按"我的"取私人集，所以按 (owner, source) 建索引
  KEY idx_sql_dataset_owner (owner_user_id, source),
  -- 社区集按状态浏览
  KEY idx_sql_dataset_status (status, source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 举报与下架（照搬 V9 的 note_reports 形状：同一套治理手段，不另造一套）
CREATE TABLE sql_dataset_reports (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dataset_key      CHAR(36)        NOT NULL,
  reporter_user_id BIGINT UNSIGNED NOT NULL,
  reason           VARCHAR(500)    NOT NULL,
  -- pending | taken_down。留字符串而不是布尔：将来加"驳回"不必改列类型
  status           VARCHAR(16)     NOT NULL DEFAULT 'pending',
  created_at       DATETIME(3)     NOT NULL,
  handled_at       DATETIME(3)     NULL,
  PRIMARY KEY (id),
  KEY idx_sql_dataset_report_status (status, created_at),
  -- 同一个人对同一个数据集只留一条：重复提交是"再用一次"，不该堆成一屏
  UNIQUE KEY uk_sql_dataset_report_once (dataset_key, reporter_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
