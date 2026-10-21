package io.github.xiaou61.ai.internal.camp;

import io.github.xiaou61.knowledge.MasteryEvidence;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 训练营成绩作为掌握度的第四路证据（WORK-029 REQ-033）。
 *
 * <p>方向：ai → knowledge 的端口由**这边实现**。知识库不认识训练营的表，
 * 只认识 {@link MasteryEvidence} 这个方法。
 *
 * <p>取"最近一次交卷"的逐概念答对率：复测比诊断晚就以复测为准。一个概念在一份卷子里通常
 * 对应多道题，按**题量加权**（正确数 / 总题数），而不是各题比例的平均——
 * 后者会让"1 题答对"与"10 题答对 9"权重相同。
 */
@Component
class CampMasteryEvidence implements MasteryEvidence {

    private final CampMapper camps;

    CampMasteryEvidence(CampMapper camps) {
        this.camps = camps;
    }

    @Override
    public Map<Long, Double> campRatios(long userId) {
        if (userId <= 0) {
            return Map.of();
        }
        Map<Long, CampMapper.ConceptTally> latest = new LinkedHashMap<>();
        for (CampMapper.ConceptTally tally : camps.findConceptTallies(userId)) {
            CampMapper.ConceptTally current = latest.get(tally.conceptId());
            if (current == null || laterThan(tally.submittedAt(), current.submittedAt())) {
                latest.put(tally.conceptId(), tally);
            }
        }
        Map<Long, Double> ratios = new LinkedHashMap<>();
        latest.forEach((conceptId, tally) -> {
            if (tally.total() > 0) {
                ratios.put(conceptId, (double) tally.correct() / tally.total());
            }
        });
        return ratios;
    }

    private static boolean laterThan(Instant candidate, Instant current) {
        if (candidate == null) {
            return false;
        }
        return current == null || candidate.isAfter(current);
    }
}
