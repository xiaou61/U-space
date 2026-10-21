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

    /**
     * 输出上限的硬顶。
     *
     * <p><b>4096 对训练营远远不够</b>：一次出 12 道带解析的单选题，实测正文约 4000 token，
     * 而 deepseek-flash 的思考还会再吃掉 3200 左右——2026-10-21 实测 {@code finish_reason=length}，
     * 4096 上限下第 3 道题就被截断，整份 JSON 不是合法的（表现为"模型输出不符合题目格式"，
     * 看起来像模型不听话，其实是我们的上限太小）。
     *
     * <p>8192 是当前 DeepSeek 可选值的硬顶；按题数算出的值超过它时仍会被它夹住。
     */
    private static final int MAX_OUTPUT_TOKENS = 8192;

    /** 每道题的输出预算。实测一道题（题干 + 4 选项 + 解析）约 350–450 token。 */
    private static final int TOKENS_PER_ITEM = 450;
    /** 思考 token 的预留。deepseek-flash 关不掉思考，实测一次 3000–3500。 */
    private static final int REASONING_RESERVE = 4096;

    private final CampItemParser parser;

    CampItemGenerator(AiProperties properties, GuardedHttpTransport transport, CampItemParser parser) {
        super(properties, transport);
        this.parser = parser;
    }

    /**
     * 按题数算输出上限：{@code 思考预留 + 题数 × 单题预算}，夹到硬顶。
     *
     * <p>固定 4096 会把 12 道题截断（实测）；18 道题更需要放大。**由调用时按本次规模算**，
     * 不放在字段里——生成器是 singleton bean，字段会被并发请求串味。
     */
    private static int budgetFor(int itemCount) {
        return Math.min(MAX_OUTPUT_TOKENS, REASONING_RESERVE + itemCount * TOKENS_PER_ITEM);
    }

    /**
     * 按题数算等待上限。
     *
     * <p>实测 12 道题耗时 25–30 秒（含 3000+ 思考 token），18 道题约 40–50 秒。
     * 取每道题 10 秒再加 60 秒余量，夹到 300 秒——与 {@code GuardedHttpTransport}
     * 的读取上限对齐。**默认的 30 秒绝对不够**：诊断卷 29.8 秒是压线通过，复测 30.1 秒就失败。
     */
    private static int timeoutFor(int itemCount) {
        return Math.min(300, 60 + itemCount * 10);
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
                CampPrompts.diagnosticUser(skillBlock, conceptBlock, tiers, topics), access,
                budgetFor(targetCount), timeoutFor(targetCount));
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }

    /**
     * 复测卷：同主题同题量同分布，避开诊断卷做过的题。
     *
     * @param previousStems 诊断卷全部题干（模型据此避免重复）
     */
    public Generated retest(String skillBlock, String conceptBlock, String quotaBlock,
                            java.util.List<String> previousStems, ModelAccess access) {
        int planned = plannedCount(quotaBlock);
        ModelReply reply = call(CampPrompts.system(),
                CampPrompts.retestUser(skillBlock, conceptBlock, quotaBlock, previousStems), access,
                budgetFor(planned), timeoutFor(planned));
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }

    /** 复测的题量藏在 quotaBlock 里（每个主题一行「N 道」），数出来就是本次要出的题数。 */
    private static int plannedCount(String quotaBlock) {
        int total = 0;
        for (String line : quotaBlock.split("\\n")) {
            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern.compile("(\\d+) 道").matcher(line);
            if (matcher.find()) {
                total += Integer.parseInt(matcher.group(1));
            }
        }
        return Math.max(1, total);
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
                access, budgetFor(12), timeoutFor(12));
        return new Generated(parser.parse(reply.text()), reply.text(), reply.usage());
    }
}
