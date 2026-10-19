package io.github.xiaou61.ai;

/**
 * 一步的身份（WORK-022）。
 *
 * @param kind 步骤类型：`llm` / `tool` / `retrieval` / `persist` / `billing`
 * @param name 明细名：工具名原样，或 `llm#2` 这种轮次标号。**不写正文与参数**
 */
public record SpanSpec(String kind, String name) {

    public static final String KIND_LLM = "llm";
    public static final String KIND_TOOL = "tool";
    public static final String KIND_RETRIEVAL = "retrieval";

    /** 第 n 次上游调用（n 从 1 开始）。 */
    public static SpanSpec llm(int round) {
        return new SpanSpec(KIND_LLM, "llm#" + round);
    }

    public static SpanSpec tool(String name) {
        return new SpanSpec(KIND_TOOL, name);
    }

    public static SpanSpec retrieval(String name) {
        return new SpanSpec(KIND_RETRIEVAL, name);
    }
}
