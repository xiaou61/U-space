package io.github.xiaou61.ai.internal.resume;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.ResumeAnalysis;
import io.github.xiaou61.ai.ResumeData;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 三桶分类（WORK-018 AC-005）。
 *
 * <p>夹具就是"一份主简历 + 一段 JD 要求"的固定组合，期望值人工标注在用例里——
 * 这样任何一次改等价表或改边界规则都会立刻显示为一条红用例。
 */
class ResumeKeywordClassifierTest {

    private final ResumeKeywordClassifier classifier = new ResumeKeywordClassifier();

    private static final ResumeData RESUME = new ResumeData(
            new ResumeData.Basics("张三", "13800000000", "a@b.c", "杭州", List.of()),
            new ResumeData.Intent("Java 后端", "杭州", ""),
            List.of(),
            List.of(new ResumeData.Work("某公司", "后端开发工程师", "2023.07", "至今",
                    "负责订单系统",
                    List.of("把接口平均响应从 800ms 降到 200ms",
                            "用 Redis 做缓存，QPS 从 1200 提到 3000",
                            "参与支付对账模块的拆分"))),
            List.of(new ResumeData.Project("内部网关", "主力开发", "2024",
                    List.of("基于 Spring Cloud Gateway 做限流"))),
            List.of(new ResumeData.SkillGroup("语言", List.of("Java", "SQL")),
                    new ResumeData.SkillGroup("中间件", List.of("Redis", "Kafka"))),
            List.of(), "");

    private static final String PLAIN = """
            张三 13800000000 a@b.c 杭州
            某公司 后端开发工程师 2023.07 - 至今
            负责订单系统
            把接口平均响应从 800ms 降到 200ms
            用 Redis 做缓存，QPS 从 1200 提到 3000
            参与支付对账模块的拆分
            内部网关 主力开发 2024
            基于 Spring Cloud Gateway 做限流
            技能 语言 Java SQL 中间件 Redis Kafka
            """;

    @Test
    @DisplayName("三桶各归其位：技能区写明=现有，正文出现过=有证据，全文没有=缺口")
    void classifiesThreeBuckets() {
        ResumeAnalysis analysis = analysis(
                "Java",            // 技能区
                "Redis",           // 技能区
                "订单系统",         // 只在经历里
                "Spring Cloud",    // 只在项目里
                "Kubernetes");     // 全文没有

        List<ResumeKeywordClassifier.Classified> result = classifier.classify(analysis, RESUME, PLAIN);

        assertThat(result).hasSize(5);
        assertThat(bucketOf(result, "Java")).isEqualTo(ResumeKeywordClassifier.BUCKET_EXISTING);
        assertThat(bucketOf(result, "Redis")).isEqualTo(ResumeKeywordClassifier.BUCKET_EXISTING);
        assertThat(bucketOf(result, "订单系统")).isEqualTo(ResumeKeywordClassifier.BUCKET_EVIDENCED);
        assertThat(bucketOf(result, "Spring Cloud")).isEqualTo(ResumeKeywordClassifier.BUCKET_EVIDENCED);
        assertThat(bucketOf(result, "Kubernetes")).isEqualTo(ResumeKeywordClassifier.BUCKET_GAP);
    }

    @Test
    @DisplayName("等价写法：k8s 命中 Kubernetes，js 命中 JavaScript")
    void matchesEquivalents() {
        ResumeData withK8s = withSkills(List.of("Kubernetes", "JavaScript"));
        ResumeAnalysis analysis = analysis("k8s", "js");

        List<ResumeKeywordClassifier.Classified> result = classifier.classify(analysis, withK8s, "");

        assertThat(result).allMatch(item -> ResumeKeywordClassifier.BUCKET_EXISTING.equals(item.bucket()));
    }

    @Test
    @DisplayName("ASCII 必须词边界命中：java 不能在 javascript 里算命中")
    void doesNotMatchSubstringForAscii() {
        ResumeData jsOnly = withSkills(List.of("JavaScript"));
        ResumeAnalysis analysis = analysis("Java");

        List<ResumeKeywordClassifier.Classified> result = classifier.classify(analysis, jsOnly, "");

        assertThat(bucketOf(result, "Java")).isEqualTo(ResumeKeywordClassifier.BUCKET_GAP);
    }

    @Test
    @DisplayName("CJK 用子串：高并发能在「有高并发经验」里命中")
    void matchesCjkAsSubstring() {
        ResumeData data = withSelfEvaluation("有高并发经验，做过压测");
        ResumeAnalysis analysis = analysis("高并发");

        assertThat(bucketOf(classifier.classify(analysis, data, ""), "高并发"))
                .isEqualTo(ResumeKeywordClassifier.BUCKET_EVIDENCED);
    }

    @Test
    @DisplayName("缺口清单只含缺口项，供提案阶段当「不许出现」的黑名单")
    void gapsOnlyReturnsGapBucket() {
        ResumeAnalysis analysis = analysis("Java", "Kubernetes", "Rust");

        List<String> gaps = classifier.gaps(classifier.classify(analysis, RESUME, PLAIN));

        assertThat(gaps).containsExactly("Kubernetes", "Rust");
    }

    @Test
    @DisplayName("同一要求只出现一次（模型可能重复列）")
    void keepsDuplicatesAsGiven() {
        ResumeAnalysis analysis = analysis("Java", "Java");

        // 不去重是刻意的：去重是调用方的事，而这里有重复就说明模型给了重复，
        // 界面应该照实显示两条（用户可以自己判断哪条是噪音）。
        assertThat(classifier.classify(analysis, RESUME, PLAIN)).hasSize(2);
    }

    // ── 助手 ────────────────────────────────────────────────────────

    private static String bucketOf(List<ResumeKeywordClassifier.Classified> items, String term) {
        return items.stream().filter(item -> item.term().equals(term)).findFirst()
                .orElseThrow(() -> new AssertionError("没有这条：" + term)).bucket();
    }

    private static ResumeAnalysis analysis(String... terms) {
        return new ResumeAnalysis(
                java.util.Arrays.stream(terms)
                        .map(term -> new ResumeAnalysis.Requirement(term, "JD 原句：" + term))
                        .toList(),
                "测试用");
    }

    private static ResumeData withSkills(List<String> skills) {
        return new ResumeData(ResumeData.empty().basics(), ResumeData.empty().intent(),
                List.of(), List.of(), List.of(),
                List.of(new ResumeData.SkillGroup("技能", skills)),
                List.of(), "");
    }

    private static ResumeData withSelfEvaluation(String text) {
        return new ResumeData(ResumeData.empty().basics(), ResumeData.empty().intent(),
                List.of(), List.of(), List.of(), List.of(), List.of(), text);
    }
}
