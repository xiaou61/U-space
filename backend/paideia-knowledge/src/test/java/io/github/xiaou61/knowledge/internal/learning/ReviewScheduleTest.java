package io.github.xiaou61.knowledge.internal.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 复习交卷后的排期规则（WORK-034 S2）：答错不变、明天；全对前进一档。 */
class ReviewScheduleTest {

    private final Instant now = Instant.parse("2026-10-23T00:00:00Z");

    @Test
    @DisplayName("答错：档位不变，明天再来")
    void failureKeepsStageAndDueTomorrow() {
        var outcome = LearningProgressRules.afterReview(2, false, now);

        assertThat(outcome.stage()).isEqualTo(2);
        assertThat(outcome.dueAt()).isEqualTo(now.plus(1, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("全对：前进一档，按新档位的间隔排")
    void successAdvancesStage() {
        var outcome = LearningProgressRules.afterReview(1, true, now);

        assertThat(outcome.stage()).isEqualTo(2);
        assertThat(outcome.dueAt()).isEqualTo(now.plus(LearningProgressRules.intervalDays(2), ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("到顶后停在最后一档，不越界")
    void stopsAtLastStage() {
        var outcome = LearningProgressRules.afterReview(99, true, now);

        assertThat(outcome.stage()).isEqualTo(LearningProgressRules.stageCount() - 1);
    }
}