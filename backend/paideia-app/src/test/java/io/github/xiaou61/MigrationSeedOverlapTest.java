package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 生产迁移不得与种子数据抢自增 id（2026-10-21 实测回归的守卫）。
 *
 * <p><b>为什么需要</b>：`V24` 用 `INSERT ... SELECT` 给「八股文」建分类时**没指定 id**。
 * 在生产库上表里已有 16 个分类，自增给了 20，一切正常；但在**全新库**上整张表是空的，
 * 它拿到 **id = 1**，紧接着 `devdata/V951` 的种子要插入硬编码的 `id = 1`（「后端基础」），
 * 主键冲突。表现是所有需要数据库的测试在 **Spring 上下文启动阶段**就全红——
 * 不是某个用例失败，是 201 个错误，看起来像"整个项目坏了"。
 *
 * <p><b>这类 bug 的形态</b>：只在"零数据的新库"上出现。生产库和开发库都有历史数据，
 * 所以它们永远看不出来；直到某次合并后在干净测试库上跑全量才炸。
 * 换任何迁移工具都不会消失（这是"生产迁移写业务数据 + 种子用硬编码 id"的组合造成的，
 * 与 Flyway 的版本模型无关），所以要靠这条纪律而不是换工具。
 *
 * <p><b>规则</b>：生产迁移（`db/migration`）向某张表插行时，若 `db/devdata` 也会往
 * 同一张表插行，则**必须显式指定 `id` 列**，并且 id 落在保留高位（≥ 900），
 * 这样它既不会和自增撞，也不会和种子的低位 id 撞。
 */
class MigrationSeedOverlapTest {

    private static final Path MIGRATION_DIR = Path.of("src/main/resources/db/migration");
    private static final Path SEED_DIR = Path.of("src/main/resources/db/devdata");

    /** 种子 id 的安全起点：种子自己用 1..99，夹具用 900+，生产数据插入应从 900 起。 */
    private static final int RESERVED_ID_FLOOR = 900;

    /**
     * 历史遗留，已应用且已推送，改不了。
     *
     * <p>`V24` 的这条 INSERT 在生产库上拿到 id=20（安全），只在全新库上会拿到 1。
     * 它的冲突已由 `devdata/V949__shift_essay_category_id.sql` 向前修复。
     * **不要往这个名单里加新条目**——新迁移请直接按规则写。
     */
    private static final Set<String> ALLOWED = Set.of("V24__add_entry_source_and_fingerprint.sql");

    @Test
    @DisplayName("生产迁移向种子表插行时必须显式给保留区间内的 id")
    void productionInsertIntoSeededTableMustPinId() throws IOException {
        Set<String> seededTables = seededTables();
        List<String> violations = new ArrayList<>();

        for (Path file : sqlFiles(MIGRATION_DIR)) {
            String name = file.getFileName().toString();
            if (ALLOWED.contains(name)) {
                continue;
            }
            for (Insert insert : insertsOf(Files.readString(file, StandardCharsets.UTF_8))) {
                if (!seededTables.contains(insert.table())) {
                    continue;   // 种子不碰这张表 → 不会抢 id，放行
                }
                if (!insert.columns().contains("id")) {
                    violations.add(name + " → INSERT INTO " + insert.table()
                            + " 未指定 id；种子也用这张表，全新库上会撞主键");
                }
            }
        }

        assertThat(violations)
                .as("生产迁移与种子数据抢自增 id。修法：显式写 id 且 ≥ %d，或把数据插入挪到应用启动时幂等 bootstrap",
                        RESERVED_ID_FLOOR)
                .isEmpty();
    }

    /** 自检：解析器本身要能认出 V24 那种"不带 id 的 INSERT ... SELECT"。 */
    @Test
    @DisplayName("解析器能识别 INSERT ... SELECT 这种无 id 写法")
    void parserDetectsInsertWithoutId() {
        String sql = "INSERT INTO knowledge_categories (name, slug, sort_order)\n"
                + "SELECT 'x', 'y', 50 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM knowledge_categories);";
        assertThat(insertsOf(sql))
                .singleElement()
                .satisfies(insert -> {
                    assertThat(insert.table()).isEqualTo("knowledge_categories");
                    assertThat(insert.columns()).doesNotContain("id");
                });
    }

    @Test
    @DisplayName("解析器能识别多行 VALUES 与显式 id")
    void parserDetectsExplicitId() {
        String sql = "INSERT INTO users (id, username, email) VALUES (1, 'a', 'a@b.c'), (2, 'd', 'e@f.g');";
        assertThat(insertsOf(sql))
                .singleElement()
                .satisfies(insert -> assertThat(insert.columns()).contains("id"));
    }

    private static List<Path> sqlFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(path -> path.toString().endsWith(".sql")).sorted().toList();
        }
    }

    private static Set<String> seededTables() throws IOException {
        Set<String> tables = new LinkedHashSet<>();
        for (Path file : sqlFiles(SEED_DIR)) {
            insertsOf(Files.readString(file, StandardCharsets.UTF_8)).forEach(i -> tables.add(i.table()));
        }
        return tables;
    }

    /**
     * 抽出一段 SQL 里所有 `INSERT INTO <table> (<cols>)`。
     *
     * <p>只认带列清单的写法：不带列清单的 INSERT 一定会用自增 id，本身就是违规，
     * 由调用方按"没有 id 列"处理——所以这里返回空列清单。
     */
    private static List<Insert> insertsOf(String sql) {
        List<Insert> inserts = new ArrayList<>();
        // 去掉 -- 行注释，避免注释里的示例被当成真语句
        String stripped = sql.replaceAll("(?m)--.*$", "");
        Pattern pattern = Pattern.compile(
                "INSERT\\s+INTO\\s+`?(\\w+)`?\\s*(?:\\(([^)]*)\\))?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(stripped);
        while (matcher.find()) {
            String table = matcher.group(1).toLowerCase(Locale.ROOT);
            String rawColumns = matcher.group(2);
            List<String> columns = rawColumns == null
                    ? List.of()
                    : Stream.of(rawColumns.split(","))
                            .map(column -> column.trim().replace("`", "").toLowerCase(Locale.ROOT))
                            .filter(column -> !column.isEmpty())
                            .toList();
            inserts.add(new Insert(table, columns));
        }
        return inserts;
    }

    private record Insert(String table, List<String> columns) {
    }
}
