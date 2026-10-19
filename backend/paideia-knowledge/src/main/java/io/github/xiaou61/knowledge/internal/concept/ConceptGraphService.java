package io.github.xiaou61.knowledge.internal.concept;

import io.github.xiaou61.knowledge.internal.assessment.SelfAssessmentService;
import io.github.xiaou61.knowledge.internal.category.Category;
import io.github.xiaou61.knowledge.internal.category.CategoryMapper;
import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import io.github.xiaou61.knowledge.internal.entry.EntryViewMapper;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressRules.BlockScore;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 图谱聚合（WORK-021）。
 *
 * <p>把四份互不相干的表拼成一张图：概念与关系是公共内容，分数／自评／浏览是本人证据。
 * <b>掌握度不落库</b>——这里每次现算，输入只有几百行，毫秒级。
 *
 * <p>三条约束：
 * <ul>
 *   <li>节点上限 {@link #MAX_NODES}：超出按"有证据优先"截断，绝不无界返回。</li>
 *   <li>概念即使没有任何载体条目也要出现——它是管理员建的，不该因为没挂内容就消失。</li>
 *   <li>证据只可能是本人的：三路来源都按 {@code userId} 过滤。</li>
 * </ul>
 */
@Service
public class ConceptGraphService {

    /** 一次返回的节点上限。超过就截断并标 {@code truncated}。 */
    static final int MAX_NODES = 500;
    /** 每个节点最多带几条载体条目——图上点开只需要几个入口。 */
    static final int MAX_CARRIERS = 8;

    private static final Logger log = LoggerFactory.getLogger(ConceptGraphService.class);

    public record Graph(List<Node> concepts, List<Edge> edges, Stats stats) {
    }

    public record Node(long id, String name, String summary, Long categoryId, String categoryName,
                       double score, String tier, boolean reviewDue, Evidence evidence,
                       List<Carrier> entries) {
    }

    public record Evidence(int viewed, String assessed, int scoredEntries) {
    }

    public record Carrier(long entryId, String title, Double scoreRatio) {
    }

    public record Edge(long from, long to, String type) {
    }

    public record Stats(int concepts, int touched, int mastered, int dueForReview, boolean truncated) {
    }

    private final ConceptMapper conceptMapper;
    private final ConceptLinkMapper linkMapper;
    private final EntryMapper entryMapper;
    private final EntryViewMapper viewMapper;
    private final CategoryMapper categoryMapper;
    private final LearningProgressService progressService;
    private final SelfAssessmentService assessmentService;

    ConceptGraphService(ConceptMapper conceptMapper,
                        ConceptLinkMapper linkMapper,
                        EntryMapper entryMapper,
                        EntryViewMapper viewMapper,
                        CategoryMapper categoryMapper,
                        LearningProgressService progressService,
                        SelfAssessmentService assessmentService) {
        this.conceptMapper = conceptMapper;
        this.linkMapper = linkMapper;
        this.entryMapper = entryMapper;
        this.viewMapper = viewMapper;
        this.categoryMapper = categoryMapper;
        this.progressService = progressService;
        this.assessmentService = assessmentService;
    }

    public Graph build(long userId) {
        List<Concept> concepts = conceptMapper.findAll();
        if (concepts.isEmpty()) {
            return new Graph(List.of(), List.of(), new Stats(0, 0, 0, 0, false));
        }

        // 三路本人证据，各自按 entryId 索引
        Map<Long, LearningProgressService.View> progress = new LinkedHashMap<>();
        for (LearningProgressService.View view : progressService.listMine(userId, 200)) {
            progress.put(view.entryId(), view);
        }
        Map<Long, String> assessments = new HashMap<>();
        for (SelfAssessmentService.AssessedItem item : assessmentService.listMine(userId, null)) {
            assessments.put(item.entryId(), item.level());
        }
        Map<Long, EntryViewMapper.Row> views = new HashMap<>();
        for (EntryViewMapper.Row row : viewMapper.findByUser(userId)) {
            views.put(row.entryId(), row);
        }

        // 概念 → 载体条目
        Map<Long, Set<Long>> entriesByConcept = new HashMap<>();
        for (ConceptLinks.EntryLink link : linkMapper.findAllEntryLinks()) {
            entriesByConcept.computeIfAbsent(link.conceptId(), key -> new LinkedHashSet<>()).add(link.entryId());
        }

        // 只取已发布条目的标题：要显示的载体必须是能点进去的
        Set<Long> wantedEntryIds = new LinkedHashSet<>();
        entriesByConcept.values().forEach(wantedEntryIds::addAll);
        Map<Long, String> titles = wantedEntryIds.isEmpty()
                ? Map.of()
                : entryMapper.findPublishedTitlesByIds(List.copyOf(wantedEntryIds)).stream()
                        .collect(Collectors.toMap(Entry::getId, Entry::getTitle));

        Map<Long, String> categoryNames = categoryMapper.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        Instant now = Instant.now();
        List<Node> nodes = new ArrayList<>(concepts.size());
        for (Concept concept : concepts) {
            nodes.add(toNode(concept, entriesByConcept.getOrDefault(concept.getId(), Set.of()),
                    titles, categoryNames, progress, assessments, views, now));
        }

        boolean truncated = nodes.size() > MAX_NODES;
        if (truncated) {
            nodes = selectNodes(nodes, MAX_NODES);
            log.warn("concept.graph.truncated total={} kept={}", concepts.size(), MAX_NODES);
        }

        Set<Long> kept = nodes.stream().map(Node::id).collect(Collectors.toSet());
        List<Edge> edges = linkMapper.findAllEdges().stream()
                .filter(edge -> kept.contains(edge.fromConceptId()) && kept.contains(edge.toConceptId()))
                .map(edge -> new Edge(edge.fromConceptId(), edge.toConceptId(), edge.relationType()))
                .toList();

        int touched = (int) nodes.stream().filter(node -> isTouched(node.evidence())).count();
        int mastered = (int) nodes.stream()
                .filter(node -> ConceptMastery.TIER_MASTERED.equals(node.tier())).count();
        int due = (int) nodes.stream().filter(Node::reviewDue).count();

        log.info("concept.graph.built userId={} nodes={} edges={} truncated={}",
                userId, nodes.size(), edges.size(), truncated);
        return new Graph(List.copyOf(nodes), edges,
                new Stats(nodes.size(), touched, mastered, due, truncated));
    }

    /**
     * 超上限时保留哪些节点：**有证据的优先**，其次分数高的，最后按 id 稳定排序。
     *
     * <p>为什么不是"按 id 截断"：那样用户会看到一堆自己从没碰过的概念，而把自己真正学过的
     * 挤出视野——截断就变成了"随机丢掉你有用的数据"。
     */
    static List<Node> selectNodes(List<Node> nodes, int max) {
        if (nodes.size() <= max) {
            return nodes;
        }
        List<Node> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator
                .comparingInt((Node node) -> isTouched(node.evidence()) ? 0 : 1)
                .thenComparing(Comparator.comparingDouble(Node::score).reversed())
                .thenComparingLong(Node::id));
        return new ArrayList<>(sorted.subList(0, max));
    }

    private static Node toNode(Concept concept, Set<Long> entryIds, Map<Long, String> titles,
                               Map<Long, String> categoryNames,
                               Map<Long, LearningProgressService.View> progress,
                               Map<Long, String> assessments,
                               Map<Long, EntryViewMapper.Row> views, Instant now) {
        List<Carrier> carriers = new ArrayList<>();
        List<ConceptMastery.Evidence> evidences = new ArrayList<>();
        boolean reviewDue = false;
        int scored = 0;
        int viewed = 0;
        String assessed = null;

        for (Long entryId : entryIds) {
            // 未发布条目不计入证据：它们对学习者不存在
            String title = titles.get(entryId);
            if (title == null) {
                continue;
            }
            LearningProgressService.View view = progress.get(entryId);
            Double ratio = view == null ? null : ratioOf(view);
            if (ratio != null) {
                scored++;
            }
            boolean seen = views.containsKey(entryId);
            if (seen) {
                viewed += views.get(entryId).viewCount();
            }
            String level = assessments.get(entryId);
            if (level != null) {
                assessed = level;
            }
            if (view != null && view.reviewDueAt() != null && !view.reviewDueAt().isAfter(now)) {
                reviewDue = true;
            }
            evidences.add(new ConceptMastery.Evidence(ratio, level, seen, latestOf(view, views.get(entryId))));
            if (carriers.size() < MAX_CARRIERS) {
                carriers.add(new Carrier(entryId, title, ratio));
            }
        }

        ConceptMastery.Result result = ConceptMastery.compute(evidences, reviewDue, now);
        return new Node(concept.getId(), concept.getName(), concept.getSummary(),
                concept.getCategoryId(), concept.getCategoryId() == null ? null : categoryNames.get(concept.getCategoryId()),
                result.score(), result.tier(), result.reviewDue(),
                new Evidence(viewed, assessed, scored), List.copyOf(carriers));
    }

    /** 一个条目的得分率：挑战与小测都算，取平均；没有记录返回 null。 */
    private static Double ratioOf(LearningProgressService.View view) {
        double sum = 0;
        int count = 0;
        for (BlockScore score : allScores(view)) {
            if (score.total() > 0) {
                sum += (double) score.score() / score.total();
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }

    private static List<BlockScore> allScores(LearningProgressService.View view) {
        List<BlockScore> all = new ArrayList<>(view.challenges().values());
        all.addAll(view.quizzes().values());
        return all;
    }

    /** 取学习记录更新时间与浏览时间里较晚的一个，用于衰减。 */
    private static Instant latestOf(LearningProgressService.View view, EntryViewMapper.Row row) {
        Instant fromView = row == null ? null : row.lastViewedAt();
        Instant fromProgress = view == null ? null : view.passedAt();
        if (fromView == null) {
            return fromProgress;
        }
        if (fromProgress == null) {
            return fromView;
        }
        return fromView.isAfter(fromProgress) ? fromView : fromProgress;
    }

    private static boolean isTouched(Evidence evidence) {
        return evidence.viewed() > 0 || evidence.assessed() != null || evidence.scoredEntries() > 0;
    }

    /** 供工具复用：只要分数最低与待复习的那些。 */
    public List<Node> weakest(long userId, int limit) {
        Graph graph = build(userId);
        List<Node> touched = graph.concepts().stream()
                .filter(node -> isTouched(node.evidence()))
                .sorted(Comparator.comparingDouble(Node::score).thenComparingLong(Node::id))
                .limit(Math.max(1, limit))
                .collect(Collectors.toCollection(ArrayList::new));
        graph.concepts().stream().filter(Node::reviewDue).forEach(node -> {
            if (touched.stream().noneMatch(existing -> existing.id() == node.id())) {
                touched.add(node);
            }
        });
        return List.copyOf(touched);
    }

}
