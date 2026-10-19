package io.github.xiaou61.platform;

import java.util.regex.Pattern;

/**
 * 工具名规则（WORK-019 REQ-003）。
 *
 * <p><b>为什么是下划线而不是点号</b>：MCP 规范确实允许点号，但**内部适配器要把同一批名字发给
 * DeepSeek 的 OpenAI 兼容接口**，那条通道只接受 {@code ^[a-zA-Z0-9_-]+$}——点号会被 400 拒绝
 * （2026-10-19 真调上游时实测：{@code Invalid 'tools[0].function.name'}）。
 *
 * <p>两条路可选：两边各用一套名字（MCP 用点号、内部用下划线），或者一个名字两边都能用。
 * 选后者——"唯一真源"的价值就在于没有第二套名字，否则将来必然有人在 MCP 侧改了名而内部没改。
 *
 * <p>前缀仍然强制：没有工具组的工具无法被任何视图切分，只能出现在"全部"里。形状是
 * {@code <toolset>_<verb_object>}。
 *
 * <p>约束比两边规范都**更紧**：只允许小写字母开头、小写字母/数字/下划线。这几个字符在 URL 路径、
 * 日志字段、配置文件里都不用转义。
 */
public final class ToolNaming {

    /** 最多 128 字符（MCP 规范的建议上限）。 */
    public static final int MAX_LENGTH = 128;

    /** `<toolset>_<verb_object>`：至少一个下划线，全小写字母/数字/下划线。 */
    private static final Pattern VALID = Pattern.compile("^[a-z][a-z0-9]*_[a-z][a-z0-9_]*$");

    private ToolNaming() {
    }

    public static boolean isValid(String name) {
        return name != null && name.length() <= MAX_LENGTH && VALID.matcher(name).matches();
    }

    /** 前缀（第一个下划线之前那一段）；不合法时返回 null。 */
    public static String toolsetOf(String name) {
        if (!isValid(name)) {
            return null;
        }
        return name.substring(0, name.indexOf('_'));
    }
}
