package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 迁移版本号的防撞守卫（2026-10-21 实测后加）。
 *
 * <p><b>背景</b>：多个 agent 并行做任务时，谁都想"下一个号"。实测（用项目自己的 Flyway 13.10）：
 * 两个分支都写了 {@code V25}，合到一起后 **应用直接起不来**——
 * {@code Found more than one migration with version 25}，不是某个迁移失败，是整个上下文中止。
 *
 * <p><b>为什么改成时间戳</b>：{@code V20261021103045__xxx.sql} 这种 14 位时间戳，
 * 两个 agent 撞号的概率从"必然"降到"同一秒同时生成文件"。实测验证过：时间戳版本接在
 * 现有 {@code V24} 之后能正常续接、重复跑幂等、更晚的时间戳继续追加。
 *
 * <p><b>它治不了什么（别误以为万能）</b>：
 * <ol>
 *   <li>同一秒生成两个文件仍然会撞（实测同样是 {@code Found more than one migration}）——
 *       概率大幅下降，不是零。</li>
 *   <li>"两个 agent 改同一张表的不同列、版本号不冲突但语义冲突"——任何迁移工具都查不出来，
 *       只能靠合并前协调。</li>
 * </ol>
 *
 * <p><b>规则</b>：新增迁移必须用 14 位时间戳（{@code yyyyMMddHHmmss}）。
 * 已存在的 {@code V1..V24} 与 {@code V900+}、{@code V949..V955} 是历史，登记在白名单里不受约束。
 */
class MigrationVersionFormatTest {

    private static final Path MIGRATION_DIR = Path.of("src/main/resources/db/migration");

    /** 14 位时间戳：yyyyMMddHHmmss。 */
    private static final Pattern TIMESTAMP = Pattern.compile("^V(\\d{14})__.+\\.[sS][qQ][lL]$");

    /** 旧的纯数字递增号：V1、V24、V900。只允许历史文件用，新文件不许。 */
    private static final Pattern NUMERIC = Pattern.compile("^V(\\d{1,6})__.+\\.[sS][qQ][lL]$");

    /**
     * 历史文件白名单：它们已经在生产库应用、且已推送，改名会让 Flyway 校验失败。
     *
     * <p><b>不要往这里加新文件</b>——加一个就等于允许一次撞号。新迁移请用时间戳。
     */
    private static final List<String> LEGACY = List.of(
            "V1__create_account_tables.sql",
            "V2__pin_account_table_collation.sql",
            "V3__create_knowledge_tables.sql",
            "V4__create_ai_tables.sql",
            "V5__create_assistant_tables.sql",
            "V6__create_memory_tables.sql",
            "V7__create_note_tables.sql",
            "V8__create_note_share_tables.sql",
            "V9__create_note_report_tables.sql",
            "V10__pin_knowledge_table_collation.sql",
            "V11__create_credit_tables.sql",
            "V12__memory_auto_extraction_and_settings.sql",
            "V13__create_code_runs.sql",
            "V14__create_learner_profiles.sql",
            "V15__create_resume_tables.sql",
            "V16__create_mcp_access_tokens.sql",
            "V17__add_learning_lines.sql",
            "V18__create_learning_progress.sql",
            "V19__create_concept_and_view_tables.sql",
            "V20__add_entry_read_indexes.sql",
            "V21__create_ai_telemetry_tables.sql",
            "V22__create_learning_tracks.sql",
            "V23__add_stage_level.sql",
            "V24__add_entry_source_and_fingerprint.sql");

    @Test
    @DisplayName("新增迁移必须用 14 位时间戳；旧纯数字号只允许白名单里的历史文件")
    void newMigrationsUseTimestamps() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : sqlFiles(MIGRATION_DIR)) {
            String name = file.getFileName().toString();
            if (LEGACY.contains(name)) {
                continue;
            }
            if (TIMESTAMP.matcher(name).matches()) {
                continue;
            }
            if (NUMERIC.matcher(name).matches()) {
                violations.add(name + " → 用了纯数字版本号；新迁移一律用 14 位时间戳"
                        + "（两个 agent 各自的 V25 会让应用起不来：Found more than one migration with version N）");
            } else {
                violations.add(name + " → 文件名不是 V<14位时间戳>__描述.sql 的形状");
            }
        }
        assertThat(violations).as("迁移版本号格式").isEmpty();
    }

    @Test
    @DisplayName("时间戳之间也不得重复（同一秒两个文件仍会撞）")
    void timestampsAreUnique() throws IOException {
        Map<String, List<String>> byVersion = new LinkedHashMap<>();
        for (Path file : sqlFiles(MIGRATION_DIR)) {
            Matcher matcher = TIMESTAMP.matcher(file.getFileName().toString());
            if (matcher.matches()) {
                byVersion.computeIfAbsent(matcher.group(1), key -> new ArrayList<>())
                        .add(file.getFileName().toString());
            }
        }
        List<String> duplicates = byVersion.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " ← " + entry.getValue())
                .toList();
        assertThat(duplicates)
                .as("同一秒生成的迁移仍然会撞（实测确认），改一下秒数重命名即可")
                .isEmpty();
    }

    @Test
    @DisplayName("白名单里的历史文件必须真的存在（改名后白名单会变成静默失效）")
    void legacyEntriesStillExist() throws IOException {
        List<String> present = sqlFiles(MIGRATION_DIR).stream()
                .map(path -> path.getFileName().toString())
                .toList();
        for (String legacy : LEGACY) {
            assertThat(present)
                    .as("白名单里的 %s 已不存在——它被改名或删除了？", legacy)
                    .contains(legacy);
        }
    }

    private static List<Path> sqlFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(path -> path.toString().toLowerCase().endsWith(".sql")).sorted().toList();
        }
    }

    /** 供人工核对：把当前目录下的迁移按版本排一遍。 */
    static List<String> describe(Path dir) throws IOException {
        List<String> names = new ArrayList<>();
        for (Path file : sqlFiles(dir)) {
            names.add(Files.readString(file, StandardCharsets.UTF_8).lines().findFirst().orElse("")
                    + " <= " + file.getFileName());
        }
        return names;
    }
}
