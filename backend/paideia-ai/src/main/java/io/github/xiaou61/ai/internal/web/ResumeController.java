package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.internal.resume.ResumeCheckup;
import io.github.xiaou61.ai.internal.resume.ResumeDocumentService;
import io.github.xiaou61.ai.internal.resume.ResumeImportService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历的导入、编辑与体检端点（WORK-018 TASK-004/005/006）。
 *
 * <p><b>全部 {@code AUTHENTICATED}，没有一个 {@code PUBLIC_READ}</b>：简历是本项目最敏的 PII，
 * 不存在"拿到链接就能看"的路径（REQ-023）。归属一律取自令牌，请求里没有用户标识可传。
 *
 * <p>路径在 {@code /api/v1/ai/} 下而不是 {@code /admin/}：它只对本人开放，
 * 而不是管理员操作面。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/resumes")
class ResumeController {

    /**
     * 上传上限。
     *
     * <p>与 {@code spring.servlet.multipart.max-file-size}（12 MB）配合：应用层先给一个更小的
     * 上限，用户拿到的是"简历超过 10 MB"这句人话，而不是容器抛出的截断错误。
     */
    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;

    private final ResumeImportService imports;
    private final ResumeDocumentService documents;

    ResumeController(ResumeImportService imports, ResumeDocumentService documents) {
        this.imports = imports;
        this.documents = documents;
    }

    // ── 视图 ────────────────────────────────────────────────────────

    /**
     * 草稿视图。
     *
     * <p><b>带 plainText</b>：它是事实闸门的比对基准（`resume_documents.plain_text`），
     * 保存时必须一起回传。不带的话，用户在核对页把识别错的一行改掉之后，闸门就失去了
     * "这句话在原件里到底有没有"的唯一凭据。
     *
     * <p>它出现在浏览器里不构成新的暴露：这份文本本来就是用户刚上传的那份文件。
     */
    record DraftView(ResumeData data, String plainText, int dropped, int truncatedFields,
                     int truncatedChars, int pages) {
    }

    record DocumentSummaryView(long id, String title, boolean isDefault, int revision, Instant updatedAt) {
        static DocumentSummaryView of(ResumeDocumentService.Summary summary) {
            return new DocumentSummaryView(summary.id(), summary.title(), summary.isDefault(),
                    summary.revision(), summary.updatedAt());
        }
    }

    record DocumentView(long id, String title, boolean isDefault, int revision,
                        ResumeData data, Instant updatedAt) {
        static DocumentView of(ResumeDocumentService.Detail detail) {
            return new DocumentView(detail.id(), detail.title(), detail.isDefault(), detail.revision(),
                    detail.data(), detail.updatedAt());
        }
    }

    record HistoryView(long id, Instant createdAt) {
    }

    record CreateRequest(String title, ResumeData data, String plainText) {
    }

    /** 保存请求。{@code revision} 必填——它是乐观锁，缺了就无法判断冲突。 */
    record SaveRequest(String title, ResumeData data, int revision) {
    }

    record RenameRequest(String title) {
    }

    record CheckupView(boolean ok, List<ResumeCheckup.Item> items) {
    }

    // ── 导入 ────────────────────────────────────────────────────────

    /**
     * 上传并解析，返回**未落库**的草稿。
     *
     * <p>草稿不落库是需求（REQ-003）：用户要在核对页确认之后才创建主简历。
     */
    @PostMapping(path = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<DraftView> parse(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "请选择要上传的简历文件");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "简历超过 " + (MAX_UPLOAD_BYTES / 1024 / 1024) + " MB 上限");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new BizException(ErrorCode.INTERNAL, "读取上传文件失败");
        }
        ResumeImportService.Draft draft = imports.parse(AiRequester.require(), content, file.getOriginalFilename());
        return ApiResponse.ok(new DraftView(draft.data(), draft.plainText(), draft.dropped(),
                draft.truncatedFields(), draft.truncatedChars(), draft.pages()));
    }

    // ── 主简历 ──────────────────────────────────────────────────────

    @GetMapping
    ApiResponse<List<DocumentSummaryView>> list() {
        return ApiResponse.ok(documents.list(AiRequester.require()).stream()
                .map(DocumentSummaryView::of).toList());
    }

    @PostMapping
    ApiResponse<DocumentView> create(@RequestBody CreateRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.data() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少简历内容");
        }
        long id = documents.create(userId, request.title(), request.data(), request.plainText());
        return ApiResponse.ok(DocumentView.of(documents.get(userId, id)));
    }

    @GetMapping("/{id}")
    ApiResponse<DocumentView> get(@PathVariable("id") long id) {
        return ApiResponse.ok(DocumentView.of(documents.get(AiRequester.require(), id)));
    }

    /** 整份保存（自动保存用它）。冲突时返回 409，由界面给用户选。 */
    @PutMapping("/{id}")
    ApiResponse<DocumentView> save(@PathVariable("id") long id, @RequestBody SaveRequest request) {
        long userId = AiRequester.require();
        if (request == null || request.data() == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少简历内容");
        }
        return ApiResponse.ok(DocumentView.of(
                documents.save(userId, id, request.title(), request.data(), request.revision())));
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable("id") long id) {
        documents.delete(AiRequester.require(), id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/title")
    ApiResponse<DocumentSummaryView> rename(@PathVariable("id") long id,
                                            @RequestBody RenameRequest request) {
        long userId = AiRequester.require();
        String title = request == null ? null : request.title();
        return ApiResponse.ok(DocumentSummaryView.of(documents.rename(userId, id, title)));
    }

    @PutMapping("/{id}/default")
    ApiResponse<DocumentSummaryView> setDefault(@PathVariable("id") long id) {
        long userId = AiRequester.require();
        return ApiResponse.ok(DocumentSummaryView.of(documents.setDefault(userId, id)));
    }

    // ── 历史与体检 ──────────────────────────────────────────────────

    @GetMapping("/{id}/history")
    ApiResponse<List<HistoryView>> history(@PathVariable("id") long id) {
        return ApiResponse.ok(documents.history(AiRequester.require(), id).stream()
                .map(item -> new HistoryView(item.id(), item.createdAt())).toList());
    }

    /** 预览某一版。返回结构化数据，界面上按只读方式渲染。 */
    @GetMapping("/{id}/history/{historyId}")
    ApiResponse<ResumeData> historyData(@PathVariable("id") long id,
                                        @PathVariable("historyId") long historyId) {
        return ApiResponse.ok(documents.historyData(AiRequester.require(), id, historyId));
    }

    /**
     * 回滚到某一版。
     *
     * <p>带 {@code revision}：回滚也是一次普通保存，同样要过乐观锁——否则两个标签页
     * 可以一边回滚一边保存，最后结果谁都不认识。
     */
    @PostMapping("/{id}/history/{historyId}/restore")
    ApiResponse<DocumentView> restore(@PathVariable("id") long id,
                                      @PathVariable("historyId") long historyId,
                                      @RequestParam("revision") int revision) {
        long userId = AiRequester.require();
        return ApiResponse.ok(DocumentView.of(documents.restore(userId, id, historyId, revision)));
    }

    /** 体检清单。纯代码，不计费、不调用模型。 */
    @GetMapping("/{id}/checkup")
    ApiResponse<CheckupView> checkup(@PathVariable("id") long id) {
        List<ResumeCheckup.Item> items = documents.checkup(AiRequester.require(), id);
        boolean allOk = items.stream().allMatch(ResumeCheckup.Item::ok);
        return ApiResponse.ok(new CheckupView(allOk, items));
    }
}
