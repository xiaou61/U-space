package io.github.xiaou61.knowledge.internal.question;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 概念候选（WORK-033）：只产候选，不写库。
 *
 * <p>打分 = 中文 bigram 的 Dice 系数 + 结构加分（题目所在条目已有的概念、概念图一层邻居）。
 * 不用向量：平台没有 embedding 端点，且候选要能说出「命中了哪些词」，方便作者一眼判断。
 *
 * <p>bigram 对中文够用：不需要分词词典，也不会因为新术语没进词典而完全召不回。
 */
public final class QuestionConceptSuggester {

    /** 低于这个分数不展示——噪声候选比没有候选更花时间。 */
    static final double MIN_SCORE = 0.05;
    static final int MAX_HITS = 5;

    public record ConceptText(long id, String name, String summary) {
    }

    public record Suggestion(long conceptId, String name, double score, List<String> hits) {
    }

    private QuestionConceptSuggester() {
    }

    /**
     * @param entryConcepts 题目所在条目已关联的概念（继承加分）
     * @param neighbours    概念 → 一层邻居概念（图传播加分）
     */
    public static List<Suggestion> suggest(String stem, List<String> options, List<ConceptText> concepts,
                                    Set<Long> entryConcepts, Map<Long, Set<Long>> neighbours,
                                    int limit) {
        Set<String> questionGrams = grams(stem + " " + String.join(" ", options));
        if (questionGrams.isEmpty()) {
            return List.of();
        }
        List<Suggestion> result = new ArrayList<>();
        for (ConceptText concept : concepts) {
            Set<String> conceptGrams = grams(concept.name() + " " + (concept.summary() == null ? "" : concept.summary()));
            if (conceptGrams.isEmpty()) {
                continue;
            }
            Set<String> intersection = new HashSet<>(questionGrams);
            intersection.retainAll(conceptGrams);
            double dice = 2.0 * intersection.size() / (questionGrams.size() + conceptGrams.size());
            double bonus = 0;
            if (entryConcepts.contains(concept.id())) {
                bonus += 0.2;
            }
            if (neighbours.getOrDefault(concept.id(), Set.of()).stream().anyMatch(entryConcepts::contains)) {
                bonus += 0.1;
            }
            double score = dice + bonus;
            if (score < MIN_SCORE) {
                continue;
            }
            List<String> hits = intersection.stream().limit(MAX_HITS).toList();
            result.add(new Suggestion(concept.id(), concept.name(), score, hits));
        }
        result.sort(Comparator.comparingDouble(Suggestion::score).reversed()
                .thenComparingLong(Suggestion::conceptId));
        return result.size() <= limit ? result : result.subList(0, limit);
    }

    /** 中文按相邻两字切；英文/数字按词切。标点与空白丢掉，避免 bigram 里出现无用组合。 */
    static Set<String> grams(String text) {
        Set<String> grams = new LinkedHashSet<>();
        if (text == null) {
            return grams;
        }
        String normalized = text.toLowerCase().replaceAll("[\\p{Punct}\\s]+", " ");
        for (String token : normalized.split("\\s+")) {
            if (token.isBlank()) {
                continue;
            }
            if (token.codePoints().allMatch(code -> code < 128)) {
                if (token.length() >= 2) {
                    grams.add(token);
                }
                continue;
            }
            for (int index = 0; index + 1 < token.length(); index++) {
                grams.add(token.substring(index, index + 2));
            }
        }
        return grams;
    }
}
