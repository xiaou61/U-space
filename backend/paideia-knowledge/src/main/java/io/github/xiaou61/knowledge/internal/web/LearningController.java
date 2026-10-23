package io.github.xiaou61.knowledge.internal.web;

import io.github.xiaou61.knowledge.internal.concept.ConceptGraphService;
import io.github.xiaou61.knowledge.internal.learning.LearningLineService;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressRules;
import io.github.xiaou61.knowledge.internal.learning.LearningProgressService;
import io.github.xiaou61.knowledge.internal.learning.LearningReviewService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习线与学习记录的端点（WORK-020）。
 *
 * <p>与自评同一纪律：**路径与请求体里都没有用户标识**，归属一律取自令牌
 * （`anyRequest().authenticated()` 兜住，无需角色限制）。
 *
 * <p>客户端只上报**分数**，不发送 `status`——通过判定在服务端，连伪造的语法都不存在。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/learning")
class LearningController {

    private final LearningLineService lines;
    private final LearningProgressService progress;
    private final ConceptGraphService graph;
    private final LearningReviewService reviews;

    LearningController(LearningLineService lines, LearningProgressService progress,
                       ConceptGraphService graph, LearningReviewService reviews) {
        this.lines = lines;
        this.progress = progress;
        this.graph = graph;
        this.reviews = reviews;
    }

    /** 到期复习列表（WORK-034 S1）。空列表是正常结果。 */
    @GetMapping("/reviews")
    ApiResponse<LearningReviewService.DueList> reviews() {
        return ApiResponse.ok(reviews.due(CurrentUserId.require(), java.time.Instant.now()));
    }

    /**
     * 线清单 + 本人的进度统计。`/learn` 页面与入口卡片都只读这一个端点。
     *
     * <p>线的集合来自分类上的 `line_kind`，**前端不写死课名与分类 id**：新增一条线不需要改前端。
     */
    @GetMapping("/lines")
    ApiResponse<LinesResponse> lines() {
        return ApiResponse.ok(new LinesResponse(lines.list(CurrentUserId.require())));
    }

    record LinesResponse(List<LearningLineService.Line> lines) {
    }

    /** 我自己的全部学习记录（只列仍已发布的条目）。 */
    @GetMapping("/progress")
    ApiResponse<List<LearningProgressService.View>> progress(
            @RequestParam(name = "limit", defaultValue = "200") int limit) {
        return ApiResponse.ok(progress.listMine(CurrentUserId.require(), limit));
    }

    /** 单站记录；没有记录时返回一份 `in_progress` 的初态（200，不是 404）。 */
    @GetMapping("/entries/{id}")
    ApiResponse<LearningProgressService.View> entry(@PathVariable("id") long id) {
        return ApiResponse.ok(progress.get(CurrentUserId.require(), id));
    }

    /**
     * 上报分数。两类块都可缺省，缺省表示"这次不报这一类"。
     *
     * <p>返回合并后的完整视图，前端据此刷新而不猜。
     */
    @PutMapping("/entries/{id}")
    ApiResponse<LearningProgressService.View> report(@PathVariable("id") long id,
                                                     @Valid @RequestBody ReportRequest body) {
        return ApiResponse.ok(progress.update(CurrentUserId.require(), id, body.toReport()));
    }

    /** 重置本站。幂等：本来没有记录也返回成功。 */
    @DeleteMapping("/entries/{id}")
    ApiResponse<Void> reset(@PathVariable("id") long id) {
        progress.reset(CurrentUserId.require(), id);
        return ApiResponse.ok(null);
    }

    /**
     * 我的知识图谱（WORK-021）：概念节点 + 关系边 + 统计。
     *
     * <p>掌握度**现算不落库**——它随一次小测、一次到期复习改变，存起来就要处理失效与并发。
     */
    @GetMapping("/graph")
    ApiResponse<ConceptGraphService.Graph> graph() {
        return ApiResponse.ok(graph.build(CurrentUserId.require()));
    }

    /**
     * 上报体。
     *
     * <p>分数写成 `{"blockId": {"score": 3, "total": 3}}`；服务层再夹一次范围
     * （这里的 record 只表达形状，取值校验在 `LearningProgressService`，因为那里也是
     * 内部调用方的入口）。
     */
    record ReportRequest(Integer tourStep,
                         Map<String, ScoreRequest> challenges,
                         Map<String, ScoreRequest> quizzes) {

        LearningProgressService.Report toReport() {
            return new LearningProgressService.Report(tourStep, toScores(challenges), toScores(quizzes));
        }

        private static Map<String, LearningProgressRules.BlockScore> toScores(Map<String, ScoreRequest> raw) {
            if (raw == null || raw.isEmpty()) {
                return Map.of();
            }
            Map<String, LearningProgressRules.BlockScore> scores = new LinkedHashMap<>();
            raw.forEach((id, value) -> {
                if (value != null) {
                    scores.put(id, new LearningProgressRules.BlockScore(value.score(), value.total()));
                }
            });
            return scores;
        }
    }

    record ScoreRequest(int score, int total) {
    }
}
