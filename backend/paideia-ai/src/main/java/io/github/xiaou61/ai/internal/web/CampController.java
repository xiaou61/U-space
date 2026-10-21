package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.camp.CampService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 训练营的 11 个端点（WORK-029）。
 *
 * <p><b>全部 {@code AUTHENTICATED}，没有一个 {@code PUBLIC_READ}</b>：营、题目、作答、计划
 * 都是用户私有数据，不存在"拿到链接就能看"的路径（REQ-039）。归属一律取自令牌，
 * 请求里没有用户标识可传。
 *
 * <p>路径不带用户标识，取不到（不存在 / 不是本人的）一律 404——区分开就等于泄露
 * "某个营存在"。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/camps")
class CampController {

    private final CampService camps;

    CampController(CampService camps) {
        this.camps = camps;
    }

    record CreateRequest(Long resumeDocumentId, Integer days) {
    }

    record AnswerItem(Long itemId, Integer choice) {
    }

    record SubmitRequest(String form, List<AnswerItem> answers) {
    }

    record TaskDoneRequest(Boolean done) {
    }

    // ── 营 ──────────────────────────────────────────────────────────

    /** **计费**：一次上游调用。按钮上必须写明。 */
    @PostMapping
    ApiResponse<CampService.Created> create(@RequestBody CreateRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.resumeDocumentId() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "请选择一份简历");
        }
        return ApiResponse.ok(camps.create(userId, request.resumeDocumentId(),
                request.days() == null ? 0 : request.days()));
    }

    @GetMapping
    ApiResponse<List<CampService.ProgramSummary>> list() {
        return ApiResponse.ok(camps.list(AiRequester.require()));
    }

    @GetMapping("/{id}")
    ApiResponse<CampService.ProgramSummary> get(@PathVariable("id") long id) {
        return ApiResponse.ok(camps.get(AiRequester.require(), id));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable("id") long id) {
        camps.delete(AiRequester.require(), id);
        return ApiResponse.ok(null);
    }

    // ── 卷与作答 ────────────────────────────────────────────────────

    /** 交卷前也不含答案与解析：返回类型里本来就没有这两个字段。 */
    @GetMapping("/{id}/items")
    ApiResponse<List<CampService.ItemView>> items(@PathVariable("id") long id,
                                                  @RequestParam("form") String form) {
        return ApiResponse.ok(camps.items(AiRequester.require(), id, form));
    }

    @PostMapping("/{id}/submit")
    ApiResponse<CampService.ResultView> submit(@PathVariable("id") long id,
                                               @RequestBody SubmitRequest request) {
        long userId = AiRequester.require();
        if (request == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少作答内容");
        }
        List<CampService.AnswerInput> answers = request.answers() == null ? List.of()
                : request.answers().stream()
                        .map(item -> new CampService.AnswerInput(item.itemId(), item.choice()))
                        .toList();
        return ApiResponse.ok(camps.submit(userId, id, request.form(), answers));
    }

    @GetMapping("/{id}/result")
    ApiResponse<CampService.ResultView> result(@PathVariable("id") long id,
                                               @RequestParam("form") String form) {
        return ApiResponse.ok(camps.result(AiRequester.require(), id, form));
    }

    /** **计费**：一次上游调用，且会新增一份复测卷。 */
    @PostMapping("/{id}/retest")
    ApiResponse<CampService.Created> retest(@PathVariable("id") long id) {
        return ApiResponse.ok(camps.retest(AiRequester.require(), id));
    }

    // ── 计划 ────────────────────────────────────────────────────────

    /** **免费**：纯函数排期，不调模型。 */
    @PostMapping("/{id}/plan")
    ApiResponse<CampService.PlanView> buildPlan(@PathVariable("id") long id) {
        return ApiResponse.ok(camps.buildPlan(AiRequester.require(), id));
    }

    @GetMapping("/{id}/plan")
    ApiResponse<CampService.PlanView> plan(@PathVariable("id") long id) {
        return ApiResponse.ok(camps.currentPlan(AiRequester.require(), id));
    }

    @PatchMapping("/{id}/tasks/{taskId}")
    ApiResponse<Boolean> markTask(@PathVariable("id") long id,
                                  @PathVariable("taskId") long taskId,
                                  @RequestBody TaskDoneRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.done() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少 done 字段");
        }
        return ApiResponse.ok(camps.markTask(userId, id, taskId, request.done()));
    }
}
