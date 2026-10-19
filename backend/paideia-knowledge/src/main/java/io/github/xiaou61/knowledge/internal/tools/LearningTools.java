package io.github.xiaou61.knowledge.internal.tools;

import io.github.xiaou61.knowledge.internal.learning.LearningLineService;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressRules.BlockScore;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressService;
import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolDescriptor;
import io.github.xiaou61.platform.ToolResult;
import io.github.xiaou61.platform.ToolScope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * 学习记录工具（WORK-020）：让助手能回答"我学到哪了、哪门课最弱、今天该复习什么"。
 *
 * <p><b>归属只来自 {@link ToolContext}</b>：参数里没有"谁的"这个口子，与
 * {@code knowledge_my_marks} 同一纪律。模型即使照着文档编一个 userId 也没有语法可写。
 *
 * <p>只读：学习记录由做题上报写入，助手不改它。
 */
public final class LearningTools {

    private LearningTools() {
    }

    @Component
    public static class Progress implements PaideiaTool {

        private final LearningLineService lines;
        private final LearningProgressService progress;

        Progress(LearningLineService lines, LearningProgressService progress) {
            this.lines = lines;
            this.progress = progress;
        }

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "learning_progress", "learning", "我的学习进度",
                    "读出**当前调用者自己**的学习线与进度：每条线通过了多少站、下一站是哪一课、"
                            + "今天有哪些站该复习、哪几站得分最低（最该补）。"
                            + "用户问「我学到哪了」「我这门课怎么样」「我哪块最弱」时用它。"
                            + "只能读当前调用者自己的记录。",
                    KnowledgeTools.objectSchema(Map.of(
                            "limit", Map.of("type", "integer", "description", "最多返回几条记录，默认 5，上限 20"))),
                    null, true, false, true, ToolScope.READ, true);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            try {
                List<String> blocks = new ArrayList<>();

                List<LearningLineService.Line> mine = lines.list(context.userId());
                if (mine.isEmpty()) {
                    blocks.add("当前用户还没有开始任何学习线。");
                } else {
                    for (LearningLineService.Line line : mine) {
                        blocks.add("【" + line.name() + "】已通过 " + line.passedCount()
                                + " / " + line.stationCount()
                                + (line.nextEntry() == null
                                        ? "，全部通过"
                                        : "，下一站：" + line.nextEntry().title())
                                + (line.dueReviewCount() > 0
                                        ? "，今天该复习 " + line.dueReviewCount() + " 站"
                                        : ""));
                    }
                }

                List<LearningProgressService.View> recent = progress.listMine(context.userId(), 20);
                if (!recent.isEmpty()) {
                    int due = 0;
                    Instant now = Instant.now();
                    for (LearningProgressService.View view : recent) {
                        if (view.reviewDueAt() != null && !view.reviewDueAt().isAfter(now)) {
                            due++;
                        }
                    }
                    if (due > 0) {
                        blocks.add("今天到期的复习共 " + due + " 站。");
                    }

                    // 最弱的三站：只看得分比例，不看题量；同分时先列早学的
                    recent.stream()
                            .filter(view -> !view.challenges().isEmpty() || !view.quizzes().isEmpty())
                            .sorted(Comparator.comparingDouble(LearningTools.Progress::ratio))
                            .limit(3)
                            .forEach(view -> blocks.add("最该补：" + view.title()
                                    + "（挑战 " + summarize(view.challenges())
                                    + "；小测 " + summarize(view.quizzes()) + "）"));
                }
                return ToolResult.ok(KnowledgeTools.join(blocks));
            } catch (RuntimeException failure) {
                return ToolResult.fail("读取学习进度失败：" + KnowledgeTools.messageOf(failure));
            }
        }

        /** 得分比例；没有记录时按 1（不会排到"最弱"里，因为调用方已过滤空记录）。 */
        private static double ratio(LearningProgressService.View view) {
            double sum = 0;
            int count = 0;
            for (BlockScore score : allScores(view)) {
                if (score.total() > 0) {
                    sum += (double) score.score() / score.total();
                    count++;
                }
            }
            return count == 0 ? 1 : sum / count;
        }

        private static String summarize(Map<String, BlockScore> scores) {
            if (scores.isEmpty()) {
                return "无";
            }
            int got = 0;
            int total = 0;
            for (BlockScore score : scores.values()) {
                got += score.score();
                total += score.total();
            }
            return got + "/" + total;
        }

        /** 挑战与小测两块合一，避免两处写同一段遍历。 */
        private static List<BlockScore> allScores(LearningProgressService.View view) {
            List<BlockScore> all = new ArrayList<>(view.challenges().values());
            all.addAll(view.quizzes().values());
            return all;
        }
    }
}
