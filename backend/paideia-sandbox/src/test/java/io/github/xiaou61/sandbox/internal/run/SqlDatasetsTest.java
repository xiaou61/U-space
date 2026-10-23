package io.github.xiaou61.sandbox.internal.run;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.sandbox.internal.dataset.DatasetStorage;
import io.github.xiaou61.sandbox.internal.dataset.SqlDatasetMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * 数据集解析的两条边界（WORK-032 / WORK-035）。
 *
 * <p>**为什么这组用例必须存在**：
 * <ol>
 *   <li>隐藏集一旦出现在 `/sandbox/status` 的清单里，前端就会把它渲染进选择器，用户选中跑一句
 *       `SELECT *` 就拿到了全部数据——隐藏集存在的唯一目的当场消失，而界面上看不出异常。</li>
 *   <li>`sql(userId, key)` 是私人数据集的**唯一安全边界**。它一旦漏掉用户判定，
 *       任何人猜到 key 就能跑别人的私人数据集。</li>
 * </ol>
 */
class SqlDatasetsTest {

    private static final long OWNER = 7L;
    private static final long STRANGER = 8L;

    /** 一个只认识固定几行的假 Mapper。真实 SQL 行为由集成测试覆盖。 */
    private static SqlDatasetMapper mapperOf(List<SqlDatasetMapper.Row> rows) {
        return new SqlDatasetMapper() {
            @Override
            public int insert(String datasetKey, String name, String source, Long ownerUserId,
                              String storageId, String schemaJson, int tableCount, int rowCount,
                              int sizeBytes, String status, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<Row> findByKey(String datasetKey) {
                return rows.stream().filter(row -> row.datasetKey().equals(datasetKey)).findFirst();
            }

            @Override
            public List<Summary> listVisible(long userId) {
                return rows.stream()
                        .filter(row -> SqlDatasets.visible(row, userId))
                        .map(row -> new Summary(row.datasetKey(), row.name(), row.source(),
                                row.ownerUserId(), row.tableCount(), row.rowCount()))
                        .toList();
            }

            @Override
            public int updateSnapshot(String datasetKey, String schemaJson, int tableCount,
                                      int rowCount, int sizeBytes, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int updateStatus(String datasetKey, String status, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int deleteOwned(String datasetKey, long userId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int deletePrivateByOwner(long userId) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int orphanCommunityByOwner(long userId, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<Summary> listCommunity(String status, int limit) {
                return List.of();
            }

            @Override
            public int upsertReport(String datasetKey, long reporterUserId, String reason, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<ReportRow> listReports(String status, int limit) {
                return List.of();
            }

            @Override
            public int countReports(String datasetKey) {
                return 0;
            }

            @Override
            public int markReportsHandled(String datasetKey, String status, Instant now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int deleteReportsByReporter(long userId) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /** 内容存储：id → 文本。 */
    private static DatasetStorage storageOf(Map<String, String> content) {
        return id -> Optional.ofNullable(content.get(id)).map(text -> text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static SqlDatasetMapper.Row row(String key, String source, Long owner, String status) {
        return new SqlDatasetMapper.Row(1L, key, key, source, owner, "storage-" + key,
                "{\"tables\":[],\"relations\":[]}", 1, 1, 100, status,
                Instant.EPOCH, Instant.EPOCH);
    }

    private static SqlDatasets datasets(List<SqlDatasetMapper.Row> rows, Map<String, String> content) {
        return new SqlDatasets(mapperOf(rows), storageOf(content));
    }

    // ── 内置集 ────────────────────────────────────────────────────────────

    /** 下发清单里不许出现隐藏集，且公开集必须仍在（否则选择器空掉）。 */
    @Test
    void publicKeysExcludesHiddenDatasets() {
        SqlDatasets datasets = datasets(List.of(), Map.of());
        assertThat(datasets.publicKeys())
                .as("隐藏集不得下发给客户端——它出现在选择器里就等于公开")
                .doesNotContain("orders-hidden-a")
                .contains("orders-public");
    }

    /** 隐藏集仍必须能跑：题面明文写着它的 key，拒绝运行会让关卡做不了。 */
    @Test
    void hiddenDatasetsStillResolvable() {
        SqlDatasets datasets = datasets(List.of(), Map.of());
        assertThat(datasets.isBuiltin("orders-hidden-a")).isTrue();
        assertThat(datasets.sql(STRANGER, "orders-hidden-a")).isNotBlank();
    }

    /** 公开清单必须是全集的真子集——相等说明过滤没生效。 */
    @Test
    void publicKeysIsStrictSubset() {
        SqlDatasets datasets = datasets(List.of(), Map.of());
        assertThat(datasets.publicKeys()).isNotEqualTo(datasets.keys().stream().sorted().toList());
    }

    /**
     * 结构快照必须真的在（WORK-035 T-03）。
     *
     * <p>**这条守的是"数据集改了但快照忘了重新生成"**：那种情况下补全会提示一个不存在的列，
     * 或者漏掉真实存在的列，而两种错都没有任何报错。
     */
    @Test
    void schemaSnapshotExistsAndParses() {
        SqlDatasets datasets = datasets(List.of(), Map.of());
        String schema = datasets.schema(OWNER, "orders-public");
        assertThat(schema).as("内置数据集必须有结构快照，否则补全与表结构侧栏都空").isNotNull();
        assertThat(schema).contains("customers", "orders", "order_items");
    }

    /** 未知 key 返回 null（调用方转 404），不抛异常。 */
    @Test
    void schemaOfUnknownKeyIsNull() {
        assertThat(datasets(List.of(), Map.of()).schema(OWNER, "no-such-dataset")).isNull();
    }

    // ── 私人 / 社区集：本项唯一的安全边界（AC-007） ─────────────────────────

    /**
     * **AC-007 反向测试**：用户 B 用用户 A 的私人数据集 key，必须拿不到内容。
     *
     * <p>这条是整项里最该有的一条测试。没有它，`sql(userId, key)` 里漏写一个 owner 判定
     * 不会让任何测试变红，只会让一个人的私人数据悄悄对所有人开放。
     */
    @Test
    void privateDatasetIsNotReadableByOthers() {
        SqlDatasets datasets = datasets(
                List.of(row("my-private", "private", OWNER, "draft")),
                Map.of("storage-my-private", "CREATE TABLE secret (id INT);"));

        assertThat(datasets.sql(OWNER, "my-private")).as("作者本人必须能跑").isNotNull();
        assertThat(datasets.sql(STRANGER, "my-private"))
                .as("他人拿到 key 也不能跑——这是本项唯一的安全边界")
                .isNull();
    }

    /**
     * 无权访问与不存在**返回同一个结果**（都是 null）。
     *
     * <p>如果两者可区分，攻击者就能靠文案差异枚举出"哪些 key 存在"。
     */
    @Test
    void forbiddenAndMissingAreIndistinguishable() {
        SqlDatasets datasets = datasets(
                List.of(row("my-private", "private", OWNER, "draft")),
                Map.of("storage-my-private", "x"));

        assertThat(datasets.sql(STRANGER, "my-private")).isEqualTo(datasets.sql(STRANGER, "does-not-exist"));
    }

    /** 社区集必须已发布：draft 与 taken_down 都不该被读到。 */
    @Test
    void communityDatasetRequiresPublished() {
        SqlDatasets datasets = datasets(
                List.of(
                        row("shared-ok", "community", OWNER, "published"),
                        row("shared-draft", "community", OWNER, "draft"),
                        row("shared-down", "community", OWNER, "taken_down")),
                Map.of("storage-shared-ok", "a", "storage-shared-draft", "b", "storage-shared-down", "c"));

        assertThat(datasets.sql(STRANGER, "shared-ok")).as("已发布的社区集任何人可跑").isNotNull();
        assertThat(datasets.sql(STRANGER, "shared-draft")).as("草稿还没发布").isNull();
        assertThat(datasets.sql(STRANGER, "shared-down")).as("已下架的不能再跑").isNull();
    }

    /** 选择器：内置公开集 + 自己的私人集 + 已发布社区集，不含别人的私人集。 */
    @Test
    void optionsIncludeOwnAndPublicButNotOthersPrivate() {
        SqlDatasets datasets = datasets(
                List.of(
                        row("mine", "private", OWNER, "draft"),
                        row("theirs", "private", STRANGER, "draft"),
                        row("shared", "community", STRANGER, "published")),
                Map.of());

        List<String> keys = datasets.options(OWNER).stream().map(SqlDatasets.DatasetOption::key).toList();
        assertThat(keys).contains("orders-public", "mine", "shared");
        assertThat(keys).as("别人的私人集不该出现在我的选择器里").doesNotContain("theirs");
        assertThat(keys).as("隐藏集永远不进选择器").doesNotContain("orders-hidden-a");
    }

    /** 来源字段要让前端能分组：内置 / 私人 / 社区。 */
    @Test
    void optionsCarrySource() {
        SqlDatasets datasets = datasets(
                List.of(row("mine", "private", OWNER, "draft"),
                        row("shared", "community", STRANGER, "published")),
                Map.of());

        Map<String, String> byKey = datasets.options(OWNER).stream()
                .collect(java.util.stream.Collectors.toMap(
                        SqlDatasets.DatasetOption::key, SqlDatasets.DatasetOption::source));
        assertThat(byKey.get("orders-public")).isEqualTo("builtin");
        assertThat(byKey.get("mine")).isEqualTo("private");
        assertThat(byKey.get("shared")).isEqualTo("community");
    }
}
