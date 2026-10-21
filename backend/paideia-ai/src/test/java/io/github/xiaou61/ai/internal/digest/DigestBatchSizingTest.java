package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 分批调用的边界（WORK-027 TASK-006）。
 *
 * <p>**2026-10-21 线上实测**：60 条一次性喂进 DeepSeek，maxTokens 被思考吃满、
 * 静默返回空，60 条全部降级。改成 24 条一批后单批输出量可控。
 *
 * <p>这组用例钉住"分批的切法"，不连上游（真连由 `DigestSummarizerLiveTest` 覆盖）。
 */
class DigestBatchSizingTest {

    @Test
    @DisplayName("60 条候选应被拆成 3 批（24 + 24 + 12）")
    void sixtyItemsSplitIntoThreeBatches() {
        List<Integer> sizes = batches(60);
        assertThat(sizes).containsExactly(24, 24, 12);
    }

    @Test
    @DisplayName("不足一批时只调一次")
    void smallInputIsSingleBatch() {
        assertThat(batches(5)).containsExactly(5);
        assertThat(batches(24)).containsExactly(24);
    }

    @Test
    @DisplayName("恰好整批时不多发一次空调用")
    void exactMultipleDoesNotEmitEmptyBatch() {
        assertThat(batches(48)).containsExactly(24, 24);
    }

    @Test
    @DisplayName("空输入不产生任何批次（不花冤枉钱）")
    void emptyInputHasNoBatch() {
        assertThat(batches(0)).isEmpty();
    }

    /** 复刻 DigestSummarizer 的分批切片，只验证切法。 */
    private static List<Integer> batches(int total) {
        int batchSize = 24;
        List<Integer> sizes = new ArrayList<>();
        for (int start = 0; start < total; start += batchSize) {
            sizes.add(Math.min(start + batchSize, total) - start);
        }
        return sizes;
    }
}
