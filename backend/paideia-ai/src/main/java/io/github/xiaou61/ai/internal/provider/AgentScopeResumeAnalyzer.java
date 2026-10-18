package io.github.xiaou61.ai.internal.provider;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.ResumeAnalyzer;
import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.ai.internal.resume.ResumeAnalysisParser;
import io.github.xiaou61.ai.internal.resume.ResumePrompts;
import io.github.xiaou61.ai.internal.resume.ResumeText;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次 JD 分析（WORK-018 TASK-003）。
 *
 * <p>它**只列 JD 要求 + 引证**，不判断候选人会不会——分桶是代码的事
 * （{@code ResumeKeywordClassifier}）。把"会不会"留给模型等于把最容易幻觉的判断交给了模型。
 */
@Component
class AgentScopeResumeAnalyzer extends ResumeModelCaller implements ResumeAnalyzer {

    private final ResumeAnalysisParser parser;

    AgentScopeResumeAnalyzer(AiProperties properties, GuardedHttpTransport transport,
                             ResumeAnalysisParser parser) {
        super(properties, transport);
        this.parser = parser;
    }

    @Override
    public Analyzed analyze(ResumeData resume, String jdText, ModelAccess access) {
        ModelReply reply = call(
                ResumePrompts.analyzeSystem(),
                ResumePrompts.analyzeUser(ResumeText.render(resume), jdText),
                access);
        return new Analyzed(parser.parse(reply.text()), reply.usage());
    }
}
