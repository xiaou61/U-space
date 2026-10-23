package io.github.xiaou61.sandbox.internal.run;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 数据集白名单的两条边界（WORK-032 回归）。
 *
 * <p>**为什么这组用例必须存在**：隐藏集一旦出现在 `/sandbox/status` 的 `datasets` 里，
 * 前端就会把它渲染进选择器，用户选中跑一句 `SELECT *` 就拿到了全部数据——隐藏集存在的
 * 唯一目的（"没法把公开集上的答案硬编码进去"）当场消失，而界面上看不出任何异常。
 */
class SqlDatasetsTest {

    private final SqlDatasets datasets = new SqlDatasets();

    /** 下发清单里不许出现隐藏集，且公开集必须仍在（否则选择器空掉）。 */
    @Test
    void publicKeysExcludesHiddenDatasets() {
        assertThat(datasets.publicKeys())
                .as("隐藏集不得下发给客户端——它出现在选择器里就等于公开")
                .doesNotContain("orders-hidden-a")
                .contains("orders-public");
    }

    /** 隐藏集仍必须能跑：题面明文写着它的 key，拒绝运行会让关卡做不了。 */
    @Test
    void hiddenDatasetsStillResolvable() {
        assertThat(datasets.exists("orders-hidden-a")).isTrue();
        assertThat(datasets.sql("orders-hidden-a")).isNotBlank();
    }

    /** 公开清单必须是全集的真子集——相等说明过滤没生效。 */
    @Test
    void publicKeysIsStrictSubset() {
        assertThat(datasets.publicKeys()).isNotEqualTo(datasets.keys().stream().sorted().toList());
    }

    /**
     * 结构快照必须真的在（WORK-035 T-03）。
     *
     * <p>**这条守的是"数据集改了但快照忘了重新生成"**：那种情况下补全会提示一个不存在的列，
     * 或者漏掉真实存在的列，而两种错都没有任何报错。断言表名足够——列数会随课程调整，
     * 但表名变化意味着快照肯定过期了。
     */
    @Test
    void schemaSnapshotExistsAndParses() {
        String schema = datasets.schema("orders-public");
        assertThat(schema).as("内置数据集必须有结构快照，否则补全与表结构侧栏都空").isNotNull();
        assertThat(schema).contains("customers", "orders", "order_items");
    }

    /** 未知 key 返回 null（调用方转 404），不抛异常。 */
    @Test
    void schemaOfUnknownKeyIsNull() {
        assertThat(datasets.schema("no-such-dataset")).isNull();
    }
}
