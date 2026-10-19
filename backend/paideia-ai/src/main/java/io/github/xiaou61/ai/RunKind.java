package io.github.xiaou61.ai;

/**
 * 一次 AI 调用的**场景**（WORK-022）。
 *
 * <p><b>这里是唯一列出可取值的的地方</b>：库里 `ai_runs.kind` 是不加约束的字符串，
 * 因为"加一条新的 AI 路径"是常态，而 MySQL 枚举每加一个值都要 DDL。
 * 新增一条路径的成本就是在这里加一行常量，统计页的筛选维度自动多一个值。
 *
 * <p>取值与既有服务名对齐，让人从 `kind` 直接猜到是哪条链路。
 */
public final class RunKind {

    /** 学习者与助手的多轮对话（含面试、导师行为）。 */
    public static final String CHAT = "chat";

    /** 每 N 轮异步整理记忆（WORK-013）。 */
    public static final String MEMORY_EXTRACT = "memory_extract";

    /** 面试点评（WORK-010）。 */
    public static final String INTERVIEW_REVIEW = "interview_review";

    /** 外部通过 MCP 调平台 AI 问答（WORK-019）。 */
    public static final String MCP_ASK = "mcp_ask";

    /** 简历解析（WORK-018）。 */
    public static final String RESUME_IMPORT = "resume_import";

    /** 简历分析与提案（WORK-018）。 */
    public static final String RESUME_OPTIMIZE = "resume_optimize";

    /** 条目精简卡片（WORK-005）。 */
    public static final String ENTRY_SUMMARY = "entry_summary";

    private RunKind() {
    }
}
