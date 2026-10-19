package io.github.xiaou61.ai;

/**
 * 一轮调用的身份（WORK-022）。
 *
 * <p>只放"开始时就一定知道"的东西；结果、耗时、用量都在 {@code RunScope} 上边跑边补。
 * 缺失的值一律为 {@code null}，不编造默认值——"这条路径没有会话"与"会话 id 是 0"不是一回事。
 *
 * <p><b>没有框架类型</b>（D-09 纪律，与 {@link AssistantConversation} 同一套）：换运行时不动这里。
 */
public record RunSpec(String kind,
                      long userId,
                      Long conversationId,
                      String harness,
                      String model,
                      String promptVersion) {

    /** 最小构造：绝大多数路径只知道"什么场景 + 谁在调"。 */
    public static RunSpec of(String kind, long userId) {
        return new RunSpec(kind, userId, null, null, null, null);
    }

    public RunSpec withConversation(Long conversationId) {
        return new RunSpec(kind, userId, conversationId, harness, model, promptVersion);
    }

    public RunSpec withHarness(String harness) {
        return new RunSpec(kind, userId, conversationId, harness, model, promptVersion);
    }

    public RunSpec withModel(String model) {
        return new RunSpec(kind, userId, conversationId, harness, model, promptVersion);
    }

    public RunSpec withPromptVersion(String promptVersion) {
        return new RunSpec(kind, userId, conversationId, harness, model, promptVersion);
    }
}
