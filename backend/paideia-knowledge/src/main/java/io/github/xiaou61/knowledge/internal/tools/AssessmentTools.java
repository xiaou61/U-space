package io.github.xiaou61.knowledge.internal.tools;

import io.github.xiaou61.knowledge.internal.assessment.SelfAssessmentService;
import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolDescriptor;
import io.github.xiaou61.platform.ToolResult;
import io.github.xiaou61.platform.ToolScope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 自评标记的工具组（WORK-019）：一个读、一个写。
 *
 * <p>它与 {@code knowledge.my_marks} 的区别是：那个读的是"知识库模块自己那份视图"，
 * 这两个是给外部助手用的自评入口，写那条会触发既有的 {@code EntryAssessmentChangedEvent}
 * （WORK-013 的"自评 → 结构化记忆"桥），所以外部助手打的标记也会进用户的记忆。
 *
 * <p><b>写工具必须标 readOnly=false</b>：目录与视图都靠这个标记把它挡在只读视图之外。
 */
public final class AssessmentTools {

    /** 合法取值透传自评服务的定义，不在这里重复字面量。 */
    private static final String LEVEL_UNDERSTOOD = "understood";
    private static final String LEVEL_UNSURE = "unsure";

    private AssessmentTools() {
    }

    /** 列出自己的标记，可按 level 过滤。 */
    @Component
    public static class ListMarks implements PaideiaTool {

        private final SelfAssessmentService assessments;

        ListMarks(SelfAssessmentService assessments) {
            this.assessments = assessments;
        }

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "assessment.list_marks", "assessment", "列出我的自评标记",
                    "列出**当前调用者自己**标记过「懂了／还不懂」的条目，可按标记类型过滤。"
                            + "与 knowledge.my_marks 是同一份数据的两个入口；新代码优先用这个。",
                    KnowledgeTools.objectSchema(Map.of(
                            "level", Map.of("type", "string",
                                    "enum", List.of(LEVEL_UNDERSTOOD, LEVEL_UNSURE),
                                    "description", "只列这一种标记；留空表示两种都要"))),
                    null, true, false, true, ToolScope.READ, true);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            List<SelfAssessmentService.AssessedItem> items =
                    assessments.listMine(context.userId(), KnowledgeTools.stringOf(arguments, "level"));
            if (items.isEmpty()) {
                return ToolResult.ok("当前用户还没有标记过任何条目。");
            }
            return ToolResult.ok(KnowledgeTools.join(items.stream()
                    .map(item -> "id=" + item.entryId() + " 标题=" + item.title() + " 标记=" + item.level())
                    .toList()));
        }
    }

    /** 打一个自评标记（写）。 */
    @Component
    public static class MarkEntry implements PaideiaTool {

        private final SelfAssessmentService assessments;

        MarkEntry(SelfAssessmentService assessments) {
            this.assessments = assessments;
        }

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "assessment.mark_entry", "assessment", "标记条目掌握程度",
                    "把一篇**已发布**知识条目标记为「懂了」或「还不懂」。这是一次写入：会覆盖该条目上"
                            + "原有的标记，并可能触发助手记忆的更新。调用前请确认用户确实要这样标。",
                    KnowledgeTools.objectSchema(Map.of(
                            "entryId", Map.of("type", "integer", "description", "条目 id"),
                            "level", Map.of("type", "string", "enum", List.of(LEVEL_UNDERSTOOD, LEVEL_UNSURE),
                                    "description", "understood=懂了，unsure=还不懂"))),
                    null, false, false, true, ToolScope.WRITE, false);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            Long entryId = KnowledgeTools.longOf(arguments, "entryId");
            String level = KnowledgeTools.stringOf(arguments, "level");
            if (entryId == null || level == null) {
                return ToolResult.fail("需要 entryId 与 level");
            }
            try {
                // 归属取 context；mark 内部会校验条目已发布，未发布/不存在抛业务异常
                assessments.mark(context.userId(), entryId, level);
                return ToolResult.ok("已把条目 id=" + entryId + " 标记为「" + level + "」。");
            } catch (RuntimeException failure) {
                return ToolResult.fail("标记失败：" + KnowledgeTools.messageOf(failure));
            }
        }
    }
}
