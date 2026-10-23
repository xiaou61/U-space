package io.github.xiaou61.knowledge.internal.learning;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 到期复习列表（WORK-034 REQ-002）。
 *
 * <p>只读：不动排期、不调 AI。空列表是正常结果——页面显示两个去处，不是错误。
 */
@Service
public class LearningReviewService {

    private final LearningReviewMapper mapper;

    LearningReviewService(LearningReviewMapper mapper) {
        this.mapper = mapper;
    }

    public record ReviewDueView(long entryId, String title, Long conceptId, String conceptName,
                                Instant reviewDueAt, int stage) {
    }

    public record DueList(List<ReviewDueView> items, int total) {
    }

    public DueList due(long userId, Instant now) {
        List<LearningReviewMapper.DueRow> rows = mapper.findDue(userId, now);
        if (rows.isEmpty()) {
            return new DueList(List.of(), 0);
        }
        Map<Long, LearningReviewMapper.ConceptRow> firstConcept = new HashMap<>();
        List<Long> entryIds = rows.stream().map(LearningReviewMapper.DueRow::entryId).toList();
        for (LearningReviewMapper.ConceptRow row : mapper.findConceptNames(entryIds)) {
            firstConcept.putIfAbsent(row.entryId(), row);
        }
        List<ReviewDueView> items = new ArrayList<>(rows.size());
        for (LearningReviewMapper.DueRow row : rows) {
            LearningReviewMapper.ConceptRow concept = firstConcept.get(row.entryId());
            items.add(new ReviewDueView(row.entryId(), row.title(),
                    concept == null ? null : concept.conceptId(),
                    concept == null ? null : concept.conceptName(),
                    row.reviewDueAt(), row.reviewStage()));
        }
        return new DueList(items, items.size());
    }
}