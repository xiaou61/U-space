-- 订正 V953 里自测题 starter 缺少右括号（`print(f"unit {step}"` → `print(f"unit {step}")`）。
--
-- V953 已执行过，**不能直接改它**：Flyway 会比对已应用迁移的校验和，改动会让所有环境的
-- 上下文起不来（实测 Migration checksum mismatch for migration version 953）。
-- 与 V10 对建表语句的处理同一个道理：新迁移只做增量订正。
--
-- V953 插入的条目固定是 id=10，这里直接按主键替换那一个片段。
-- SQL 里 \\" 是 JSON 的转义、\\\\n 是 JSON 文本里的 \n 字面量，故用 REPLACE 精确匹配。

UPDATE knowledge_entries
SET body = REPLACE(
        body,
        'print(f\\"unit {step}\\"\\n\\nfor i in range(3)',
        'print(f\\"unit {step}\\")\\n\\nfor i in range(3)'
    ),
    updated_at = '2026-01-01 00:00:00.000'
WHERE id = 10;
