package io.github.xiaou61.platform;

/**
 * 可执行的工具。
 *
 * <p>实现放在**拥有数据的模块**里（知识工具在 knowledge、笔记工具在 note），并且只依赖本模块的
 * 公共 API；`paideia-mcp` 只认这个接口，不 import 任何业务包（Spring Modulith 边界不变）。
 *
 * <p>实现者注意：`arguments` 是**模型给的不可信输入**。按 schema 校验之后仍要自己夹范围
 * （例如条数上限），因为校验只保证类型，不保证取值合理。
 */
public interface PaideiaTool {

    ToolDescriptor descriptor();

    ToolResult call(ToolContext context, java.util.Map<String, Object> arguments);
}
