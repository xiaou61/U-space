package io.github.xiaou61.ai.internal.tools;

import io.agentscope.core.model.ToolSchema;
import io.github.xiaou61.ai.NoteDraft;
import io.github.xiaou61.knowledge.KnowledgeApi;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 助手的**只读**工具（WORK-006 TASK-006）。
 *
 * <p>三个工具：检索条目、列分类、读本人的自评标记。**只有读**——写路径属 TASK-009/010，
 * 而且必须经用户点击确认，不会出现在这里。
 *
 * <p><b>归属绑在调用侧，模型碰不到</b>：`userId` 由调用方（当前请求）传进来，模型只能提供
 * 关键词与条数。工具参数里**没有任何表示"读谁的数据"的字段**，所以不存在"模型换个 id 读别人"
 * 这条路——不是靠校验挡住，而是那条路根本不存在。检索与分类本身就是公开内容（只返回已发布），
 * 不需要归属；自评标记是私有的，那个工具拿的正是调用方的 userId。
 *
 * <p>失败**不抛异常**：工具出错要作为可观察结果交回模型，让它自己决定换个方式回答，
 * 而不是把整轮对话炸掉。
 */
@Component
public class AssistantTools {

    /** 工具名。模型看到的就是这三个字符串；改名等于改提示词，要一起改。 */
    static final String SEARCH_ENTRIES = "search_entries";
    static final String LIST_CATEGORIES = "list_categories";
    static final String MY_MARKS = "my_marks";
    static final String DRAFT_NOTE = "draft_note";

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final KnowledgeApi knowledge;

    AssistantTools(KnowledgeApi knowledge) {
        this.knowledge = knowledge;
    }

    /**
     * 一次工具调用的结果。
     *
     * <p>`error=true` 表示这次调用失败，由模型决定怎么应对；`draft` 非空表示这次调用产出了
     * 一条**待确认的笔记草稿**（它不写库，只送到界面）。
     */
    public record Outcome(boolean error, String text, NoteDraft draft) {

        static Outcome text(String text) {
            return new Outcome(false, text, null);
        }

        static Outcome failed(String text) {
            return new Outcome(true, text, null);
        }
    }

    /** 交给模型的工具清单。描述写清"什么时候该用"，模型选不选得对主要靠它。 */
    public List<ToolSchema> schemas() {
        return List.of(
                ToolSchema.builder()
                        .name(SEARCH_ENTRIES)
                        .description("按关键词检索平台上的知识条目，返回条目的 id 与标题。"
                                + "用户问「有没有讲过 X」「哪些内容涉及 X」时用它。"
                                + "关键词留空则返回最近的内容。拿到 id 后如果需要正文，直接说明你需要哪一条。")
                        .parameters(objectSchema(Map.of(
                                "keyword", Map.of("type", "string", "description", "检索关键词，可为空"),
                                "limit", Map.of("type", "integer", "description", "最多返回几条，默认 5，上限 20"))))
                        .build(),
                ToolSchema.builder()
                        .name(LIST_CATEGORIES)
                        .description("列出平台全部知识分类（含上级分类的 id）。"
                                + "用户问「都有哪些分类」「内容是怎么组织的」时用它。")
                        .parameters(objectSchema(Map.of()))
                        .build(),
                ToolSchema.builder()
                        .name(MY_MARKS)
                        .description("列出**当前用户自己**标记过「懂了／还不懂」的条目。"
                                + "用户问「我哪些还没搞明白」「帮我复习标记过的内容」时用它。"
                                + "只能读当前用户自己的标记，无法查询他人。")
                        .parameters(objectSchema(Map.of(
                                "limit", Map.of("type", "integer", "description", "最多返回几条，默认 5，上限 20"))))
                        .build(),
                ToolSchema.builder()
                        .name(DRAFT_NOTE)
                        .description("起草一条给当前用户自己看的笔记。用户说「帮我记一条」「把这个记下来」时用它。"
                                + "**它只是草稿，不会真的保存**——要用户点确认才会存进笔记。"
                                + "正文写清完整内容（用户可能直接采用），标题一句话概括。")
                        .parameters(objectSchema(Map.of(
                                "title", Map.of("type", "string", "description", "笔记标题，一句话"),
                                "body", Map.of("type", "string", "description", "笔记正文，可多行"))))
                        .build());
    }

    /**
     * 执行一次工具调用。
     *
     * @param userId 当前调用者的账号 id；这是**唯一**的归属来源，不从工具参数里取
     */
    public Outcome run(long userId, String name, Map<String, Object> input) {
        try {
            return switch (name == null ? "" : name) {
                case SEARCH_ENTRIES -> Outcome.text(search(input));
                case LIST_CATEGORIES -> Outcome.text(categories());
                case DRAFT_NOTE -> draftNote(input);
                case MY_MARKS -> Outcome.text(marks(userId, input));
                default -> Outcome.failed("没有名为「" + name + "」的工具");
            };
        } catch (RuntimeException exception) {
            // 工具失败回到模型，不往上抛：一次坏查询不该让整轮对话失败
            return Outcome.failed("工具执行失败：" + messageOf(exception));
        }
    }

    /**
     * 起草一条笔记。
     *
     * <p><b>它不写库</b>：只把标题与正文包成草稿交出去，界面据此问用户"要不要存"。
     * 这是"自主只建议不动作"的落点——模型能提议，不能落笔。
     *
     * <p>标题与正文是**工具参数**：结构化由 JSON Schema 承担，因此不需要 provider 的结构化输出，
     * 也就不存在"工具与结构化输出不能同一次调用并用"那条限制（DeepSeek 的坑）。
     */
    private Outcome draftNote(Map<String, Object> input) {
        String title = stringOf(input, "title");
        String body = stringOf(input, "body");
        if (body == null) {
            // 空草稿没有意义：让模型知道这次没成，它可以补上再提一次
            return Outcome.failed("草稿的正文不能为空");
        }
        NoteDraft draft = new NoteDraft(title, body);
        return new Outcome(false, "已准备一条笔记草稿（标题：" + draft.title() + "），等待用户确认。", draft);
    }

    private String search(Map<String, Object> input) {
        String keyword = stringOf(input, "keyword");
        List<KnowledgeApi.EntryBrief> hits = knowledge.searchPublished(keyword, limitOf(input));
        if (hits.isEmpty()) {
            // 明确说"没找到"而不是空串：空字符串会让模型以为工具坏了或者数据不存在
            return keyword == null ? "平台上还没有已发布的内容。" : "没有检索到与「" + keyword + "」相关的已发布内容。";
        }
        return hits.stream()
                .map(hit -> "id=" + hit.id() + " 标题=" + hit.title())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String categories() {
        List<KnowledgeApi.CategoryNode> nodes = knowledge.listCategories();
        if (nodes.isEmpty()) {
            return "平台还没有建立分类。";
        }
        return nodes.stream()
                .map(node -> "id=" + node.id() + " 名称=" + node.name()
                        + " 上级=" + (node.parentId() == null ? "无（顶级）" : node.parentId()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    private String marks(long userId, Map<String, Object> input) {
        List<KnowledgeApi.MarkedEntry> marks = knowledge.findMarks(userId, limitOf(input));
        if (marks.isEmpty()) {
            return "当前用户还没有标记过任何条目。";
        }
        return marks.stream()
                .map(mark -> "id=" + mark.entryId() + " 标题=" + mark.title() + " 标记=" + mark.level())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    /**
     * 工具参数 → 条数。
     *
     * <p>模型给的值**不可信**：可能缺、可能是字符串、可能给 0 或 1000。这里统一夹到
     * [1, MAX_LIMIT]，负数和 0 都退回默认值——一次要一千条会把上下文塞爆。
     */
    private static int limitOf(Map<String, Object> input) {
        Object raw = input == null ? null : input.get("limit");
        int value;
        if (raw instanceof Number number) {
            value = number.intValue();
        } else if (raw instanceof String text) {
            try {
                value = Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return DEFAULT_LIMIT;
            }
        } else {
            return DEFAULT_LIMIT;
        }
        if (value <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(value, MAX_LIMIT);
    }

    private static String stringOf(Map<String, Object> input, String key) {
        Object raw = input == null ? null : input.get(key);
        if (raw == null) {
            return null;
        }
        String text = raw.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static Map<String, Object> objectSchema(Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties, "required", List.of());
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }
}
