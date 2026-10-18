package io.github.xiaou61.platform;

import java.util.Map;
import java.util.Set;

/**
 * 一次工具调用的上下文。**归属的唯一来源**（WORK-019 REQ-017）。
 *
 * <p><b>为什么 userId 在这里而不在工具参数里</b>：工具参数由模型填写，模型可以照着 README 编一个
 * 别人的 id。把归属放进上下文，意味着"读谁的数据"这个决定在调用链上**由凭据解析结果给出**，
 * 模型连表达它的语法都没有——不是靠校验挡住，而是那条路不存在。这与 WORK-006 给助手工具定的
 * 纪律是同一条。
 *
 * @param userId 调用者账号 id；工具实现只能用它做归属过滤
 * @param role   账号角色（`ADMIN` / `USER` / null）；管理类工具额外要求
 * @param scopes 本次凭据的作用域集合；内部调用是"全部允许"，外部调用来自 PAT
 * @param origin 调用来源，供日志与差异化行为（例如外部调用不写会话历史）
 */
public record ToolContext(long userId, String role, Set<ToolScope> scopes, Origin origin) {

    public enum Origin {
        /** 自家助手：进程内调用，不经过网络与凭据校验。 */
        INTERNAL,
        /** 外部 MCP 客户端：凭据是 PAT。 */
        EXTERNAL
    }

    public ToolContext {
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }

    public boolean has(ToolScope scope) {
        return scopes.contains(scope);
    }

    public boolean admin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /** 内部调用：凭据由服务端自己掌握，scope 视为全给（仍受角色约束）。 */
    public static ToolContext internal(long userId, String role) {
        return new ToolContext(userId, role, Set.of(ToolScope.READ, ToolScope.WRITE, ToolScope.ADMIN), Origin.INTERNAL);
    }
}
