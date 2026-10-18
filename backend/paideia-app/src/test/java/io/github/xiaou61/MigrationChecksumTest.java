package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 迁移文件的校验和守卫：**已应用过的迁移不得再改**。
 *
 * <p><b>为什么需要这个测试</b>：Flyway 把每个已应用迁移的校验和记进 {@code flyway_schema_history}，
 * 下次启动时逐条比对。改了已经跑过的迁移文件，Flyway 会拒绝启动——但这只有**在那个库上**跑才会发现。
 * 2026-10-18 就是这样弄坏了一个库：`V14` 第一次只有 `CREATE TABLE learner_profiles` 一句、
 * 应用成功；之后又往同一个文件里追加了另三句（`ai_conversations.harness` 列、记忆表的唯一键、
 * `ai_interview_evaluations` 表），而那个库再没跑过 V14。结果是 history 说"已应用"、三样对象却不存在，
 * 库里还留着一批用过旧结构的测试账号——看起来像"库坏了"，实际根因就是改了已应用的迁移。
 *
 * <p><b>规则</b>：迁移一经提交就冻结，要改表就新增一个版本号更高的文件。本测试把它钉死：
 * 已登记的文件内容一变（或文件消失），立刻红。
 *
 * <p>新增迁移会让 {@link #newMigrationsAreRecorded} 红一次，这是刻意的——登记这一步必须留痕。
 * 用 {@code -Dmigration.checksums.update=true} 重跑一次就登记好了，然后 review
 * {@code src/test/resources/migration-checksums.txt} 的 diff：那份 diff 就是"这次动了哪些迁移"的清单。
 *
 * <p>校验和算法与 Flyway 一致：把每一行（不含行尾）按 UTF-8 依次喂给 CRC32，首行去掉 BOM，
 * 取低 32 位。这里刻意自己算而不用 Flyway 的 internal API——版本升级不该让守卫失效，
 * 而自算的自洽性足够回答"这个文件跟登记时是不是同一份"。
 */
class MigrationChecksumTest {

    private static final String UPDATE_FLAG = "migration.checksums.update";
    private static final Path SNAPSHOT = Path.of("src/test/resources/migration-checksums.txt");

    /** 三处 locations 都要扫：漏掉一处，那里改成什么都不会有人发现。 */
    private static final List<Path> MIGRATION_ROOTS = List.of(
            Path.of("src/main/resources/db/migration"),
            Path.of("src/main/resources/db/devdata"),
            Path.of("../paideia-persistence/src/test/resources/db/testdata"));

    @Test
    @DisplayName("已登记过的迁移文件内容不得改动，也不得删改文件名")
    void appliedMigrationsAreNotEdited() throws IOException {
        if (!Files.exists(SNAPSHOT)) {
            return; // 首次生成交给 newMigrationsAreRecorded
        }
        Map<String, Integer> recorded = read();
        Map<String, Integer> current = scan();

        List<String> problems = new java.util.ArrayList<>();
        recorded.forEach((file, sum) -> {
            Integer now = current.get(file);
            if (now == null) {
                problems.add("- 迁移文件消失或改名：" + file);
            } else if (now.intValue() != sum.intValue()) {
                problems.add("~ 已应用的迁移被改动：" + file + "（登记 " + sum + "，现在 " + now + "）");
            }
        });

        assertThat(problems)
                .as("""
                        迁移文件与登记不一致。
                        已应用过的迁移**不能再改**：Flyway 会认为历史对不上，那个库就停在"history 说已应用、
                        对象却不存在"的半迁移状态（2026-10-18 踩过，只能删库重建）。
                        要改表请新增一个更高版本号的文件（如 V16__xxx.sql），不要改 V14。
                        若只是新增了迁移，见 newMigrationsAreRecorded 的提示。""")
                .isEmpty();
    }

    @Test
    @DisplayName("新增迁移必须在清单里登记（-Dmigration.checksums.update=true 自动登记）")
    void newMigrationsAreRecorded() throws IOException {
        Map<String, Integer> current = scan();
        Map<String, Integer> recorded = Files.exists(SNAPSHOT) ? read() : Map.of();

        List<String> unknown = current.keySet().stream()
                .filter(file -> !recorded.containsKey(file))
                .sorted()
                .toList();
        if (unknown.isEmpty()) {
            return;
        }

        if (Boolean.getBoolean(UPDATE_FLAG)) {
            Map<String, Integer> merged = new TreeMap<>(recorded);
            unknown.forEach(file -> merged.put(file, current.get(file)));
            write(merged);
            System.out.println("[migration-checksums] 已登记 " + unknown.size() + " 个新迁移："
                    + unknown + "（请 review 这份 diff 再提交）");
            return;
        }

        assertThat(unknown)
                .as("""
                        有迁移文件还没登记校验和。
                        这是新增迁移的正常一步：用 -Dmigration.checksums.update=true 重跑本测试完成登记，
                        再 review src/test/resources/migration-checksums.txt 的 diff 并与迁移一起提交。
                        注意：该开关**只登记新文件**，不会改写已登记文件的校验和——改旧迁移永远是红的。""")
                .isEmpty();
    }

    /** 扫三处目录，键用相对路径（统一成 `/`，Windows 与 Linux 得到同一份清单）。 */
    private static Map<String, Integer> scan() throws IOException {
        Map<String, Integer> result = new TreeMap<>();
        for (Path root : MIGRATION_ROOTS) {
            assertThat(Files.isDirectory(root))
                    .as("迁移目录不存在：%s —— 目录被移动或改名了？守卫必须真的扫到它，否则等于静默失效",
                            root.toAbsolutePath().normalize())
                    .isTrue();
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".sql")).toList()) {
                    result.put(file.normalize().toString().replace('\\', '/'), checksum(file));
                }
            }
        }
        return result;
    }

    /** 与 Flyway 相同的算法：逐行（不含行尾）拼接后算 CRC32，首行去 BOM。 */
    private static int checksum(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        for (int index = 0; index < lines.size(); index++) {
            String line = index == 0 ? lines.get(index).replace("\uFEFF", "") : lines.get(index);
            crc.update(line.getBytes(StandardCharsets.UTF_8));
        }
        return (int) crc.getValue();
    }

    private static Map<String, Integer> read() throws IOException {
        Map<String, Integer> result = new TreeMap<>();
        for (String line : Files.readAllLines(SNAPSHOT, StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            int tab = line.lastIndexOf(' ');
            result.put(line.substring(0, tab), Integer.parseInt(line.substring(tab + 1).trim()));
        }
        return result;
    }

    private static void write(Map<String, Integer> checksums) throws IOException {
        StringBuilder text = new StringBuilder("# 迁移文件校验和。改一行都会让守卫同历史对不上，新增迁移用");
        text.append(" -Dmigration.checksums.update=true 登记。\n");
        checksums.forEach((file, sum) -> text.append(file).append(' ').append(sum).append('\n'));
        Files.writeString(SNAPSHOT, text.toString(), StandardCharsets.UTF_8);
    }
}
