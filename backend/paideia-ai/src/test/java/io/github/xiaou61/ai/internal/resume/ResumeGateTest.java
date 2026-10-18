package io.github.xiaou61.ai.internal.resume;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.ResumeProposal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 事实闸门（WORK-018 AC-004）。
 *
 * <p>四条检查各有正例与反例。<b>反例是重点</b>：每一种失真都必须真的被拦住，
 * 否则"AI 优化简历"就会变成"AI 编简历"。
 */
class ResumeGateTest {

    private final ResumeGate gate = new ResumeGate();

    private static final ResumeData RESUME = new ResumeData(
            new ResumeData.Basics("张三", "13800000000", "a@b.c", "杭州", List.of()),
            new ResumeData.Intent("Java 后端", "杭州", ""),
            List.of(),
            List.of(new ResumeData.Work("某公司", "后端开发工程师", "2023.07", "至今",
                    "负责订单系统",
                    List.of("把接口平均响应从 800ms 降到 200ms",
                            "用 Redis 做缓存，QPS 从 1200 提到 3000"))),
            List.of(), List.of(new ResumeData.SkillGroup("语言", List.of("Java"))),
            List.of(), "");

    private static final String PLAIN = """
            某公司 后端开发工程师 2023.07 - 至今
            把接口平均响应从 800ms 降到 200ms
            用 Redis 做缓存，QPS 从 1200 提到 3000
            """;

    // ── ① 数字 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("数字：原文有的数字放行")
    void allowsNumbersPresentInSource() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "高并发场景下把接口平均响应从 800ms 降到 200ms");

        assertThat(gate.check(RESUME, PLAIN, List.of(proposal), List.of())).isEmpty();
    }

    @Test
    @DisplayName("数字：原文没有的数字被拦（编造指标）")
    void blocksInventedNumber() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "把接口平均响应从 800ms 降到 200ms，额外提升 30% 吞吐");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.NUMBERS_NOT_IN_SOURCE);
    }

    @Test
    @DisplayName("数字：80 不能命中 800（完整数字才算）")
    void numberMustMatchWholeToken() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "把接口平均响应从 800ms 降到 80ms");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.NUMBERS_NOT_IN_SOURCE);
    }

    @Test
    @DisplayName("数字：纯年份不算指标（不该被这条误伤）")
    void yearAloneIsNotMetric() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "2023 年 7 月起把接口平均响应从 800ms 降到 200ms");

        // 年份不进入指标检查；身份检查那一侧会看到时间在原句里（2023.07），
        // 而原句没有"2023 年 7 月"字样，所以那条也不触发。这条用例要的是"不误报指标"。
        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .doesNotContain(ResumeGate.NUMBERS_NOT_IN_SOURCE);
    }

    // ── ② 重合度 ────────────────────────────────────────────────────

    @Test
    @DisplayName("重合度：正常换说法放行")
    void allowsLegitimateRewrite() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "高并发场景下优化接口性能，平均响应从 800ms 降到 200ms");

        assertThat(gate.check(RESUME, PLAIN, List.of(proposal), List.of())).isEmpty();
    }

    @Test
    @DisplayName("重合度：整条换成另一件事被拦")
    void blocksUnrelatedRewrite() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "用 Redis 做缓存，QPS 从 1200 提到 3000",
                "主导跨境电商结算平台的架构设计与团队管理，带 8 人团队交付");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.LOW_OVERLAP);
    }

    @Test
    @DisplayName("重合度：reorder 不检查（只是顺序变化）")
    void skipsOverlapForReorder() {
        ResumeProposal proposal = new ResumeProposal(
                "work[0].bullets[0]", "bullet",
                "把接口平均响应从 800ms 降到 200ms",
                "把接口平均响应从 800ms 降到 200ms",
                "reorder", "按照 JD 的相关度提前", List.of());

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .doesNotContain(ResumeGate.LOW_OVERLAP);
    }

    @Test
    @DisplayName("重合度：压缩成一句（内容全来自原文）不算低覆盖")
    void allowsCompression() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "接口响应从 800ms 降到 200ms");

        assertThat(gate.check(RESUME, PLAIN, List.of(proposal), List.of())).isEmpty();
    }

    // ── ③ 身份字段 ──────────────────────────────────────────────────

    @Test
    @DisplayName("身份：职位名被改掉 → 拦（升职式改写）")
    void blocksChangedTitle() {
        ResumeProposal proposal = proposal("work[0].summary",
                "后端开发工程师 负责订单系统",
                "后端技术专家 负责订单系统");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.IDENTITY_CHANGED);
    }

    @Test
    @DisplayName("身份：公司名被改掉 → 拦")
    void blocksChangedCompany() {
        ResumeProposal proposal = proposal("work[0].summary",
                "某公司 负责订单系统",
                "某大型互联网公司 负责订单系统");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.IDENTITY_CHANGED);
    }

    @Test
    @DisplayName("身份：时间被改掉 → 拦")
    void blocksChangedPeriod() {
        ResumeProposal proposal = proposal("work[0].summary",
                "2023.07 - 至今 负责订单系统",
                "2022.01 - 2024.12 负责订单系统");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of())))
                .contains(ResumeGate.IDENTITY_CHANGED);
    }

    // ── ④ 缺口词 ────────────────────────────────────────────────────

    @Test
    @DisplayName("缺口词：改写里出现简历完全没有的能力 → 拦")
    void blocksGapKeywordLeak() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "用 Kubernetes 编排后把接口平均响应从 800ms 降到 200ms");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of("Kubernetes"))))
                .contains(ResumeGate.GAP_KEYWORD_LEAK);
    }

    @Test
    @DisplayName("缺口词：CJK 缺口用子串判定")
    void blocksCjkGapKeyword() {
        ResumeProposal proposal = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "做过高并发治理，把接口平均响应从 800ms 降到 200ms");

        assertThat(codes(gate.check(RESUME, PLAIN, List.of(proposal), List.of("高并发"))))
                .contains(ResumeGate.GAP_KEYWORD_LEAK);
    }

    // ── 汇总 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("多条提案：违规按提案序号定位，不互相污染")
    void violationsCarryProposalIndex() {
        ResumeProposal clean = proposal("work[0].bullets[0]",
                "把接口平均响应从 800ms 降到 200ms",
                "高并发场景下把接口平均响应从 800ms 降到 200ms");
        ResumeProposal dirty = proposal("work[0].bullets[1]",
                "用 Redis 做缓存，QPS 从 1200 提到 3000",
                "用 Redis 做缓存，QPS 从 1200 提到 50000");

        List<ResumeGate.Violation> violations = gate.check(RESUME, PLAIN, List.of(clean, dirty), List.of());

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).proposalIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("覆盖率算法：完全一致为 1，完全无关为 0")
    void overlapBaseline() {
        assertThat(ResumeGate.overlap("把接口响应从 800ms 降到 200ms", "把接口响应从 800ms 降到 200ms"))
                .isEqualTo(1.0);
        assertThat(ResumeGate.overlap("把接口响应从 800ms 降到 200ms", "主导跨境结算平台架构"))
                .isLessThan(0.2);
    }

    private static List<String> codes(List<ResumeGate.Violation> violations) {
        return violations.stream().map(ResumeGate.Violation::code).toList();
    }

    private static ResumeProposal proposal(String ref, String original, String proposed) {
        return new ResumeProposal(ref, "bullet", original, proposed, "rewrite", "测试用", List.of());
    }
}
