package io.github.xiaou61.sandbox.internal.dataset;

import io.github.xiaou61.sandbox.internal.run.CodeRun;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 把 `information_schema` 的查询结果转成结构 JSON（WORK-035 T-09）。
 *
 * <p>输出形状与构建期脚本 `scripts/gen-dataset-schema.mjs` **逐字段一致**——
 * 前端 `dataset-schema.ts` 只认识这一种形状，两处不一致会让内置集与上传集表现不同。
 *
 * <p>这个转换在服务端做而不是让前端做：上传流程要立刻知道"几张表"，而那是入库字段。
 */
final class SqlDatasetSchemaReader {

    /** 与前端 `SchemaTable` 的字段名一一对应。 */
    private static final List<String> COLUMNS = List.of("tbl", "col", "typ", "nullable", "colkey");

    private SqlDatasetSchemaReader() {
    }

    /**
     * @param table 探测查询的结果集（列：tbl / col / typ / nullable / colkey）
     * @return 结构 JSON；没有列信息时返回 null
     */
    static String toJson(CodeRun.Table table) {
        // 探测查询的列顺序由 SQL 决定，不假设；按列名定位
        Map<String, Integer> index = new LinkedHashMap<>();
        for (int i = 0; i < table.columns().size(); i++) {
            index.put(table.columns().get(i), i);
        }
        if (!COLUMNS.stream().allMatch(index::containsKey)) {
            // 结果集形状不对（镜像换了、查询被改过）——返回 null 而不是造一份错的结构
            return null;
        }

        // 表名 → 列列表。用 LinkedHashMap 保留 SQL 的 ORDER BY 顺序
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        for (List<String> row : table.rows()) {
            String tableName = cell(row, index.get("tbl"));
            if (tableName.isEmpty()) {
                continue;
            }
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("name", cell(row, index.get("col")));
            column.put("type", cell(row, index.get("typ")));
            column.put("nullable", "YES".equals(cell(row, index.get("nullable"))));
            column.put("primaryKey", "PRI".equals(cell(row, index.get("colkey"))));
            tables.computeIfAbsent(tableName, name -> new ArrayList<>()).add(column);
        }
        if (tables.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : tables.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("columns", entry.getValue());
            list.add(item);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("tables", list);
        // 外键关系留空：上传的数据集通常不建外键，作者可以后续手工标注
        root.put("relations", List.of());
        return write(root);
    }

    /**
     * 最小 JSON 序列化：只处理 Map / List / String / Boolean。
     *
     * <p>**为什么不引 Jackson**：本模块只需要写出这一种固定形状，而引入一个序列化器
     * 会让"输出形状"变成依赖其默认配置的东西（缩进、null 处理、字段顺序都会随版本变）。
     * 手写 20 行，形状完全可控。
     */
    static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(out, value);
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    private static void append(StringBuilder out, Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String text) {
            out.append('"');
            for (int i = 0; i < text.length(); i++) {
                char ch = text.charAt(i);
                switch (ch) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> {
                        if (ch < 0x20) {
                            out.append(String.format("\\u%04x", (int) ch));
                        } else {
                            out.append(ch);
                        }
                    }
                }
            }
            out.append('"');
        } else if (value instanceof Boolean flag) {
            out.append(flag);
        } else if (value instanceof Number number) {
            out.append(number);
        } else if (value instanceof Map<?, ?> map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) map).entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                append(out, entry.getKey());
                out.append(':');
                append(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof List<?> items) {
            out.append('[');
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                append(out, items.get(i));
            }
            out.append(']');
        } else {
            throw new IllegalArgumentException("不支持的 JSON 值：" + value.getClass());
        }
    }

    /**
     * 从结构 JSON 里数出表数。
     *
     * <p>**只数表数，不数列数**：`row_count` 与 `table_count` 是入库字段，
     * 而"多少列"在界面上由结构自己算（`schema.tables.reduce(...)`）。
     * 为这个数字写一个 JSON 解析器不值得——所以这里按 `"columns":[` 的出现次数数表，
     * 它只出现在表层级。
     */
    static SqlDatasetSchemaCounts count(String schemaJson) {
        if (schemaJson == null || schemaJson.isBlank()) {
            return new SqlDatasetSchemaCounts(0);
        }
        int tables = 0;
        int index = 0;
        while ((index = schemaJson.indexOf("\"columns\":", index)) != -1) {
            tables++;
            index += "\"columns\":".length();
        }
        return new SqlDatasetSchemaCounts(tables);
    }

    private static String cell(List<String> row, Integer position) {
        if (position == null || position >= row.size()) {
            return "";
        }
        String value = row.get(position);
        return value == null || "\u0000NULL".equals(value) ? "" : value;
    }
}

/** 结构统计：表数。行数由上传方按 INSERT 语句数近似（精确值要跑 COUNT，不值得）。 */
record SqlDatasetSchemaCounts(int tables) {
}
