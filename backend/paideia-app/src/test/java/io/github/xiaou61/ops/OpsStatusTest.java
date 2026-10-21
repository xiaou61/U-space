package io.github.xiaou61.ops;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 四态判定的边界（WORK-028 AC-001/AC-005）。
 *
 * <p>重点不是"大于阈值会报红"，而是**没有数据时必须是未知**。NaN 与 null 参与比较
 * 全是 false，一旦漏掉这两支，结果会一路落到 OK——"没数据"变成"一切正常"，
 * 而这正是这类页面最危险的一种错。
 */
class OpsStatusTest {

    @Test
    @DisplayName("阈值两侧与边界值")
    void thresholdsAreInclusive() {
        assertThat(OpsStatus.of(0.10, 0.70, 0.85)).isEqualTo(OpsStatus.OK);
        assertThat(OpsStatus.of(0.699, 0.70, 0.85)).isEqualTo(OpsStatus.OK);
        assertThat(OpsStatus.of(0.70, 0.70, 0.85)).isEqualTo(OpsStatus.WARN);
        assertThat(OpsStatus.of(0.849, 0.70, 0.85)).isEqualTo(OpsStatus.WARN);
        assertThat(OpsStatus.of(0.85, 0.70, 0.85)).isEqualTo(OpsStatus.ERROR);
        assertThat(OpsStatus.of(2.0, 0.70, 0.85)).isEqualTo(OpsStatus.ERROR);
    }

    @Test
    @DisplayName("无数据是未知，不是正常")
    void missingDataIsUnknown() {
        assertThat(OpsStatus.of(null, 0.70, 0.85)).isEqualTo(OpsStatus.UNKNOWN);
        assertThat(OpsStatus.of(Double.NaN, 0.70, 0.85)).isEqualTo(OpsStatus.UNKNOWN);
        assertThat(OpsStatus.of(Double.POSITIVE_INFINITY, 0.70, 0.85)).isEqualTo(OpsStatus.UNKNOWN);
        assertThat(OpsStatus.of(Double.NEGATIVE_INFINITY, 0.70, 0.85)).isEqualTo(OpsStatus.UNKNOWN);
    }

    @Test
    @DisplayName("分组取最严重的一档；未知不压过已知的异常")
    void worstPicksMostSevere() {
        assertThat(OpsStatus.worst(OpsStatus.OK, OpsStatus.WARN)).isEqualTo(OpsStatus.WARN);
        assertThat(OpsStatus.worst(OpsStatus.WARN, OpsStatus.ERROR, OpsStatus.OK)).isEqualTo(OpsStatus.ERROR);
        assertThat(OpsStatus.worst(OpsStatus.UNKNOWN, OpsStatus.OK)).isEqualTo(OpsStatus.UNKNOWN);
        // 已经知道某项异常时，另一项"未知"不该把整组降级成未知
        assertThat(OpsStatus.worst(OpsStatus.UNKNOWN, OpsStatus.ERROR)).isEqualTo(OpsStatus.ERROR);
        assertThat(OpsStatus.worst(OpsStatus.UNKNOWN, OpsStatus.WARN)).isEqualTo(OpsStatus.WARN);
        assertThat(OpsStatus.worst()).isEqualTo(OpsStatus.OK);
    }
}
