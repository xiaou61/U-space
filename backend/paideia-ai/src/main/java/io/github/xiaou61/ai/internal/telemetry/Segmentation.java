package io.github.xiaou61.ai.internal.telemetry;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 时间分桶（WORK-036）。
 *
 * <p>纯函数，不碰数据库也不认识任何记录类型——它只回答"这段时间该切成几个桶、边界在哪"。
 * 分两步是刻意的：边界一次算准，补零再按边界铺，任何一步错了另一条用例不会替它遮掩。
 *
 * <p><b>边界按 zone 的自然日/整点对齐</b>，不是"从 from 起每 24 小时切一段"：不对齐的话，
 * 用户 15:30 打开页面，每个"日桶"实际是 15:30 到次日 15:30，"昨天"会包含今天下午的数据。
 *
 * <p>首尾桶通常是**不完整**的（第一个从 00:00 起、但查询只取 from 之后的数据）。
 * 这是刻意的：宁可显示一个"今天到目前为止"的半桶，也不要把 from 到下一个整点之间的
 * 数据整个丢掉——那恰好是用户最关心的最近一段。
 *
 * <p>粒度只有两档（≤48 小时按小时，否则按天）：它服务的是"看一眼趋势"，
 * 不是分钟级排障——那种场景有明细列表。
 */
final class Segmentation {

    enum Granularity {
        HOUR,
        DAY
    }

    /** 跨度上限，超过它按天分桶。 */
    private static final Duration HOURLY_LIMIT = Duration.ofHours(48);

    /** 一个桶：起点（含）与它的值。用 record 而不是 Map.Entry——值允许为 null。 */
    record Bucket<T>(Instant at, T value) {
    }

    private Segmentation() {
    }

    static Granularity of(Instant from, Instant to) {
        return Duration.between(from, to).compareTo(HOURLY_LIMIT) <= 0
                ? Granularity.HOUR
                : Granularity.DAY;
    }

    /**
     * 桶边界，长度 = 桶数 + 1：第 i 个桶是 {@code [boundaries[i], boundaries[i+1])}。
     *
     * <p>窗口为空或反向时返回两个相同边界（桶数为 0）——调用方据此返回空序列，
     * 而不是造一个宽度为负的假桶。
     */
    static List<Instant> boundaries(Instant from, Instant to, ZoneId zone) {
        if (!to.isAfter(from)) {
            return List.of(from, from);
        }
        if (of(from, to) == Granularity.HOUR) {
            Instant start = from.atZone(zone).truncatedTo(ChronoUnit.HOURS).toInstant();
            return edges(start, to, ChronoUnit.HOURS, zone);
        }
        Instant start = from.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
        return edges(start, to, ChronoUnit.DAYS, zone);
    }

    private static List<Instant> edges(Instant start, Instant to, ChronoUnit unit, ZoneId zone) {
        List<Instant> edges = new ArrayList<>();
        Instant cursor = start;
        while (true) {
            edges.add(cursor);
            if (!cursor.isBefore(to)) {
                return edges;
            }
            // 按天前进必须走 zone 的自然日：直接 plus(1, DAYS) 在跨 DST 的地区会偏一小时
            cursor = unit == ChronoUnit.DAYS
                    ? cursor.atZone(zone).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant()
                    : cursor.plus(1, ChronoUnit.HOURS);
        }
    }

    /**
     * 按边界铺满：每个边界起点产出一个桶，缺的用 {@code zero} 填。
     *
     * @param raw  已按桶起点索引的原始值；键不在边界上的被忽略
     * @param zero 缺失桶的值。**分位数传 null**——"没有数据"与"0 毫秒"不是一回事
     */
    static <T> List<Bucket<T>> fill(List<Instant> boundaries, Map<Instant, T> raw, T zero) {
        // 退化窗口（from == to）的两个边界相同：不拦的话会产出一个"零宽度桶"，
        // 界面上显示成"有一小时的数据是 0"，而真相是这段时间根本没人查。
        if (boundaries.size() < 2
                || !boundaries.get(boundaries.size() - 1).isAfter(boundaries.get(0))) {
            return List.of();
        }
        List<Bucket<T>> filled = new ArrayList<>(boundaries.size() - 1);
        for (int i = 0; i < boundaries.size() - 1; i++) {
            Instant at = boundaries.get(i);
            filled.add(new Bucket<>(at, raw.getOrDefault(at, zero)));
        }
        return filled;
    }
}
