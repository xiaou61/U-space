package io.github.xiaou61.ai.internal.camp;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import io.github.xiaou61.ai.internal.provider.SingleShotModelCaller;
import org.springframework.stereotype.Component;

/**
 * 出题端口（WORK-029 TASK-008）。
 *
 * <p><b>不带任何工具</b>：DeepSeek 的工具调用与结构化输出不能在同一次调用里并用，而这里的
 * 输出必须是可校验的 JSON。复用 {@link SingleShotModelCaller} 的骨架——那些设置
 * （不挂工具、关原生结构化输出、超时、错误归一、空内容按失败）与简历那三次完全同源，
 * 分散复制会得到"出卷走思考模式、简历不走"这种极难察觉的不一致。
 *
 * <p>端口与实现同一个类：这里只有"组装提示词 + 调模型 + 解析"三件事，没有第二个实现的需求。
 */
@Component
public class CampItemGenerator extends SingleShotModelCaller {

    private final CampItemParser parser;

    CampItemGenerator(AiProperties properties, GuardedHttpTransport transport, CampItemParser parser) {
        super(properties, transport);
        this.parser = parser;
    }

    /**
     * 一次生成的结果。
     *
     * <p>{@code rawText} 要带回去：修复重试必须把**原输出**连同违规原因一起回传，
     * 模型才认得出是哪几道题。只传 {@code items} 会丢掉模型自己的写法。
     */
    public record Generated(java.util.List<ItemValidator.RawItem> items, String rawText, TokenUsage usage) {
    }

    /**
     * 诊断卷。
     *
     * @param targetCount 目标题数（3 天营 12、7 天营 18）
     */
    public Generated diagnostic(String skillBlock, String conceptBlock, int targetCount,
                                ModelAccess access) {
        TierDistribution.Tiers tiers = TierDistribution.split(targetCount);
        int topics = Math.max(1, Math.min(6, targetCount / 2));
        ModelReply reply = call(CampPrompts.system(),
                CampPrompts.diagnosticUser(skillBlock, conceptBlock, tiers, topics), access);
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }

    /**
     * 复测卷：同主题同题量同分布，避开诊断卷做过的题。
     *
     * @param previousStems 诊断卷全部题干（模型据此避免重复）
     */
    public Generated retest(String skillBlock, String conceptBlock, String quotaBlock,
                            java.util.List<String> previousStems, ModelAccess access) {
        ModelReply reply = call(CampPrompts.system(),
                CampPrompts.retestUser(skillBlock, conceptBlock, quotaBlock, previousStems), access);
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }

    /**
     * 修复重试：把违规项原文与原因回传，只要求改这些项。
     *
     * <p>不重试"空返回"是刻意的：空返回通常意味着 max_tokens 被思考吃满，再问一次同样会被吃满，
     * 只会把一次失败变成两次花费。调用方据此直接退款。
     */
    public Generated repair(String previousRaw, java.util.List<String> violations, ModelAccess access) {
        ModelReply reply = call(CampPrompts.repairSystem(),
                "原输出：\n" + previousRaw + "\n\n需要修正的题目与原因：\n" + String.join("\n", violations),
                access);
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }
}
