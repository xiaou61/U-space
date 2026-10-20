-- 关卡难度（WORK-024）。
--
-- **为什么是新增一列而不是改 V22**：V22 已在线上执行过，Flyway 会比对已应用迁移的
-- 校验和——改一行就让所有环境起不来（2026-10-20 真的踩了，线上健康检查超时）。
-- 与 V10 / V954 同一个处理方式：新迁移只做增量。
--
-- 取值：beginner（入门）/ intermediate（进阶）/ advanced（深入）。
-- 用字符串不用枚举：难度档位以后可能细分（比如加 expert），改表比重建枚举便宜。
--
-- 默认 beginner 是安全的：老数据（如果有）不会被标成"深入"而吓退初学者。

ALTER TABLE learning_track_stages
  ADD COLUMN level VARCHAR(16) NOT NULL DEFAULT 'beginner' AFTER requires_entry_id;

-- 地图上按轨道 + 难度筛选时会用到
CREATE INDEX idx_learning_stage_level ON learning_track_stages (track_id, level);
