package io.github.xiaou61.platform;

import java.util.Map;

/**
 * 一条工具的**完整元数据**。它是内部助手与外部 MCP 共同的真源（WORK-019 REQ-001/002）。
 *
 * <p><b>为什么不把 SDK 类型直接当契约</b>：MCP SDK 的 `Tool` 来自 `io.modelcontextprotocol.*`，
 * 而 AgentScope 也会传递带入一个版本的 SDK；两处若各自引类型，工具实现就被绑死在某个 SDK 版本上。
 * 这里只放 JSON Schema 与布尔标记这类稳定形状，由两个适配器各自翻译。
 *
 * <p><b>schema 用 {@code Map<String,Object>} 而不是 Strongly-typed</b>：它要被原样序列化给模型，
 * 任何中间表示都得再转一次；MCP 与 OpenAI 工具协议本来就吃 JSON Schema 字面量。
 *
 * @param name         稳定名称，`toolset.verb_object` 形状（点号是 MCP 规范允许的字符）
 * @param toolset      所属工具组（视图按它切分）
 * @param title        给人看的一行标题（MCP 的 `title` 字段）
 * @param description  给模型看的描述；**选不选得对主要靠它**
 * @param inputSchema  输入 JSON Schema，必须是 `type: object`
 * @param outputSchema 输出 JSON Schema；没有结构化输出时为 {@code null}
 * @param readOnly     只读标记（MCP `readOnlyHint`）。**只读视图按它过滤**
 * @param destructive  破坏性标记（删除/覆盖类）。只读工具不得为 true
 * @param idempotent   幂等标记（重复调用结果相同）
 * @param scope        所需作用域
 * @param defaultGroup 是否属于默认视图/默认核心包
 */
public record ToolDescriptor(
        String name,
        String toolset,
        String title,
        String description,
        Map<String, Object> inputSchema,
        Map<String, Object> outputSchema,
        boolean readOnly,
        boolean destructive,
        boolean idempotent,
        ToolScope scope,
        boolean defaultGroup) {
}
