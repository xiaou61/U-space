package io.github.xiaou61.ai;

/**
 * 一步的身份（WORK-022，WORK-036 加层级）。
 *
 * @param kind      步骤类型：`llm` / `tool` / `retrieval` / `persist` / `billing`
 * @param name      明细名：工具名原样，或 `llm#2` 这种轮次标号。**不写正文与参数**
 * @param parentSeq 父步骤的 seq；为空表示顶层
 */
public record SpanSpec(String kind, String name, Integer parentSeq) {

    public static final String KIND_LLM = "llm";
    public static final String KIND_TOOL = "tool";
    public static final String KIND_RETRIEVAL = "retrieval";

    public SpanSpec(String kind, String name) {
        this(kind, name, null);
    }

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

    /**
     * 挂在另一条步骤下的子步骤（WORK-036 REQ-014）。
     *
     * <p><b>本期没有实际调用点</b>：工具步内部没有二次模型调用，`retrieval` 也零使用。
     * 这里先让端口与渲染支持层级，是因为列、mapper、`SpanView` 早就齐了，
     * 唯一缺的就是这个入口——没有它，`parent_seq` 永远是 NULL，
     * 将来真需要分层时还得再改一次端口签名。
     */
    public static SpanSpec child(String kind, String name, int parentSeq) {
        return new SpanSpec(kind, name, parentSeq);
    }
}
