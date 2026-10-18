package io.github.xiaou61.platform;

import java.util.Map;

/**
 * 一次工具调用的结果。
 *
 * <p><b>失败不抛异常</b>：工具出错要作为可观察结果交回模型，让它自己决定换个方式回答，
 * 而不是把整轮对话炸掉（与 {@code AssistantTools.Outcome} 同一条纪律）。两个适配器都据此
 * 映射：内部喂回模型，外部转成 MCP 的 `isError: true`。
 *
 * @param error      是否失败
 * @param text       给模型/用户看的文本；失败时是**脱敏**的一句，不含堆栈与内部地址
 * @param structured 结构化输出（可选）；有 `outputSchema` 的工具应当填它
 */
public record ToolResult(boolean error, String text, Map<String, Object> structured) {

    public static ToolResult ok(String text) {
        return new ToolResult(false, text, null);
    }

    public static ToolResult ok(String text, Map<String, Object> structured) {
        return new ToolResult(false, text, structured);
    }

    public static ToolResult fail(String text) {
        return new ToolResult(true, text, null);
    }
}
