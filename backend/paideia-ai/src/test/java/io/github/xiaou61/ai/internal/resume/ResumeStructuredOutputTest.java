package io.github.xiaou61.ai.internal.resume;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.ai.ResumeAnalysis;
import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.ResumeProposal;
import io.github.xiaou61.platform.BizException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 分析与提案解析里的三道结构校验（WORK-018 TASK-003）。
 *
 * <p>重点不是"能读 JSON"，而是三条**反向**用例：位置不存在、原文被改、缺口词出现。
 * 每一条都对应一类模型真实会犯的错，也都是"如果不拦就会进定稿"的错误。
 */
class ResumeStructuredOutputTest {

    private final ResumeAnalysisParser analysisParser = new ResumeAnalysisParser();
    private final ResumeProposalParser proposalParser = new ResumeProposalParser();

    private static final ResumeData RESUME = new ResumeData(
            new ResumeData.Basics("张三", "13800000000", "a@b.c", "杭州", List.of()),
            new ResumeData.Intent("Java 后端", "杭州", ""),
            List.of(),
            List.of(new ResumeData.Work("某公司", "后端开发工程师", "2023.07", "至今", "负责订单系统",
                    List.of("把接口平均响应从 800ms 降到 200ms", "参与支付对账模块的拆分"))),
            List.of(),
            List.of(new ResumeData.SkillGroup("语言", List.of("Java"))),
            List.of(), "");

    // ── 分析 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("分析：正常返回要求清单")
    void parsesAnalysis() {
        String raw = """
                {"summary":"看重高并发与稳定性",
                 "requirements":[{"term":"高并发","quote":"有高并发系统经验"},
                                 {"term":"Kubernetes","quote":"熟悉 Kubernetes 优先"}]}
                """;

        ResumeAnalysis analysis = analysisParser.parse(raw);

        assertThat(analysis.requirements()).hasSize(2);
        assertThat(analysis.requirements().get(0).term()).isEqualTo("高并发");
        assertThat(analysis.summary()).isEqualTo("看重高并发与稳定性");
    }

    @Test
    @DisplayName("分析：零要求判失败，不能当成\"这个岗位什么都不要\"")
    void emptyRequirementsIsFailure() {
        assertThatThrownBy(() -> analysisParser.parse("{\"summary\":\"x\",\"requirements\":[]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有给出任何 JD 要求");
    }

    @Test
    @DisplayName("分析：缺引证的要求保留（缺出处不等于编造），空 term 丢弃")
    void analysisKeepsQuoteLessRequirementsButDropsBlankTerms() {
        String raw = """
                {"summary":"x",
                 "requirements":[{"term":"高并发","quote":""},
                                 {"term":"","quote":"有 Kubernetes 经验"},
                                 {"term":"Kubernetes","quote":"有 Kubernetes 经验"}]}
                """;

        ResumeAnalysis analysis = analysisParser.parse(raw);

        assertThat(analysis.requirements()).hasSize(2);
        assertThat(analysis.requirements().get(1).term()).isEqualTo("Kubernetes");
    }

    // ── 提案 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("提案：正常返回，字段完整")
    void parsesProposals() {
        String raw = """
                {"proposals":[{
                  "entryRef":"work[0].bullets[0]","field":"bullet",
                  "originalText":"把接口平均响应从 800ms 降到 200ms",
                  "proposedText":"高并发场景下把接口平均响应从 800ms 降到 200ms",
                  "changeType":"rewrite",
                  "rationale":"JD 提到高并发","coversKeywords":["高并发"]
                }]}
                """;

        ResumeProposalParser.ParsedProposals parsed = proposalParser.parse(raw, RESUME, List.of());

        assertThat(parsed.proposals()).hasSize(1);
        assertThat(parsed.dropped()).isZero();
        assertThat(parsed.proposals().get(0).changeType()).isEqualTo("rewrite");
        assertThat(parsed.proposals().get(0).coversKeywords()).containsExactly("高并发");
    }

    @Test
    @DisplayName("提案：entryRef 指向不存在的位置 → 丢弃（否则闸门无法回查原文）")
    void dropsUnknownEntryRef() {
        String raw = """
                {"proposals":[
                  {"entryRef":"work[9].bullets[0]","originalText":"x","proposedText":"y","changeType":"rewrite"},
                  {"entryRef":"work[0].bullets[0]",
                   "originalText":"把接口平均响应从 800ms 降到 200ms",
                   "proposedText":"高并发下的接口优化","changeType":"rewrite"}
                ]}
                """;

        ResumeProposalParser.ParsedProposals parsed = proposalParser.parse(raw, RESUME, List.of());

        assertThat(parsed.proposals()).hasSize(1);
        assertThat(parsed.dropped()).isEqualTo(1);
    }

    @Test
    @DisplayName("提案：模型篡改 originalText → 丢弃（用户看到的\"原文\"必须是他的原文）")
    void dropsTamperedOriginalText() {
        String raw = """
                {"proposals":[{
                  "entryRef":"work[0].bullets[0]",
                  "originalText":"把接口平均响应从 9000ms 降到 10ms",
                  "proposedText":"高并发下的接口优化","changeType":"rewrite"
                }]}
                """;

        assertThatThrownBy(() -> proposalParser.parse(raw, RESUME, List.of()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有通过校验");
    }

    @Test
    @DisplayName("提案：缺口词出现在改写里 → 丢弃（提示词写了不够，代码才算数）")
    void dropsGapKeywordLeak() {
        String raw = """
                {"proposals":[
                  {"entryRef":"work[0].bullets[0]",
                   "originalText":"把接口平均响应从 800ms 降到 200ms",
                   "proposedText":"用 Kubernetes 编排订单系统","changeType":"rewrite"},
                  {"entryRef":"work[0].bullets[1]",
                   "originalText":"参与支付对账模块的拆分",
                   "proposedText":"参与支付对账模块的服务拆分","changeType":"rewrite"}
                ]}
                """;

        ResumeProposalParser.ParsedProposals parsed =
                proposalParser.parse(raw, RESUME, List.of("Kubernetes"));

        assertThat(parsed.proposals()).hasSize(1);
        assertThat(parsed.proposals().get(0).proposedText()).doesNotContain("Kubernetes");
        assertThat(parsed.dropped()).isEqualTo(1);
    }

    @Test
    @DisplayName("提案：全部被丢弃 → 判失败，不返回空清单让界面以为\"没问题\"")
    void allDroppedIsFailure() {
        String raw = """
                {"proposals":[{"entryRef":"work[9]","originalText":"x","proposedText":"y"}]}
                """;

        assertThatThrownBy(() -> proposalParser.parse(raw, RESUME, List.of()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有通过校验");
    }

    @Test
    @DisplayName("提案：不认识的 changeType 归 rewrite，不因此丢整条")
    void unknownChangeTypeFallsBack() {
        String raw = """
                {"proposals":[{
                  "entryRef":"work[0].bullets[0]",
                  "originalText":"把接口平均响应从 800ms 降到 200ms",
                  "proposedText":"高并发下的接口优化","changeType":"polish"
                }]}
                """;

        assertThat(proposalParser.parse(raw, RESUME, List.of()).proposals().get(0).changeType())
                .isEqualTo("rewrite");
    }
}
