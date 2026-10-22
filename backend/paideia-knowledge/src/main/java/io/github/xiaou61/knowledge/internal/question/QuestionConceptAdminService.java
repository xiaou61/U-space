package io.github.xiaou61.knowledge.internal.question;

import io.github.xiaou61.knowledge.internal.entry.Entry;
import io.github.xiaou61.knowledge.internal.entry.EntryMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理端的题目清单、概念标注与候选（WORK-033）。
 *
 * <p>候选**只读不写**：作者确认后才落库。AI/启发式只产出"建议"，这条边界是需求 D-04 的直接结果。
 */
@Service
public class QuestionConceptAdminService {

    private static final Set<String> SOURCES = Set.of("author", "ai", "inferred");

    private final QuestionAttemptMapper mapper;
    private final EntryMapper entryMapper;

    QuestionConceptAdminService(QuestionAttemptMapper mapper, EntryMapper entryMapper) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
    }

    public record QuestionView(String blockKey, String questionKey, String stem,
                               int optionCount, int answerIndex,
                               List<Tagged> concepts) {
    }

    public record Tagged(long conceptId, String name, double weight, String source) {
    }

    public record TagRequest(long conceptId, double weight) {
    }

    /** 某条目下的全部题目与已确认的概念。 */
    public List<QuestionView> list(long entryId) {
        Entry entry = requireEntry(entryId);
        Map<Long, String> names = conceptNames();
        List<QuestionView> result = new ArrayList<>();
        for (QuestionCatalog.Question question : QuestionCatalog.parse(entry.getId(), entry.getBody())) {
            List<Tagged> tagged = mapper.findLinks(entryId, question.blockKey(), question.questionKey())
                    .stream()
                    .map(link -> new Tagged(link.conceptId(),
                            names.getOrDefault(link.conceptId(), ""), link.weight(), "author"))
                    .toList();
            result.add(new QuestionView(question.blockKey(), question.questionKey(), question.stem(),
                    question.options().size(), question.answerIndex(), tagged));
        }
        return result;
    }

    /**
     * 覆盖一道题的概念标注。
     *
     * <p>先删后插而不是合并：作者的意图是"这道题就该是这几个概念"，合并会让误标清不掉。
     */
    @Transactional
    public void replace(long entryId, String blockKey, String questionKey, List<TagRequest> concepts,
                        String source) {
        Entry entry = requireEntry(entryId);
        QuestionCatalog.find(entry.getId(), entry.getBody(), blockKey, questionKey)
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_ARGUMENT, "找不到这道题"));
        String normalized = SOURCES.contains(source) ? source : "author";
        if (concepts == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少 concepts");
        }
        mapper.deleteLinks(entryId, blockKey, questionKey);
        Instant now = Instant.now();
        for (TagRequest concept : concepts) {
            if (concept.conceptId() <= 0) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "概念 id 非法");
            }
            double weight = Math.max(0.1, Math.min(1.0, concept.weight()));
            mapper.insertLink(entryId, blockKey, questionKey, concept.conceptId(),
                    weight, normalized, null, now);
        }
    }

    /** 候选：词面 + 结构加分，最多 5 条，不写库。 */
    public List<QuestionConceptSuggester.Suggestion> suggestions(long entryId, String blockKey,
                                                                 String questionKey, int limit) {
        Entry entry = requireEntry(entryId);
        QuestionCatalog.Question question = QuestionCatalog
                .find(entry.getId(), entry.getBody(), blockKey, questionKey)
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_ARGUMENT, "找不到这道题"));
        List<QuestionConceptSuggester.ConceptText> concepts = mapper.findAllConcepts().stream()
                .map(row -> new QuestionConceptSuggester.ConceptText(row.id(), row.name(), row.summary()))
                .toList();
        Set<Long> entryConcepts = new HashSet<>();
        for (QuestionAttemptMapper.EntryConceptRow row : mapper.findEntryConcepts(entryId)) {
            entryConcepts.add(row.conceptId());
        }
        Map<Long, Set<Long>> neighbours = new HashMap<>();
        for (QuestionAttemptMapper.EdgeRow edge : mapper.findEdges()) {
            neighbours.computeIfAbsent(edge.toConceptId(), key -> new HashSet<>())
                    .add(edge.fromConceptId());
            neighbours.computeIfAbsent(edge.fromConceptId(), key -> new HashSet<>())
                    .add(edge.toConceptId());
        }
        int size = Math.max(1, Math.min(10, limit));
        return QuestionConceptSuggester.suggest(question.stem(), question.options(), concepts,
                entryConcepts, neighbours, size);
    }

    private Map<Long, String> conceptNames() {
        Map<Long, String> names = new HashMap<>();
        for (QuestionAttemptMapper.ConceptRow row : mapper.findAllConcepts()) {
            names.put(row.id(), row.name());
        }
        return names;
    }

    private Entry requireEntry(long entryId) {
        return entryMapper.findPublishedById(entryId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "条目不存在"));
    }
}
