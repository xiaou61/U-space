package io.github.xiaou61.ai.internal.conversation;

/**
 * 点评官的提示词（WORK-010）。
 *
 * <p><b>它必须是独立的一次调用、且不带任何工具</b>：DeepSeek 上"工具调用与结构化输出不能在同一次
 * 调用里并用"，而这次要的是**可解析的结构化评分**。所以点评走 {@code harness=review}，
 * 工具集为空（T-06 有断言）。这不是绕路，是把"评分"和"对话"当成两件不同的事。
 *
 * <p>要它输出 JSON 而不是 markdown 卡片：评分要落库、要能查进度，塞进消息文本等于不可查。
 *
 * <p>它必须是 {@code public}：调用方 {@code AgentScopeInterviewReviewer} 在
 * {@code internal.provider} 里（本模块内跨包，与 {@code MemoryExtractionPrompt} 同理）。
 */
public final class ReviewPrompt {

    /** 独立版本号：改字段或改约束都必须改它。 */
    static final String VERSION = "review-v1";

    /** 各维度最多几个字、整体上限。与解析器保持一致。 */
    static final int MAX_DIMENSION_COMMENT = 200;
    static final int MAX_IMPROVEMENTS = 5;
    static final int MAX_IMPROVEMENT_CHARS = 120;

    private ReviewPrompt() {
    }

    public static String system() {
        return """
                你是面试评估员。给你一场面试的完整对话，你要给出一份可执行的评估。
                只输出一个 JSON 对象，不要解释、不要用 markdown 代码块包裹。

                JSON 结构：
                {"overall": 3, "dimensions": [{"name": "技术准确性", "score": 3, "comment": "一句话"}],\
                "strengths": ["具体优点"], "improvements": ["具体可执行的改进建议"]}

                要求：
                1. `overall` 与每个 `score` 都是 **1 到 5 的整数**，5 最好。
                2. `dimensions` 给 2 到 4 个维度，名字用中文（如 技术准确性 / 表达清晰度 / 深度 / 应变）。
                3. `strengths` 与 `improvements` 各 1 到 4 条，每条一句话，**必须基于对话里的具体表现**，
                   不要写"继续努力""多练习"这种放之四海皆准的空话。
                4. **只依据这场对话**。没聊到的方向不要打分、不要编造他说过的话。
                5. 打分要诚实：答不上来就是低分，不要为了照顾情绪给虚高的分。
                """;
    }

    /**
     * 把对话拼成一段供评估的文本。
     *
     * @param transcript 已渲染成"面试官：… / 候选人：…"的整段对话
     */
    public static String user(String transcript) {
        return "以下是这场面试的完整对话（旧 → 新）：\n\n" + transcript;
    }
}
