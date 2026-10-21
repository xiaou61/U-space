package io.github.xiaou61.ai.internal.camp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 分档、指纹与题目校验（WORK-029 TASK-003/004）。
 *
 * <p>校验那部分用表驱动：每条坏输入对应一条用例，于是"哪条检查被删了"能直接指向哪条红。
 */
class ItemRulesTest {

    // ── 分档 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("分档：整除与余数都落在中档")
    void tierDistribution() {
        assertThat(TierDistribution.split(12)).isEqualTo(new TierDistribution.Tiers(4, 4, 4));
        assertThat(TierDistribution.split(18)).isEqualTo(new TierDistribution.Tiers(6, 6, 6));
        assertThat(TierDistribution.split(2)).isEqualTo(new TierDistribution.Tiers(0, 2, 0));
        assertThat(TierDistribution.split(0)).isEqualTo(new TierDistribution.Tiers(0, 0, 0));
        assertThat(TierDistribution.split(-3)).isEqualTo(new TierDistribution.Tiers(0, 0, 0));
        // 4 → 1/2/1，三档之和始终等于入参
        TierDistribution.Tiers four = TierDistribution.split(4);
        assertThat(four.total()).isEqualTo(4);
        assertThat(four.medium()).isEqualTo(2);
    }

    // ── 指纹 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("指纹忽略标点与空白，不同题干得到不同指纹")
    void fingerprintIgnoresPunctuation() {
        String plain = "HashMap 为什么线程不安全？";
        String noisy = "HashMap为什么线程不安全";
        assertThat(CampFingerprint.of(plain)).isEqualTo(CampFingerprint.of(noisy));
        assertThat(CampFingerprint.of(plain)).hasSize(64);
        assertThat(CampFingerprint.of("另一个完全不同的问题")).isNotEqualTo(CampFingerprint.of(plain));
        assertThat(CampFingerprint.of(null)).isEqualTo(CampFingerprint.of(""));
    }

    // ── 校验 ────────────────────────────────────────────────────────

    private static ItemValidator.RawItem ok() {
        return new ItemValidator.RawItem("集合", "HashMap", "medium",
                "HashMap 在并发扩容时可能发生什么？",
                List.of("死循环", "内存泄漏", "数据丢失", "编译失败"), 0,
                "死循环是 JDK7 头插法在并发扩容时的典型后果。");
    }

    private static ItemValidator.RawItem withOptions(List<String> options) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), base.conceptName(), base.difficulty(),
                base.stem(), options, base.answerIndex(), base.explanation());
    }

    private static ItemValidator.RawItem withStem(String stem) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), base.conceptName(), base.difficulty(),
                stem, base.options(), base.answerIndex(), base.explanation());
    }

    private static ItemValidator.RawItem withExplanation(String explanation) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), base.conceptName(), base.difficulty(),
                base.stem(), base.options(), base.answerIndex(), explanation);
    }

    private static ItemValidator.RawItem withAnswer(int index) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), base.conceptName(), base.difficulty(),
                base.stem(), base.options(), index, base.explanation());
    }

    private static ItemValidator.RawItem withDifficulty(String difficulty) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), base.conceptName(), difficulty,
                base.stem(), base.options(), base.answerIndex(), base.explanation());
    }

    private static ItemValidator.RawItem withTopic(String topic) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(topic, base.conceptName(), base.difficulty(),
                base.stem(), base.options(), base.answerIndex(), base.explanation());
    }

    private static ItemValidator.RawItem withConcept(String conceptName) {
        ItemValidator.RawItem base = ok();
        return new ItemValidator.RawItem(base.topic(), conceptName, base.difficulty(),
                "另一道完全不同的题干，用来避开指纹去重", base.options(), base.answerIndex(),
                base.explanation());
    }

    @Test
    @DisplayName("一条合法题通过，未知名概念置空但保留题")
    void goodItemPassesAndUnknownConceptIsNulled() {
        ItemValidator.Validated result = ItemValidator.validate(List.of(ok(), withConcept("不存在的概念")),
                Map.of("hashmap", 7L));

        assertThat(result.accepted()).hasSize(2);
        assertThat(result.dropped()).isEmpty();
        assertThat(result.accepted().get(0).conceptId()).isEqualTo(7L);
        assertThat(result.accepted().get(1).conceptId()).isNull();
    }

    @Test
    @DisplayName("十二种坏输入各自命中对应原因")
    void everyCheckHasATest() {
        assertThat(dropReason(withOptions(List.of("A项", "B项", "C项"))))
                .isEqualTo(ItemValidator.Reason.OPTION_COUNT);
        assertThat(dropReason(withOptions(List.of("A项", "B项", "C项", "D项", "E项"))))
                .isEqualTo(ItemValidator.Reason.OPTION_COUNT);
        assertThat(dropReason(withAnswer(-1))).isEqualTo(ItemValidator.Reason.ANSWER_INDEX);
        assertThat(dropReason(withAnswer(4))).isEqualTo(ItemValidator.Reason.ANSWER_INDEX);
        assertThat(dropReason(withOptions(List.of("死循环", "死循环", "数据丢失", "编译失败"))))
                .isEqualTo(ItemValidator.Reason.OPTION_DUPLICATED);
        assertThat(dropReason(withStem("太短"))).isEqualTo(ItemValidator.Reason.STEM_LENGTH);
        assertThat(dropReason(withStem("长".repeat(301)))).isEqualTo(ItemValidator.Reason.STEM_LENGTH);
        assertThat(dropReason(withExplanation(""))).isEqualTo(ItemValidator.Reason.EXPLANATION_LENGTH);
        assertThat(dropReason(withExplanation("这段解析讲的是另一件事，完全没提到正确选项。")))
                .isEqualTo(ItemValidator.Reason.EXPLANATION_NOT_ANCHORED);
        // "hard " 带空格：模型没按契约输出，丢题而不是替他改正
        assertThat(dropReason(withDifficulty("hard "))).isEqualTo(ItemValidator.Reason.DIFFICULTY);
        assertThat(dropReason(withTopic("主".repeat(61)))).isEqualTo(ItemValidator.Reason.TOPIC_LENGTH);
        assertThat(dropReason(withOptions(List.of("A", "B", "C", "D"))))
                .isEqualTo(ItemValidator.Reason.OPTION_LENGTH);
    }

    @Test
    @DisplayName("同卷重复题干只保留第一道")
    void duplicateStemIsDropped() {
        ItemValidator.RawItem second = new ItemValidator.RawItem(ok().topic(), ok().conceptName(),
                ok().difficulty(), "HashMap 在并发扩容时可能发生什么！", ok().options(),
                ok().answerIndex(), ok().explanation());

        ItemValidator.Validated result = ItemValidator.validate(List.of(ok(), second), Map.of());

        assertThat(result.accepted()).hasSize(1);
        assertThat(result.dropped()).singleElement()
                .satisfies(violation -> assertThat(violation.reason())
                        .isEqualTo(ItemValidator.Reason.DUPLICATE_STEM));
    }

    @Test
    @DisplayName("null / 空列表不抛错")
    void nullInputIsSafe() {
        assertThat(ItemValidator.validate(null, Map.of()).accepted()).isEmpty();
        assertThat(ItemValidator.validate(List.of(), Map.of()).dropped()).isEmpty();
    }

    private static ItemValidator.Reason dropReason(ItemValidator.RawItem item) {
        ItemValidator.Validated result = ItemValidator.validate(List.of(item), Map.of());
        assertThat(result.dropped()).as("应当被丢弃").hasSize(1);
        return result.dropped().get(0).reason();
    }
}
