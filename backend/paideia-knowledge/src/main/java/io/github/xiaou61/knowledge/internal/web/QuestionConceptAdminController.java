package io.github.xiaou61.knowledge.internal.web;

import io.github.xiaou61.knowledge.internal.question.QuestionConceptAdminService;
import io.github.xiaou61.knowledge.internal.question.QuestionConceptSuggester;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端的题目概念标注（WORK-033）。
 *
 * <p>路径必须是 `/api/v1/learning/admin/**` 且安全规则排在 `/api/v1/learning/**` 之前，
 * 否则会落到 `anyRequest().authenticated()`——任何登录用户都能改标注。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
@RequestMapping("/api/v1/learning/admin")
class QuestionConceptAdminController {

    private final QuestionConceptAdminService service;

    QuestionConceptAdminController(QuestionConceptAdminService service) {
        this.service = service;
    }

    @GetMapping("/questions")
    ApiResponse<List<QuestionConceptAdminService.QuestionView>> questions(
            @RequestParam("entryId") long entryId) {
        return ApiResponse.ok(service.list(entryId));
    }

    @PutMapping("/question-concepts")
    ApiResponse<Void> replace(@RequestBody ReplaceRequest body) {
        service.replace(body.entryId(), body.blockKey(), body.questionKey(),
                body.concepts(), body.source());
        return ApiResponse.ok(null);
    }

    @GetMapping("/question-concepts/suggestions")
    ApiResponse<List<QuestionConceptSuggester.Suggestion>> suggestions(
            @RequestParam("entryId") long entryId,
            @RequestParam("blockKey") String blockKey,
            @RequestParam("questionKey") String questionKey,
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return ApiResponse.ok(service.suggestions(entryId, blockKey, questionKey, limit));
    }

    record ReplaceRequest(long entryId, String blockKey, String questionKey,
                          List<QuestionConceptAdminService.TagRequest> concepts, String source) {
    }
}
