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
}
