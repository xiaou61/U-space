package io.github.xiaou61.knowledge.internal.tools;

import io.github.xiaou61.knowledge.internal.concept.ConceptGraphService;
import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolDescriptor;
import io.github.xiaou61.platform.ToolResult;
import io.github.xiaou61.platform.ToolScope;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 薄弱概念查询（WORK-021）：让助手能回答"我哪里弱"。
 *
 * <p>它直接复用 {@code ConceptGraphService}，不另写一套掌握度计算——两处算法必然漂移，
 * 而界面上那张图和助手说的这句话必须是同一份结论。
 *
 * <p>只读工具：{@code readOnly = true}，目录据此把它放进只读视图。
 */
public final class ConceptTools {

    private ConceptTools() {
    }

    @Component
    public static class WeakConcepts implements PaideiaTool {

        private static final int LIMIT = 5;

        private final ConceptGraphService graph;

        WeakConcepts(ConceptGraphService graph) {
            this.graph = graph;
        }

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "knowledge_weak_concepts", "knowledge", "我掌握得最弱与待复习的概念",
                    "列出**当前调用者自己**掌握度最低的几个知识概念，以及已到期该复习的概念。"
                            + "用于回答「我哪里薄弱」「我该复习什么」。只返回本人数据。",
                    KnowledgeTools.objectSchema(Map.of()),
                    null, true, false, true, ToolScope.READ, true);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            List<ConceptGraphService.Node> nodes = graph.weakest(context.userId(), LIMIT);
            if (nodes.isEmpty()) {
                return ToolResult.ok("当前用户还没有可判断掌握度的知识概念（没有接触过任何概念，"
                        + "或管理员尚未建立概念）。");
            }
            StringBuilder text = new StringBuilder("掌握最弱与待复习的概念：\n");
            for (ConceptGraphService.Node node : nodes) {
                text.append("- ").append(node.name())
                        .append("（").append(tierLabel(node.tier()));
                if (node.reviewDue()) {
                    text.append("，待复习");
                }
                if (!node.entries().isEmpty()) {
                    text.append("，载体：").append(node.entries().get(0).title());
                }
                text.append("）\n");
            }
            return ToolResult.ok(text.toString());
        }

        private static String tierLabel(String tier) {
            return switch (tier) {
                case "mastered" -> "已掌握";
                case "familiar" -> "熟悉";
                case "learning" -> "了解";
                default -> "未接触";
            };
        }
    }
}
