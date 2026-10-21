package io.github.xiaou61.ai.internal.provider;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.ResumeParser;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.ai.internal.resume.ResumeJson;
import io.github.xiaou61.ai.internal.resume.ResumePrompts;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次简历解析（WORK-018 TASK-003）。
 *
 * <p>与另外两次调用共享 {@link SingleShotModelCaller} 的五条设置（不挂工具、非流式、
 * 关原生化输出、关思考、超时归一）。这里只剩两件事：拼提示词、把返回值交给严格校验。
 */
@Component
class AgentScopeResumeParser extends SingleShotModelCaller implements ResumeParser {

    private final ResumeJson json;

    AgentScopeResumeParser(AiProperties properties, GuardedHttpTransport transport, ResumeJson json) {
        super(properties, transport);
        this.json = json;
    }

    @Override
    public Parsed parse(String text, ModelAccess access) {
        ModelReply reply = call(ResumePrompts.parseSystem(), ResumePrompts.parseUser(text), access);
        ResumeJson.ParsedDraft draft = json.parseDraft(reply.text(), text);
        return new Parsed(draft.data(), draft.dropped(), draft.truncatedFields(), reply.usage());
    }
}
