package io.github.xiaou61.ai.internal.camp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * 诊断卷 vs 复测卷的逐主题对比（WORK-029）。
 *
 * <p><b>不出"提升百分比"</b>：3 题到 4 题不是"提升 33%"，那是在给噪声编单位。题量不同时
 * 干脆不给结论（{@link Verdict#INCONCLUSIVE}），只把两次数字并排放着。
 *
 * <p>纯函数。
 */
public final class CampComparer {

    public enum Verdict {
        IMPROVED,
        SAME,
        WORSE,
        INCONCLUSIVE
    }

    /**
     * 一个主题的对比。{@code verdict} 为 {@link Verdict#INCONCLUSIVE} 时 {@code reason} 说明原因。
     */
    public record Compared(String topic, TopicScore.Difficulty diagnostic, TopicScore.Difficulty retest,
                           Verdict verdict, String reason) {
    }

    public record Comparison(List<Compared> topics, List<String> onlyInDiagnostic,
                             List<String> onlyInRetest, List<String> mismatched) {
    }

    private CampComparer() {
    }

    public static Comparison compare(Map<String, TopicScore.TopicResult> diagnostic,
                                     Map<String, TopicScore.TopicResult> retest) {
        Map<String, TopicScore.TopicResult> left = diagnostic == null ? Map.of() : diagnostic;
        Map<String, TopicScore.TopicResult> right = retest == null ? Map.of() : retest;

        TreeSet<String> all = new TreeSet<>(left.keySet());
        all.addAll(right.keySet());

        List<Compared> compared = new ArrayList<>();
        List<String> onlyLeft = new ArrayList<>();
        List<String> onlyRight = new ArrayList<>();
        List<String> mismatched = new ArrayList<>();

        for (String topic : all) {
            TopicScore.TopicResult before = left.get(topic);
            TopicScore.TopicResult after = right.get(topic);
            if (before == null) {
                onlyRight.add(topic);
                continue;
            }
            if (after == null) {
                onlyLeft.add(topic);
                continue;
            }
            TopicScore.Difficulty a = new TopicScore.Difficulty(before.correct(), before.total());
            TopicScore.Difficulty b = new TopicScore.Difficulty(after.correct(), after.total());
            if (before.total() != after.total()) {
                mismatched.add(topic);
                compared.add(new Compared(topic, a, b, Verdict.INCONCLUSIVE,
                        "两次题量不同（" + before.total() + " 题 vs " + after.total() + " 题），对比仅供参考"));
                continue;
            }
            int sign = Integer.compare(after.correct(), before.correct());
            Verdict verdict = sign > 0 ? Verdict.IMPROVED : sign == 0 ? Verdict.SAME : Verdict.WORSE;
            compared.add(new Compared(topic, a, b, verdict, null));
        }
        return new Comparison(List.copyOf(compared), List.copyOf(onlyLeft),
                List.copyOf(onlyRight), List.copyOf(mismatched));
    }

    /** 整体结论：只要有主题题量不同就是 INCONCLUSIVE；否则按净进步/持平/退步。 */
    public static Verdict summary(Comparison comparison) {
        if (comparison == null || comparison.topics().isEmpty() || !comparison.mismatched().isEmpty()) {
            return Verdict.INCONCLUSIVE;
        }
        int score = 0;
        for (Compared topic : comparison.topics()) {
            score += switch (topic.verdict()) {
                case IMPROVED -> 1;
                case WORSE -> -1;
                default -> 0;
            };
        }
        return score > 0 ? Verdict.IMPROVED : score == 0 ? Verdict.SAME : Verdict.WORSE;
    }

    /** 供视图层拼装：主题 → 两次的计数（键序稳定）。 */
    static Map<String, int[]> counts(Comparison comparison) {
        Map<String, int[]> counts = new LinkedHashMap<>();
        for (Compared topic : comparison.topics()) {
            counts.put(topic.topic(), new int[]{
                    topic.diagnostic().correct(), topic.diagnostic().total(),
                    topic.retest().correct(), topic.retest().total()});
        }
        return counts;
    }
}
