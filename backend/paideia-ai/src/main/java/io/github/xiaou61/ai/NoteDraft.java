package io.github.xiaou61.ai;

/**
 * 助手产出的一条**笔记草稿**。
 *
 * <p><b>它不是笔记</b>：模型只能提出"我打算记这么一条"，真要落库得由用户点击确认后调笔记端点
 * （用户裁决 7：自主只建议不动作）。所以这个类型的生命周期止于"送到界面"。
 *
 * <p><b>它同时是"结构化输出"在本项的载体</b>：标题与正文是**工具的参数**（有 JSON Schema 约束），
 * 而不是靠 provider 的结构化输出能力。这正是绕开 DeepSeek "工具与结构化输出不能在同一次调用
 * 并用"那条限制的办法——不去让两者共存，而是让工具参数本身承担结构化。
 */
public record NoteDraft(String title, String body) {

    /** 兜底标题：模型没给出标题时用它，避免生成一条列表里显示空白的笔记。 */
    public static final String FALLBACK_TITLE = "来自助手的笔记";

    public NoteDraft {
        title = title == null || title.isBlank() ? FALLBACK_TITLE : title.trim();
        body = body == null ? "" : body;
    }
}
