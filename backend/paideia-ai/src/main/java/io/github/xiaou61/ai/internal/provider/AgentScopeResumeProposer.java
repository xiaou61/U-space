package io.github.xiaou61.ai.internal.provider;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.ResumeAnalysis;
import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.ResumeProposal;
import io.github.xiaou61.ai.ResumeProposer;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.ai.internal.resume.ResumeAnalysisView;
import io.github.xiaou61.ai.internal.resume.ResumeProposalParser;
import io.github.xiaou61.ai.internal.resume.ResumePrompts;
import io.github.xiaou61.ai.internal.resume.ResumeText;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 生成逐条改写提案（WORK-018 TASK-003）。
 *
 * <p>这次调用拿到的上下文最多（简历 + JD + 上一步分析 + 缺口清单），也是唯一一次
 * 产出会直接进定稿的文字。因此返回值必须过 {@link ResumeProposalParser} 的三道结构校验
 * ——位置真实、原文对得上、缺口词不出现。
 */
@Component
class AgentScopeResumeProposer extends SingleShotModelCaller implements ResumeProposer {

    private final ResumeProposalParser parser;

    AgentScopeResumeProposer(AiProperties properties, GuardedHttpTransport transport,
                             ResumeProposalParser parser) {
        super(properties, transport);
        this.parser = parser;
    }

    @Override
    public Proposed propose(ResumeData resume, String jdText, ResumeAnalysis analysis,
                            List<String> gaps, ModelAccess access) {
        List<ResumeAnalysisView> requirements = analysis.requirements().stream()
                .map(item -> new ResumeAnalysisView(item.term(), item.quote()))
                .toList();
        String analysisText = ResumeText.renderAnalysis(requirements, analysis.summary());
        String gapText = gaps.isEmpty() ? "（无：这份简历覆盖了 JD 里所有显式要求）" : String.join("、", gaps);

        ModelReply reply = call(
                ResumePrompts.proposeSystem(),
                ResumePrompts.proposeUser(ResumeText.render(resume), jdText, analysisText, gapText),
                access);
        ResumeProposalParser.ParsedProposals parsed = parser.parse(reply.text(), resume, gaps);
        return new Proposed(parsed.proposals(), parsed.dropped(), reply.usage());
    }
}
