package io.github.xiaou61.ai;

import java.util.List;

/**
 * 记忆自动整理的端口（WORK-013）。
 *
 * <p><b>签名里没有任何框架类型</b>（D-09 的要求）：实现放在 {@code internal.provider}，目前由
 * AgentScope 承载；换实现或升级框架都不动调用方。与 {@link EntrySummarizer} 同一纪律。
 *
 * <p>它只回答"给我这几轮对话和已有记忆，告诉我该对记忆做什么"——**不做**余额检查、不做限流、
 * 不管事务、不判断该不该跑（那些是 {@code MemoryExtractionService} 的事）。
 *
 * <p>失败语义：上游不可用、被拒、返回空内容、字段校验不通过，一律抛
 * {@link io.github.xiaou61.platform.BizException}；调用方**整次放弃**，不写半条。
 */
public interface MemoryExtractor {

    /**
     * @param request 一次整理的输入（增量对话 + 已有记忆）
     * @param access  本次调用用的凭据（明文只在这一次调用期间存在）
     */
    MemoryExtraction extract(MemoryExtractionRequest request, ModelAccess access);

    /**
     * 一次整理的输入。
     *
     * @param turns   上次整理之后新增的对话（旧 → 新）。**是增量，不是整个会话**——
     *                那既是成本控制，也是"这一段在聊什么"的判断依据
     * @param memories 已有记忆（上限由调用方控制）。模型要靠这里的 id 做 `UPDATE`/`DELETE`，
     *                 所以它必须**带 id**，不能只给文本
     */
    record MemoryExtractionRequest(List<ConversationTurn> turns, List<MemoryLine> memories) {
    }

    /** 已有记忆的一行：id + 文本 + 在哪一路。 */
    record MemoryLine(String store, long id, String text) {
    }

    /**
     * 一次整理的结果。
     *
     * @param candidates 模型判定的候选（可能为空数组——"这几轮没什么值得记的"是合法结果）。
     *                   **注意与"模型返回空内容"区分**：后者是故障，由实现抛异常，不该走到这里
     * @param usage      本次调用的用量，供结算
     */
    record MemoryExtraction(List<MemoryCandidate> candidates, TokenUsage usage) {
    }
}
