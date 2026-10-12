-- 个人笔记（WORK-006 TASK-007，范围由原 WORK-007 并入）。
--
-- 与知识库的条目**不是一类东西**：条目是"管理员写、所有人读同一份"，笔记是"用户写、
-- 只有自己能看（除非主动分享）"。两套可见性规则相反，所以它们不在同一个模块里
-- （见 design 的「组件与职责」）。
--
-- 与 V4/V5/V6 一致：**不建跨模块外键**，级联删除由应用层按清单负责。
-- entry_id 只存数字：条目被删不该连带删掉用户自己的笔记——笔记本身仍有价值。

CREATE TABLE notes (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  -- 可挂靠某个知识条目；不是每条笔记都必须挂在条目上。
  entry_id   BIGINT UNSIGNED NULL,
  title      VARCHAR(200)    NOT NULL,
  body       MEDIUMTEXT      NOT NULL,
  created_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  -- 列表按"最近改过的在前"：笔记是天天写的，时间就是最自然的排序
  KEY idx_note_user (user_id, updated_at)
) ENGINE = InnoDB;
