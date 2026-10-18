package io.github.xiaou61.platform;

import java.util.regex.Pattern;

/**
 * 工具名规则（WORK-019 REQ-003）。
 *
 * <p>形状固定为 `toolset.verb_object`：**点号是 MCP 规范允许的字符**，同时把"聚合网关撞名"
 * 这个问题提前解决——规范专门提醒过，多个 server 的工具会重名、建议加前缀。我们不等聚合时
 * 才处理，而是从第一天就把工具组写进名字里。
 *
 * <p>约束是**比规范更紧**的：规范允许大写、连字符与数字开头；这里只允许小写字母开头、
 * 小写字母/数字/下划线的段。理由是这几个字符在 URL 路径、日志字段、配置文件里都不用转义。
 */
public final class ToolNaming {

    /** 最多 128 字符（MCP 规范的建议上限）。 */
    public static final int MAX_LENGTH = 128;

    /**
     * 两段以上，每段 `[a-z][a-z0-9_]*`，段间用点号。
     *
     * <p>要求至少一个点号是刻意的：没有工具组的工具无法被任何视图切分出来，等于只能出现在
     * "全部"里。强制带前缀，让每个工具从一开始就属于某个组。
     */
    private static final Pattern VALID =
            Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$");

    private ToolNaming() {
    }

    public static boolean isValid(String name) {
        return name != null && name.length() <= MAX_LENGTH && VALID.matcher(name).matches();
    }

    /** 从合法名字里取工具组前缀；不合法时返回 null。 */
    public static String toolsetOf(String name) {
        if (!isValid(name)) {
            return null;
        }
        return name.substring(0, name.indexOf('.'));
    }
}
