package io.github.xiaou61.ai.internal.camp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 弱项 → 每日任务（WORK-029）。
 *
 * <p><b>这里一行模型调用都没有，是刻意的</b>：让模型写计划，它会编造不存在的条目，而且同一输入
 * 两次会给出两份不同的计划——既不可复现，也没法测试。计划项一律锚定 {@code knowledge_entries}
 * 的真实 id，同样输入必得同样计划。
 *
 * <p>五步：排序 → 前置修正 → 展开候选 → 装箱 → 溢出。
 *
 * <p><b>第 1 天第一项必然是最弱主题</b>：这是"按优先级降序"与"顺序装箱"的直接结果，
 * 不需要特判。
 *
 * <p>纯函数。
 */
public final class PlanBuilder {

    /** 一个概念最多展开几条载体条目。两个入口够了，再多就是"今天只学这一块"。 */
    static final int MAX_CARRIERS_PER_CONCEPT = 2;
    /** 复习任务上限：复习是补充，不该占满一天。 */
    static final int MAX_REVIEWS = 3;

    public static final String KIND_READ = "read";
    public static final String KIND_DO = "do";
    public static final String KIND_GAP = "gap";
    public static final String KIND_REVIEW = "review";

    /**
     * 一个弱项。
     *
     * @param prerequisiteConceptIds 本概念的前置概念 id（可能为空）。只有当前置**也弱**时才影响顺序；
     *                               不弱的前置不产生任务——他已经会了，别浪费第一天
     */
    public record WeakTopic(String topic, Long conceptId, double ratio, double coverage,
                            int correct, int total, List<Long> prerequisiteConceptIds) {
    }

    /** 一个可读的载体条目（只含已发布的）。 */
    public record Carrier(long entryId, String title, int bodyChars, boolean hasBlocks) {
    }

    /** 到期该复习的一条。 */
    public record DueReview(Long conceptId, long entryId, String title) {
    }

    /** 计划里的一项。{@code entryId} 为空表示缺口项（没有可读内容）。 */
    public record Task(int day, int seq, String kind, String topic, String title, int minutes,
                       String why, Long entryId, Long conceptId) {
    }

    public record DayTasks(int day, List<Task> tasks) {
    }

    public record Plan(List<DayTasks> days, List<Task> unscheduled) {
    }

    private PlanBuilder() {
    }

    /**
     * @param days          3 或 7
     * @param budgetMinutes 每天的时间预算
     */
    public static Plan build(List<WeakTopic> weak, Map<Long, List<Carrier>> carriers,
                             List<DueReview> due, int days, int budgetMinutes) {
        int dayCount = Math.max(1, days);
        int budget = Math.max(1, budgetMinutes);
        List<Candidate> candidates = candidates(sorted(weak), carriers, due);
        List<List<Candidate>> bins = pack(candidates, dayCount, budget);
        balance(bins, candidates);
        return toPlan(bins, candidates, dayCount);
    }

    // ── ① + ② 排序与前置修正 ────────────────────────────────────────

    /**
     * 排序：{@code priority = (1 - ratio) * coverage} 降序，相等按 topic 字典序。
     *
     * <p>覆盖度参与排序的理由：一个只出现在技能列表里的词（可能是抄的）与一个同时出现在项目、
     * 经历里的词，同样答错时后者更值得先补。
     */
    static List<WeakTopic> sorted(List<WeakTopic> weak) {
        List<WeakTopic> ordered = new ArrayList<>();
        if (weak == null) {
            return ordered;
        }
        for (WeakTopic topic : weak) {
            if (topic != null && topic.topic() != null && !topic.topic().isBlank()) {
                ordered.add(topic);
            }
        }
        ordered.sort(Comparator
                .comparingDouble((WeakTopic topic) -> priority(topic)).reversed()
                .thenComparing(WeakTopic::topic));
        return applyPrerequisites(ordered);
    }

    private static double priority(WeakTopic topic) {
        double gap = 1 - Math.max(0, Math.min(1, topic.ratio()));
        double coverage = Math.max(0, Math.min(1, topic.coverage()));
        return gap * coverage;
    }

    /**
     * 前置修正：若 A 是 B 的前置且 A 也弱，把 A 提到 B 之前。
     *
     * <p>最多两轮：现实中前置链通常只有一两层，两轮足以让直接与间接前置都到位；
     * 真出现更长的链，剩下的差距也只是顺序问题，不值得为它写拓扑排序。
     */
    private static List<WeakTopic> applyPrerequisites(List<WeakTopic> ordered) {
        List<WeakTopic> result = new ArrayList<>(ordered);
        for (int round = 0; round < 2; round++) {
            boolean moved = false;
            for (int index = 0; index < result.size(); index++) {
                WeakTopic topic = result.get(index);
                for (Long prerequisite : topic.prerequisiteConceptIds() == null
                        ? List.<Long>of() : topic.prerequisiteConceptIds()) {
                    int at = indexOfConcept(result, prerequisite);
                    if (at > index) {
                        result.add(index, result.remove(at));
                        moved = true;
                        break;
                    }
                }
            }
            if (!moved) {
                break;
            }
        }
        return result;
    }

    private static int indexOfConcept(List<WeakTopic> topics, Long conceptId) {
        for (int index = 0; index < topics.size(); index++) {
            if (conceptId.equals(topics.get(index).conceptId())) {
                return index;
            }
        }
        return -1;
    }

    // ── ③ 展开候选 ─────────────────────────────────────────────────

    /** 候选任务（还没分天）。`seq` 先记排序位次，装箱后再重编。 */
    private record Candidate(String kind, String topic, String title, int minutes, String why,
                             Long entryId, Long conceptId, boolean movedByPrerequisite) {
    }

    private static List<Candidate> candidates(List<WeakTopic> ordered, Map<Long, List<Carrier>> carriers,
                                              List<DueReview> due) {
        List<Candidate> list = new ArrayList<>();
        Map<Long, List<Carrier>> available = carriers == null ? Map.of() : carriers;
        Set<Long> usedEntries = new HashSet<>();

        for (WeakTopic topic : ordered) {
            String wrongSummary = whyTemplate(topic);
            List<Carrier> entries = topic.conceptId() == null
                    ? List.of()
                    : available.getOrDefault(topic.conceptId(), List.of());
            int taken = 0;
            for (Carrier carrier : entries) {
                if (taken >= MAX_CARRIERS_PER_CONCEPT) {
                    break;
                }
                taken++;
                usedEntries.add(carrier.entryId());
                list.add(new Candidate(
                        carrier.hasBlocks() ? KIND_DO : KIND_READ,
                        topic.topic(),
                        carrier.title(),
                        ReadingMinutes.of(carrier.bodyChars(), carrier.hasBlocks()),
                        wrongSummary,
                        carrier.entryId(),
                        topic.conceptId(),
                        moved(ordered, topic)));
            }
            if (taken == 0) {
                list.add(new Candidate(KIND_GAP, topic.topic(), topic.topic(), ReadingMinutes.GAP,
                        "这一概念没有可读内容", null, topic.conceptId(), moved(ordered, topic)));
            }
        }

        // 到期复习追加在最后：它不解决弱项，只是别忘了
        int reviews = 0;
        Set<Long> reviewSeen = new HashSet<>();
        for (DueReview review : due == null ? List.<DueReview>of() : due) {
            if (reviews >= MAX_REVIEWS) {
                break;
            }
            if (review == null || !reviewSeen.add(review.entryId()) || usedEntries.contains(review.entryId())) {
                continue;
            }
            reviews++;
            usedEntries.add(review.entryId());
            list.add(new Candidate(KIND_REVIEW, review.conceptId() == null ? "复习" : review.title(),
                    review.title(), ReadingMinutes.REVIEW, "距上次通过已过复习期",
                    review.entryId(), review.conceptId(), false));
        }
        return list;
    }

    /**
     * 这一项是否因为前置关系被提前了。只用于给 {@code why} 补一句说明，
     * 判定逻辑本身不依赖它。
     */
    private static boolean moved(List<WeakTopic> ordered, WeakTopic topic) {
        if (topic.prerequisiteConceptIds() == null || topic.prerequisiteConceptIds().isEmpty()) {
            return false;
        }
        for (Long prerequisite : topic.prerequisiteConceptIds()) {
            if (indexOfConcept(ordered, prerequisite) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static String whyTemplate(WeakTopic topic) {
        int wrong = Math.max(0, topic.total() - topic.correct());
        String base = "共 " + topic.total() + " 题错 " + wrong + " 题";
        if (topic.prerequisiteConceptIds() == null || topic.prerequisiteConceptIds().isEmpty()) {
            return base;
        }
        return base + "，前置概念已排在这一项之前";
    }

    // ── ④ 装箱 ─────────────────────────────────────────────────────

    /** 顺序装箱：放不下就换下一天，任务不切开。溢出由调用方按天数判断。 */
    private static List<List<Candidate>> pack(List<Candidate> candidates, int days, int budget) {
        List<List<Candidate>> bins = new ArrayList<>(days);
        for (int index = 0; index < days; index++) {
            bins.add(new ArrayList<>());
        }
        int day = 0;
        int remaining = budget;
        for (Candidate candidate : candidates) {
            if (candidate.minutes() > remaining) {
                day++;
                remaining = budget;
            }
            if (day >= days) {
                break;
            }
            bins.get(day).add(candidate);
            remaining -= candidate.minutes();
        }
        return bins;
    }

    /**
     * 空白日修正。
     *
     * <p>两件事：
     * <ol>
     *   <li><b>均衡</b>：顺序装箱会把所有东西堆在第 1 天（预算根本用不完），后几天空着——
     *       那看起来像"计划生成失败了"。这里从项最多的一天搬末尾项过去。</li>
     *   <li><b>补漏</b>：所有任务都分完了还有空天（弱项只有一两个），用**已排过的真实条目**
     *       补一条复习——回看前一天读的东西是这份计划本来就该有的动作，不是编造。
     *       条目的 id 与标题都来自 {@code carriers}，所以 REQ-021「每项落到真实内容」仍成立。</li>
     * </ol>
     *
     * <p>只搬**末尾**的项：第 1 天的第一项必须留在第 1 天，否则"首日最弱"就不成立了。
     */
    private static void balance(List<List<Candidate>> bins, List<Candidate> candidates) {
        int days = bins.size();
        for (int empty = 0; empty < days; empty++) {
            if (!bins.get(empty).isEmpty()) {
                continue;
            }
            int donor = -1;
            for (int index = 0; index < days; index++) {
                if (bins.get(index).size() > 1
                        && (donor < 0 || bins.get(index).size() > bins.get(donor).size())) {
                    donor = index;
                }
            }
            if (donor >= 0) {
                bins.get(empty).add(bins.get(donor).remove(bins.get(donor).size() - 1));
            }
        }
        fillWithReview(bins);
    }

    /**
     * 用前面天里已出现过的条目补足空天。
     *
     * <p>只在"候选任务本来就少于天数"时走到：此时不补就是空日历，而空日历会被读成
     * "这个计划没生成好"。补的是一条复习项，指向同一个真实条目。
     */
    private static void fillWithReview(List<List<Candidate>> bins) {
        for (int day = 0; day < bins.size(); day++) {
            if (!bins.get(day).isEmpty()) {
                continue;
            }
            Candidate source = null;
            for (int earlier = day - 1; earlier >= 0 && source == null; earlier--) {
                for (Candidate candidate : bins.get(earlier)) {
                    if (candidate.entryId() != null) {
                        source = candidate;
                        break;
                    }
                }
            }
            if (source == null) {
                continue;
            }
            bins.get(day).add(new Candidate(KIND_REVIEW, source.topic(), source.title(),
                    ReadingMinutes.REVIEW, "昨天读过的内容今天再回看一遍", source.entryId(),
                    source.conceptId(), false));
        }
    }

    private static Plan toPlan(List<List<Candidate>> bins, List<Candidate> candidates, int days) {
        List<DayTasks> dayTasks = new ArrayList<>(days);
        int placed = 0;
        for (int index = 0; index < days; index++) {
            List<Task> tasks = new ArrayList<>();
            int seq = 1;
            for (Candidate candidate : bins.get(index)) {
                tasks.add(new Task(index + 1, seq++, candidate.kind(), candidate.topic(),
                        candidate.title(), candidate.minutes(), candidate.why(),
                        candidate.entryId(), candidate.conceptId()));
                placed++;
            }
            dayTasks.add(new DayTasks(index + 1, List.copyOf(tasks)));
        }
        List<Task> unscheduled = new ArrayList<>();
        int seq = 1;
        for (int index = placed; index < candidates.size(); index++) {
            Candidate candidate = candidates.get(index);
            unscheduled.add(new Task(0, seq++, candidate.kind(), candidate.topic(),
                    candidate.title(), candidate.minutes(), candidate.why(),
                    candidate.entryId(), candidate.conceptId()));
        }
        return new Plan(List.copyOf(dayTasks), List.copyOf(unscheduled));
    }

    /** 供测试与调用方核对装箱结果：未经 balance 的原始分组也走同一条路。 */
    static Map<Integer, Integer> minutesPerDay(Plan plan) {
        Map<Integer, Integer> minutes = new LinkedHashMap<>();
        for (DayTasks day : plan.days()) {
            minutes.put(day.day(), day.tasks().stream().mapToInt(Task::minutes).sum());
        }
        return minutes;
    }
}
