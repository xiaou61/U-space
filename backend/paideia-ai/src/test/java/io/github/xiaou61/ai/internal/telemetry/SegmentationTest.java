package io.github.xiaou61.ai.internal.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 分桶边界（WORK-036 TASK-001/002）。
 *
 * <p>重点盯三件会静默出错的事：48 小时这条分界线、日桶按 zone 的 00:00 对齐、
 * 补零时"没有数据"不能被写成 0。
 */
class SegmentationTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private static Instant at(String local) {
        return LocalDateTime.parse(local).atZone(ZONE).toInstant();
    }

    @Test
    void fortyEightHoursIsStillHourly() {
        // 需求写的是「≤ 48 小时用小时桶」。这条**刻意钉在分界线上**：
        // 把 <= 改成 < 会让 48 小时刚好掉进日桶，而那种偏移在界面上一眼看不出。
        assertThat(Segmentation.of(at("2026-10-20T00:00:00"), at("2026-10-22T00:00:00")))
                .isEqualTo(Segmentation.Granularity.HOUR);
    }

    @Test
    void justOverFortyEightHoursIsDaily() {
        assertThat(Segmentation.of(at("2026-10-20T00:00:00"), at("2026-10-22T00:01:00")))
                .isEqualTo(Segmentation.Granularity.DAY);
    }

    @Test
    void dailyBucketsAlignToMidnightInTheZone() {
        List<Instant> edges = Segmentation.boundaries(at("2026-10-20T15:30:00"), at("2026-10-23T10:00:00"), ZONE);

        // 从 15:30 起算的话，每个桶会是 15:30..次日 15:30，"昨天"就含今天下午的数据。
        assertThat(edges).first().isEqualTo(at("2026-10-20T00:00:00"));
        assertThat(edges.get(edges.size() - 1)).isAfter(at("2026-10-23T10:00:00"));
    }

    @Test
    void hourlyBucketsAlignToWholeHours() {
        List<Instant> edges = Segmentation.boundaries(at("2026-10-20T10:37:00"), at("2026-10-20T13:05:00"), ZONE);

        assertThat(edges).first().isEqualTo(at("2026-10-20T10:00:00"));
        assertThat(Duration.between(edges.get(0), edges.get(1))).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void dailyBucketsCrossMonthAndYear() {
        List<Instant> edges = Segmentation.boundaries(at("2026-12-30T00:00:00"), at("2027-01-02T00:00:00"), ZONE);

        assertThat(edges).endsWith(at("2027-01-02T00:00:00"));
        assertThat(edges).hasSize(4); // 12-30、12-31、01-01、01-02（末边界）
    }

    @Test
    void emptyWindowHasNoBuckets() {
        assertThat(Segmentation.boundaries(at("2026-10-20T00:00:00"), at("2026-10-20T00:00:00"), ZONE))
                .hasSize(2);
        assertThat(Segmentation.fill(
                Segmentation.boundaries(at("2026-10-20T00:00:00"), at("2026-10-20T00:00:00"), ZONE),
                Map.of(), 0)).isEmpty();
    }

    @Test
    void fillKeepsMissingBucketsInsteadOfSkippingThem() {
        List<Instant> edges = Segmentation.boundaries(at("2026-10-20T00:00:00"), at("2026-10-23T00:00:00"), ZONE);
        Map<Instant, Integer> raw = new LinkedHashMap<>();
        raw.put(at("2026-10-20T00:00:00"), 5);
        raw.put(at("2026-10-22T00:00:00"), 7);
        // 10-21 没有任何数据：折线在这里必须是一个 0，不是断开

        List<Segmentation.Bucket<Integer>> buckets = Segmentation.fill(edges, raw, 0);

        assertThat(buckets).extracting(Segmentation.Bucket::value).containsExactly(5, 0, 7);
    }

    @Test
    void fillUsesNullForMissingPercentiles() {
        // 必须 > 48 小时才会进日桶；恰好 48 小时是小时桶（48 个），那样断言长度没有意义。
        List<Instant> edges = Segmentation.boundaries(at("2026-10-19T00:00:00"), at("2026-10-21T01:00:00"), ZONE);
        Map<Instant, Integer> raw = Map.of(at("2026-10-19T00:00:00"), 120);

        List<Segmentation.Bucket<Integer>> buckets = Segmentation.fill(edges, raw, null);

        // **0 毫秒是"很快"，null 是"没数据"**：写成 0 会让 p50 曲线凭空出现一个谷底
        // 日桶下至少 3 个桶（跨度 > 48 小时且按 00:00 对齐），后两个都必须是 null
        assertThat(buckets).extracting(Segmentation.Bucket::value).containsExactly(120, null, null);
    }

    @Test
    void fillIgnoresKeysOutsideTheBoundaries() {
        List<Instant> edges = Segmentation.boundaries(at("2026-10-19T00:00:00"), at("2026-10-21T01:00:00"), ZONE);
        Map<Instant, Integer> raw = Map.of(
                at("2026-10-19T00:00:00"), 3,
                at("2026-10-18T00:00:00"), 99); // 窗口外

        assertThat(Segmentation.fill(edges, raw, 0)).extracting(Segmentation.Bucket::value)
                .containsExactly(3, 0, 0);
    }
}
