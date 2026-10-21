package io.github.xiaou61.ai.internal.camp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 排期（WORK-029 TASK-006）。
 *
 * <p>四组必测：确定性、顺序无关、预算与空白日、溢出。外加首日最弱与前置关系——
 * 那是这一整套排序唯一的产品意义。
 */
class PlanBuilderTest {

    private static PlanBuilder.WeakTopic weak(String topic, Long conceptId, int correct, int total) {
        return new PlanBuilder.WeakTopic(topic, conceptId, (double) correct / total,
                1.0 / 3, correct, total, List.of());
    }

    private static PlanBuilder.Carrier carrier(long entryId, String title, int chars, boolean blocks) {
        return new PlanBuilder.Carrier(entryId, title, chars, blocks);
    }

    @Test
    @DisplayName("同输入两次调用，days 与 unscheduled 逐项相等")
    void planIsDeterministic() {
        List<PlanBuilder.WeakTopic> weak = List.of(weak("并发", 1L, 1, 4), weak("集合", 2L, 2, 4));
        Map<Long, List<PlanBuilder.Carrier>> carriers = Map.of(
                1L, List.of(carrier(11L, "并发入门", 2000, false)),
                2L, List.of(carrier(22L, "集合入门", 800, true)));

        assertThat(PlanBuilder.build(weak, carriers, List.of(), 3, 90))
                .isEqualTo(PlanBuilder.build(weak, carriers, List.of(), 3, 90));
    }

    @Test
    @DisplayName("弱项列表打乱后结果不变（内部先排序）")
    void planIsOrderIndependent() {
        List<PlanBuilder.WeakTopic> weak = new ArrayList<>(List.of(
                weak("并发", 1L, 1, 4), weak("集合", 2L, 2, 4), weak("事务", 3L, 0, 4)));
        Map<Long, List<PlanBuilder.Carrier>> carriers = Map.of(
                1L, List.of(carrier(11L, "并发入门", 2000, false)));

        PlanBuilder.Plan baseline = PlanBuilder.build(weak, carriers, List.of(), 3, 90);
        for (int round = 0; round < 5; round++) {
            List<PlanBuilder.WeakTopic> shuffled = new ArrayList<>(weak);
            Collections.shuffle(shuffled);
            assertThat(PlanBuilder.build(shuffled, carriers, List.of(), 3, 90)).isEqualTo(baseline);
        }
    }

    @Test
    @DisplayName("第一天第一项是最弱主题")
    void firstDayStartsWithWeakest() {
        // 词典序是"事务"在前，而最弱的是"索引"——两者不同，才能真的测到优先级排序
        PlanBuilder.Plan plan = PlanBuilder.build(List.of(
                        weak("事务", 1L, 2, 4),
                        weak("索引", 2L, 0, 4)),
                Map.of(1L, List.of(carrier(11L, "事务入门", 800, false)),
                        2L, List.of(carrier(22L, "索引入门", 800, false))),
                List.of(), 3, 90);

        assertThat(plan.days().get(0).tasks().get(0).topic()).isEqualTo("索引");
    }

    @Test
    @DisplayName("每天分钟和不超过预算")
    void budgetIsRespected() {
        List<PlanBuilder.WeakTopic> weak = new ArrayList<>();
        Map<Long, List<PlanBuilder.Carrier>> carriers = new java.util.HashMap<>();
        for (long index = 1; index <= 8; index++) {
            weak.add(weak("主题" + index, index, 1, 4));
            carriers.put(index, List.of(carrier(index * 10, "材料" + index, 8000, true)));
        }
        PlanBuilder.Plan threeDays = PlanBuilder.build(weak, carriers, List.of(), 3, 90);
        for (PlanBuilder.DayTasks day : threeDays.days()) {
            assertThat(day.tasks().stream().mapToInt(PlanBuilder.Task::minutes).sum())
                    .isLessThanOrEqualTo(90);
        }
        PlanBuilder.Plan sevenDays = PlanBuilder.build(weak, carriers, List.of(), 7, 60);
        for (PlanBuilder.DayTasks day : sevenDays.days()) {
            assertThat(day.tasks().stream().mapToInt(PlanBuilder.Task::minutes).sum())
                    .isLessThanOrEqualTo(60);
        }
    }

    @Test
    @DisplayName("弱项只有 1 个时，三天营每天都至少有一项")
    void noEmptyDay() {
        PlanBuilder.Plan plan = PlanBuilder.build(
                List.of(weak("并发", 1L, 1, 4)),
                Map.of(1L, List.of(carrier(11L, "并发入门", 800, false))),
                List.of(), 3, 90);

        assertThat(plan.days()).hasSize(3);
        assertThat(plan.days()).allSatisfy(day -> assertThat(day.tasks()).isNotEmpty());
        // 首日第一项仍是最弱的那个
        assertThat(plan.days().get(0).tasks().get(0).topic()).isEqualTo("并发");
    }

    @Test
    @DisplayName("弱项 20 个、三天营：排不下的进 unscheduled 且计数正确")
    void overflowGoesToUnscheduled() {
        List<PlanBuilder.WeakTopic> weak = new ArrayList<>();
        for (int index = 1; index <= 20; index++) {
            weak.add(weak("主题" + index, (long) index, 1, 4));
        }
        PlanBuilder.Plan plan = PlanBuilder.build(weak, Map.of(), List.of(), 3, 90);

        int scheduled = plan.days().stream().mapToInt(day -> day.tasks().size()).sum();
        assertThat(plan.unscheduled()).isNotEmpty();
        assertThat(scheduled + plan.unscheduled().size()).isEqualTo(20);
        // 每个缺口项 15 分钟、预算 90 → 每天最多 6 项，三天 18 项
        assertThat(scheduled).isLessThanOrEqualTo(18);
    }

    @Test
    @DisplayName("前置弱项排在后面那个之前；前置不弱则不产生它的任务")
    void prerequisitesOrderButDoNotInject() {
        PlanBuilder.WeakTopic later = new PlanBuilder.WeakTopic("事务", 9L, 0.25, 1.0, 1, 4, List.of(8L));
        PlanBuilder.WeakTopic prerequisite = new PlanBuilder.WeakTopic("索引", 8L, 0.5, 1.0, 2, 4,
                List.of());
        PlanBuilder.Plan plan = PlanBuilder.build(List.of(later, prerequisite), Map.of(), List.of(), 3, 90);

        List<String> order = plan.days().stream()
                .flatMap(day -> day.tasks().stream())
                .map(PlanBuilder.Task::topic)
                .toList();
        // 只校验前两项的顺序：第 2、3 天会用同一条缺口项补空天，尾部内容不属于这条断言
        assertThat(order.subList(0, 2)).containsExactly("索引", "事务");

        // 前置不在 weak 里 → 不产生它的任务
        PlanBuilder.Plan withoutPrerequisite = PlanBuilder.build(
                List.of(new PlanBuilder.WeakTopic("事务", 9L, 0.25, 1.0, 1, 4, List.of(8L))),
                Map.of(), List.of(), 3, 90);
        assertThat(withoutPrerequisite.days().stream()
                .flatMap(day -> day.tasks().stream())
                .map(PlanBuilder.Task::topic)
                .distinct()).containsExactly("事务");
    }

    @Test
    @DisplayName("没有载体条目的概念降级为 gap，entryId 为 null")
    void missingCarrierDegradesToGap() {
        PlanBuilder.Plan plan = PlanBuilder.build(
                List.of(weak("并发", 1L, 1, 4)), Map.of(), List.of(), 3, 90);

        PlanBuilder.Task task = plan.days().get(0).tasks().get(0);
        assertThat(task.kind()).isEqualTo(PlanBuilder.KIND_GAP);
        assertThat(task.entryId()).isNull();
        assertThat(task.minutes()).isEqualTo(ReadingMinutes.GAP);
        assertThat(task.why()).contains("没有可读内容");
    }

    @Test
    @DisplayName("含交互块的载体出 do 任务，时长比 read 多 10 分钟")
    void blocksBecomeDoTasks() {
        PlanBuilder.Plan plan = PlanBuilder.build(
                List.of(weak("并发", 1L, 1, 4)),
                Map.of(1L, List.of(carrier(11L, "并发实战", 800, true))),
                List.of(), 3, 90);

        PlanBuilder.Task task = plan.days().get(0).tasks().get(0);
        assertThat(task.kind()).isEqualTo(PlanBuilder.KIND_DO);
        assertThat(task.minutes()).isEqualTo(ReadingMinutes.of(800, true));
        assertThat(task.entryId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("到期复习追加在后面，最多 3 条且不与已有条目重复")
    void dueReviewsAreCapped() {
        PlanBuilder.Plan plan = PlanBuilder.build(
                List.of(weak("并发", 1L, 1, 4)),
                Map.of(1L, List.of(carrier(11L, "并发入门", 800, false))),
                List.of(new PlanBuilder.DueReview(2L, 11L, "并发入门"),
                        new PlanBuilder.DueReview(3L, 33L, "复习甲"),
                        new PlanBuilder.DueReview(4L, 44L, "复习乙"),
                        new PlanBuilder.DueReview(5L, 55L, "复习丙"),
                        new PlanBuilder.DueReview(6L, 66L, "复习丁")),
                3, 90);

        List<PlanBuilder.Task> tasks = plan.days().stream().flatMap(day -> day.tasks().stream()).toList();
        assertThat(tasks.stream().filter(task -> PlanBuilder.KIND_REVIEW.equals(task.kind())))
                .describedAs("11 已作为 read 出现，不重复；其余最多 3 条")
                .hasSize(3);
        assertThat(tasks.get(0).kind()).isNotEqualTo(PlanBuilder.KIND_REVIEW);
    }
}
