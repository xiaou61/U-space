package io.github.xiaou61.ai.internal.camp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 按主题 × 难度聚合作答（WORK-029）。
 *
 * <p><b>不出总分</b>：跨主题求平均会把"强项 + 弱项"糊成一个看不出方向的数字。用户要知道的
 * 是"哪块弱"，而不是"我考了 67 分"。
 *
 * <p>题量为 0 的主题不出现：它没有作答，不是"考得差"。
 *
 * <p>纯函数。
 */
public final class TopicScore {

    /** 一题的一次作答。`difficulty` 必须已归一（easy/medium/hard）。 */
    public record ScoredItem(String topic, String difficulty, boolean correct) {
    }

    public record Difficulty(int correct, int total) {
    }

    public record TopicResult(String topic, int correct, int total, Map<String, Difficulty> byDifficulty) {

        /** 答对率；题量为 0 时返回 0（调用方不应使用这种结果）。 */
        public double ratio() {
            return total == 0 ? 0 : (double) correct / total;
        }

        public Difficulty of(String tier) {
            return byDifficulty.getOrDefault(tier, new Difficulty(0, 0));
        }
    }

    private TopicScore() {
    }

    /**
     * @return 主题 → 聚合结果，按主题字典序（与调用方传入顺序无关，保证可复现）
     */
    public static Map<String, TopicResult> aggregate(List<ScoredItem> items) {
        Map<String, TopicResult> result = new TreeMap<>();
        if (items == null || items.isEmpty()) {
            return result;
        }
        Map<String, Map<String, int[]>> counters = new TreeMap<>();
        for (ScoredItem item : items) {
            if (item == null || item.topic() == null || item.topic().isBlank()) {
                continue;
            }
            Map<String, int[]> tiers = counters.computeIfAbsent(item.topic(), key -> new TreeMap<>());
            int[] counter = tiers.computeIfAbsent(item.difficulty() == null ? "medium" : item.difficulty(),
                    key -> new int[2]);
            counter[1]++;
            if (item.correct()) {
                counter[0]++;
            }
        }
        for (Map.Entry<String, Map<String, int[]>> entry : counters.entrySet()) {
            int correct = 0;
            int total = 0;
            Map<String, Difficulty> byDifficulty = new LinkedHashMap<>();
            for (Map.Entry<String, int[]> tier : entry.getValue().entrySet()) {
                int[] counter = tier.getValue();
                byDifficulty.put(tier.getKey(), new Difficulty(counter[0], counter[1]));
                correct += counter[0];
                total += counter[1];
            }
            if (total == 0) {
                continue;
            }
            result.put(entry.getKey(), new TopicResult(entry.getKey(), correct, total,
                    Map.copyOf(byDifficulty)));
        }
        return result;
    }

    /** 只保留通过率的排序口径（供弱项排序复用，避免两处各写一遍降序）。 */
    static List<TopicResult> ascendingByRatio(Map<String, TopicResult> topics) {
        List<TopicResult> values = new ArrayList<>(topics.values());
        values.sort(Comparator.comparingDouble(TopicResult::ratio)
                .thenComparing(TopicResult::topic));
        return values;
    }
}
