package io.github.xiaou61.knowledge.internal.question;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 前置缺口诊断（WORK-033）。
 *
 * <p>答错一个概念时，不要只推它自己——用户的缺口往往在更靠前的那一层。这里沿
 * **前置方向**（`to == 当前`）反向走，找「自身没掌握、但它的直接前置都已掌握」的节点：
 * 那才是能立刻补上的那一关，再往前的都还没轮到。
 *
 * <p>用带衰减的 BFS 而不是 PageRank：概念图当前只有十几条边、深度不超过三层，
 * BFS 的结果能附完整路径，解释性比一个分数好。
 */
public final class PrerequisiteDiagnosis {

    /** 视为「已掌握」的阈值，与 MasteryModel 的 familiar 下限一致。 */
    static final double MASTERED = 0.70;
    static final int DEFAULT_MAX_DEPTH = 3;
    /** 每深一层，建议权重减半：直接前置永远排在更远的前置之前。 */
    static final double DECAY = 0.5;
    static final int MAX_SUGGESTIONS = 3;

    /** 一条边：`from` 是前置，`to` 是后继。与 `knowledge_concept_edges` 的方向一致。 */
    public record Edge(long from, long to) {
    }

    public record Suggestion(long conceptId, double weight, List<Long> path) {
    }

    private PrerequisiteDiagnosis() {
    }

    /**
     * 给出补课建议。
     *
     * @param failedConceptId 答错的概念
     * @param mastery         用户对各概念的掌握概率；缺失视为 0（没学过）
     * @param edges           全部前置边
     */
    public static List<Suggestion> suggest(long failedConceptId, Map<Long, Double> mastery,
                                    List<Edge> edges, int maxDepth) {
        Map<Long, List<Long>> parents = new HashMap<>();
        for (Edge edge : edges) {
            parents.computeIfAbsent(edge.to(), key -> new ArrayList<>()).add(edge.from());
        }

        Set<Long> visited = new HashSet<>();
        visited.add(failedConceptId);
        ArrayDeque<Candidate> queue = new ArrayDeque<>();
        queue.add(new Candidate(failedConceptId, 1.0, List.of(failedConceptId)));

        List<Suggestion> result = new ArrayList<>();
        while (!queue.isEmpty() && result.size() < MAX_SUGGESTIONS) {
            Candidate current = queue.poll();
            if (current.path().size() > maxDepth + 1) {
                continue;
            }
            List<Long> directParents = parents.getOrDefault(current.conceptId(), List.of());
            boolean frontier = !directParents.isEmpty()
                    && directParents.stream().allMatch(parent -> isMastered(parent, mastery));
            if (frontier && !isMastered(current.conceptId(), mastery)) {
                result.add(new Suggestion(current.conceptId(), current.weight(), current.path()));
                // 前沿节点不再往前扩：它的前置都已掌握，更远的不是当前该补的
                continue;
            }
            for (Long parent : directParents) {
                if (!visited.add(parent)) {
                    continue;
                }
                List<Long> path = new ArrayList<>(current.path());
                path.add(0, parent);
                queue.add(new Candidate(parent, current.weight() * DECAY, List.copyOf(path)));
            }
        }
        return result;
    }

    private static boolean isMastered(long conceptId, Map<Long, Double> mastery) {
        return mastery.getOrDefault(conceptId, 0.0) >= MASTERED;
    }

    private record Candidate(long conceptId, double weight, List<Long> path) {
    }
}
