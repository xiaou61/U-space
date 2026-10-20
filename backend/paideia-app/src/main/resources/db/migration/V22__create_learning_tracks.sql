-- 学习轨道（WORK-024）：把「从零开始」从知识库里拆出来，成为一个独立的东西。
--
-- **为什么不再复用分类标记**（WORK-020 的裁决，本项推翻）：那条路下"学习线"只是知识库分类的
-- 一个属性，于是课程和文章浏览混在同一个入口里、没有关卡感、也没有关卡之间的依赖。
-- 用户要的是"闯关/图谱"那种形态，那需要**自己的导航层**。
--
-- 取舍：**关卡正文仍然是 knowledge_entries**。理由是不重复造渲染器与进度：
-- markdown 渲染、quiz/code-challenge 块、learning_progress 的通过判定与复习排期全部现成，
-- 自建一套等于把四件事各写一遍。所以这里只新增"导航"这一层，正文仍由 entry 承载。
--
-- 轨道与关卡的关系：
--   learning_tracks        一条专题（Java 从零开始）
--   learning_track_stages  轨道内的一关，`seq` 决定顺序，`chapter` 用于地图分组
--   `requires_entry_id`    前置关卡 —— **这就是"线一个一个关联"那条边**
--
-- 时间列由应用传值（UTC），不用 DEFAULT CURRENT_TIMESTAMP——项目纪律。

CREATE TABLE learning_tracks (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  -- 路径里用的稳定标识：/learn/java
  slug        VARCHAR(64)     NOT NULL,
  name        VARCHAR(128)    NOT NULL,
  -- 一句话说清这条轨道讲什么（列表页用）
  summary     VARCHAR(500)    NULL,
  -- course（从零学）| reading（读源码）——与既有 line_kind 同名，前端分组逻辑可复用
  kind        VARCHAR(16)     NOT NULL DEFAULT 'course',
  sort_order  INT             NOT NULL DEFAULT 100,
  created_at  DATETIME(3)     NOT NULL,
  updated_at  DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_learning_tracks_slug (slug),
  KEY idx_learning_tracks_kind (kind, sort_order)
) ENGINE = InnoDB;

CREATE TABLE learning_track_stages (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  track_id          BIGINT UNSIGNED NOT NULL,
  -- 正文与通过判定都在这一行指向的条目上
  entry_id          BIGINT UNSIGNED NOT NULL,
  -- 轨道内顺序，从 1 开始
  seq               INT             NOT NULL,
  -- 地图分组（"入门篇""对象篇"…）。空串表示不分章
  chapter           VARCHAR(64)     NOT NULL DEFAULT '',
  -- 前置关卡：为空表示无前置（第一关）。**这就是关卡间的关联边**
  requires_entry_id BIGINT UNSIGNED NULL,
  created_at        DATETIME(3)     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_learning_stage (track_id, seq),
  -- 同一轨道里同一个条目只能出现一次（否则进度会指向两关）
  UNIQUE KEY uk_learning_stage_entry (track_id, entry_id),
  KEY idx_learning_stage_requires (track_id, requires_entry_id),
  CONSTRAINT fk_learning_stage_entry FOREIGN KEY (entry_id)
    REFERENCES knowledge_entries (id) ON DELETE CASCADE
) ENGINE = InnoDB;
