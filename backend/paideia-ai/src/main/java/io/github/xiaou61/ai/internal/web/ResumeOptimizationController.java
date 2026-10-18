package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.internal.resume.ResumeDocumentService;
import io.github.xiaou61.ai.internal.resume.ResumeKeywordClassifier;
import io.github.xiaou61.ai.internal.resume.ResumeOptimizationService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位定制的端点（WORK-018 TASK-009）。
 *
 * <p>四个动作按"花了多少钱"分成两组：
 * <ul>
 *   <li><b>免费</b>：建版本、改采纳状态、定稿（只跑确定性闸门）。</li>
 *   <li><b>计费</b>：分析与提案（各自一次模型调用）。界面必须在按钮上写明。</li>
 * </ul>
 *
 * <p>归属与另外几个端点一致：路径里没有用户标识，一律取自令牌，拿不到统一 404。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai")
class ResumeOptimizationController {

    private final ResumeOptimizationService optimizations;

    ResumeOptimizationController(ResumeOptimizationService optimizations) {
        this.optimizations = optimizations;
    }

    // ── 视图 ────────────────────────────────────────────────────────

    record CreateVersionRequest(String company, String role, String jdText) {
    }

    record VersionView(long id, String company, String role, String status,
                       int proposalCount, int acceptedCount, Instant createdAt) {
        static VersionView of(ResumeOptimizationService.VersionSummary summary) {
            return new VersionView(summary.id(), summary.company(), summary.role(), summary.status(),
                    summary.proposalCount(), summary.acceptedCount(), summary.createdAt());
        }
    }

    /**
     * 分析视图。
     *
     * <p>`classified` 里的每条都带 `bucket`（existing / evidenced / gap）与 `hit`（命中位置）。
     * 界面按桶分组展示；`covered` / `total` 是关键词覆盖面，**不是录用概率**。
     */
    record AnalysisView(String summary, List<ClassifiedView> classified, int covered, int total) {
        static AnalysisView of(ResumeOptimizationService.AnalysisView view) {
            return new AnalysisView(view.summary(), view.classified().stream().map(ClassifiedView::of).toList(),
                    view.covered(), view.total());
        }
    }

    record ClassifiedView(String term, String quote, String bucket, String hit) {
        static ClassifiedView of(ResumeKeywordClassifier.Classified classified) {
            return new ClassifiedView(classified.term(), classified.quote(), classified.bucket(),
                    classified.hit());
        }
    }

    record ProposalView(long id, int seq, String entryRef, String field, String originalText,
                        String proposedText, String changeType, String rationale,
                        List<String> coversKeywords, boolean accepted, String gateStatus,
                        List<String> gateViolations) {
        static ProposalView of(ResumeOptimizationService.ProposalView view) {
            return new ProposalView(view.id(), view.seq(), view.entryRef(), view.field(),
                    view.originalText(), view.proposedText(), view.changeType(), view.rationale(),
                    view.coversKeywords(), view.accepted(), view.gateStatus(), view.gateViolations());
        }
    }

    record VersionDetailView(long id, long documentId, String company, String role, String status,
                            AnalysisView analysis, ResumeData finalData, Instant createdAt) {
        static VersionDetailView of(ResumeOptimizationService.VersionDetail detail) {
            return new VersionDetailView(detail.id(), detail.documentId(), detail.company(),
                    detail.role(), detail.status(),
                    detail.analysis() == null ? null : AnalysisView.of(detail.analysis()),
                    detail.finalData(), detail.createdAt());
        }
    }

    /** 采纳请求。两个字段都可空：只切状态、或只改文本。 */
    record UpdateProposalRequest(Boolean accepted, String proposedText) {
    }

    // ── 端点 ────────────────────────────────────────────────────────

    /** 建版本（**免费**）：只把 JD 存下来，准备好一个容器。 */
    @PostMapping("/resumes/{documentId}/versions")
    ApiResponse<Long> createVersion(@PathVariable("documentId") long documentId,
                                    @RequestBody CreateVersionRequest request) {
        long userId = AiRequester.require();
        if (request == null) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "缺少 JD 内容");
        }
        return ApiResponse.ok(optimizations.createVersion(userId, documentId,
                request.company(), request.role(), request.jdText()));
    }

    @GetMapping("/resumes/{documentId}/versions")
    ApiResponse<List<VersionView>> listVersions(@PathVariable("documentId") long documentId) {
        return ApiResponse.ok(optimizations.listVersions(AiRequester.require(), documentId).stream()
                .map(VersionView::of).toList());
    }

    @DeleteMapping("/versions/{versionId}")
    ApiResponse<Void> deleteVersion(@PathVariable("versionId") long versionId) {
        optimizations.deleteVersion(AiRequester.require(), versionId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/versions/{versionId}")
    ApiResponse<VersionDetailView> getVersion(@PathVariable("versionId") long versionId) {
        return ApiResponse.ok(VersionDetailView.of(optimizations.getVersion(AiRequester.require(), versionId)));
    }

    /** 分析（**计费**）。重复调用会再花一次积分——界面必须写明。 */
    @PostMapping("/versions/{versionId}/analysis")
    ApiResponse<AnalysisView> analyze(@PathVariable("versionId") long versionId) {
        return ApiResponse.ok(AnalysisView.of(optimizations.analyze(AiRequester.require(), versionId)));
    }

    /** 生成提案（**计费**）。旧提案会被这一批替换。 */
    @PostMapping("/versions/{versionId}/proposals")
    ApiResponse<List<ProposalView>> propose(@PathVariable("versionId") long versionId) {
        return ApiResponse.ok(optimizations.propose(AiRequester.require(), versionId).stream()
                .map(ProposalView::of).toList());
    }

    @GetMapping("/versions/{versionId}/proposals")
    ApiResponse<List<ProposalView>> listProposals(@PathVariable("versionId") long versionId) {
        return ApiResponse.ok(optimizations.listProposals(AiRequester.require(), versionId).stream()
                .map(ProposalView::of).toList());
    }

    /**
     * 采纳/取消采纳，或改"改后文本"。
     *
     * <p>改文本会**重跑该条的闸门**：不重跑的话，"编辑改后文本"就成了绕过闸门的后门
     * ——先采纳一条合规的，再把文字改成不合规的。
     */
    @PatchMapping("/proposals/{proposalId}")
    ApiResponse<ProposalView> updateProposal(@PathVariable("proposalId") long proposalId,
                                             @RequestBody(required = false) UpdateProposalRequest request) {
        UpdateProposalRequest body = request == null ? new UpdateProposalRequest(null, null) : request;
        return ApiResponse.ok(ProposalView.of(optimizations.updateProposal(
                AiRequester.require(), proposalId, body.accepted(), body.proposedText())));
    }

    /** 定稿（**免费**）：全量闸门 → 合成 → finalized。任何一条不通过则整次被拒。 */
    @PostMapping("/versions/{versionId}/finalize")
    ApiResponse<VersionDetailView> finalize(@PathVariable("versionId") long versionId) {
        return ApiResponse.ok(VersionDetailView.of(
                optimizations.finalizeVersion(AiRequester.require(), versionId)));
    }
}
