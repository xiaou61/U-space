package io.github.xiaou61.knowledge.internal.web;

import io.github.xiaou61.knowledge.internal.question.PrerequisiteDiagnosis;
import io.github.xiaou61.knowledge.internal.question.QuestionAttemptService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 逐题作答与掌握诊断（WORK-033）。
 *
 * <p>与学习记录同一条纪律：请求体里没有用户标识，归属取自令牌；对错由服务端判定，
 * 客户端只能上报"选了第几个"。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/learning")
class QuestionAttemptController {

    private final QuestionAttemptService service;

    QuestionAttemptController(QuestionAttemptService service) {
        this.service = service;
    }

    @PostMapping("/attempts")
    ApiResponse<QuestionAttemptService.AttemptResult> attempt(
            @RequestBody QuestionAttemptService.AttemptRequest body) {
        return ApiResponse.ok(service.record(CurrentUserId.require(), body));
    }

    @GetMapping("/mastery")
    ApiResponse<List<QuestionAttemptService.ConceptMasteryView>> mastery() {
        return ApiResponse.ok(service.mastery(CurrentUserId.require()));
    }

    @GetMapping("/diagnosis")
    ApiResponse<List<PrerequisiteDiagnosis.Suggestion>> diagnosis(
            @RequestParam("conceptId") long conceptId) {
        return ApiResponse.ok(service.diagnosis(CurrentUserId.require(), conceptId));
    }
}
