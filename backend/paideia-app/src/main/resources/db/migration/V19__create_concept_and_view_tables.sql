-- 个人知识图谱（WORK-021）：概念、条目↔概念、概念间关系、浏览记录。
--
-- 四张表分成两类：
-- * 前三张是**公共内容**——概念与关系由管理员维护，任何已登录用户都能读；
-- * 第四张是**用户私有数据**——只有本人能读，注销时要删（靠
--   KnowledgeUserDeletionListener 的清单，与 self_assessments / learning_progress 同口径）。
--
-- 三处刻意的取舍：
-- 1. knowledge_concepts.category_id **不建外键**：概念可以不属于任何分类，且分类删除
--    不应该带走概念——概念是知识单元，不是分类的附属物。
-- 2. 三张关联/边表都带 ON DELETE CASCADE：删概念或删条目不留悬空行；
--    浏览表的外键只指条目，用户删除靠监听器。
-- 3. 掌握度**不落库**：它是派生的，输入是新的一次小测、一次到期复习、一次自评。
--    落库就要处理失效与并发，而输入只有几十到几百行。算法替换时只改一个纯函数。
--
-- 时间列全部由应用传值（UTC），不用 DEFAULT CURRENT_TIMESTAMP——项目纪律：
-- 建表默认值同样属于"让数据库取时间"。

CREATE TABLE knowledge_concepts (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 全局唯一：两个同名概念会让图谱出现无法区分的两个点
  name        VARCHAR(64)     NOT NULL,
  -- 一句话说清这个概念是什么；图上点选时显示
  summary     VARCHAR(500)    NULL,
  -- 归属分类，可为空；不建外键，见文件头
  category_id BIGINT UNSIGNED NULL,
  created_at  DATETIME(3)     NOT NULL,
  updated_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_knowledge_concepts_name (name),
  KEY idx_knowledge_concepts_category (category_id)
) ENGINE = InnoDB;

-- 条目是概念的载体：一个条目可以有多个概念，一个概念可以分散在多个条目里。
-- 正是这张表让"三门课考同一个知识点"成为一条数据而不是三个人为的孤立条目。
CREATE TABLE knowledge_entry_concepts (
  entry_id   BIGINT UNSIGNED NOT NULL,
  concept_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3)     NOT NULL,
  PRIMARY KEY (entry_id, concept_id),
  KEY idx_knowledge_entry_concepts_concept (concept_id),
  CONSTRAINT fk_kec_entry FOREIGN KEY (entry_id)
    REFERENCES knowledge_entries (id) ON DELETE CASCADE,
  CONSTRAINT fk_kec_concept FOREIGN KEY (concept_id)
    REFERENCES knowledge_concepts (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- 概念之间的关系。**不从条目级 prerequisite 推导**：两个各挂 5 个概念的条目
-- 会推导出 25 条边，绝大多数没有语义，图会立刻变成一团毛线。
-- 关系必须是人明确写下的。
CREATE TABLE knowledge_concept_edges (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  from_concept_id BIGINT UNSIGNED NOT NULL,
  to_concept_id   BIGINT UNSIGNED NOT NULL,
  -- prerequisite（from 是 to 的前置）| related（无向，只存一条）
  relation_type   VARCHAR(32)     NOT NULL,
  created_at      DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_knowledge_concept_edges (from_concept_id, to_concept_id, relation_type),
  KEY idx_knowledge_concept_edges_to (to_concept_id),
  CONSTRAINT fk_kce_from FOREIGN KEY (from_concept_id)
    REFERENCES knowledge_concepts (id) ON DELETE CASCADE,
  CONSTRAINT fk_kce_to FOREIGN KEY (to_concept_id)
    REFERENCES knowledge_concepts (id) ON DELETE CASCADE,
  CONSTRAINT ck_kce_not_self CHECK (from_concept_id <> to_concept_id),
  CONSTRAINT ck_kce_type CHECK (relation_type IN ('prerequisite', 'related'))
) ENGINE = InnoDB;

-- "看过哪些"——本项目唯一事后补不回来的数据：
-- 分数有 learning_progress、自评有专表、代码运行有 code_runs，只有浏览没有记录。
--
-- 不塞进 learning_progress：那里的一行代表"我开始学这一课了"，会被课程进度当成已开始；
-- 浏览只是路过。混在一起后"什么都没做"和"看了一眼"就分不开了。
CREATE TABLE knowledge_entry_views (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id         BIGINT UNSIGNED NOT NULL,
  entry_id        BIGINT UNSIGNED NOT NULL,
  view_count      INT             NOT NULL DEFAULT 1,
  first_viewed_at DATETIME(3)     NOT NULL,
  last_viewed_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  -- upsert 的唯一落点：同一用户同一篇重复打开只累加，不产生新行
  UNIQUE KEY uk_knowledge_entry_views (user_id, entry_id),
  KEY idx_knowledge_entry_views_entry (entry_id),
  KEY idx_knowledge_entry_views_user_recent (user_id, last_viewed_at),
  CONSTRAINT fk_kev_entry FOREIGN KEY (entry_id)
    REFERENCES knowledge_entries (id) ON DELETE CASCADE
) ENGINE = InnoDB;
