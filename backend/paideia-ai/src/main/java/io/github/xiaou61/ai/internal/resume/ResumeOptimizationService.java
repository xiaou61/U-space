package io.github.xiaou61.ai.internal.resume;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.ResumeAnalysis;
import io.github.xiaou61.ai.ResumeAnalyzer;
import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.ai.ResumeProposal;
import io.github.xiaou61.ai.ResumeProposer;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.billing.CreditService;
import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位定制的编排（WORK-018 TASK-009）。
 *
 * <p>一次求职版本的生命周期：
 * <pre>
 * 建版本（免费）→ 分析（计费 #2）→ 提案（计费 #3）→ 逐条采纳 → 定稿（免费，跑闸门）
 * </pre>
 *
 * <p>三条设计取舍：
 * <ol>
 *   <li><b>定稿不调用模型</b>：定稿只是"把已采纳的提案合成成一份内容 + 跑闸门"，
 *       两件事都是确定性的。让它免费，用户才敢在"再想想"和"定稿"之间反复。</li>
 *   <li><b>采纳状态即时落库</b>：刷新、换设备都要保持（REQ-016）。挂在内存里的话，
 *       "逐项采纳"就变成一个随时会丢的临时状态。</li>
 *   <li><b>重新生成先删后插</b>：同一版本只保留一批提案。删除在同一事务里，
 *       中途失败不会留下"新旧两批混在一起"的状态。</li>
 * </ol>
 */
@Service
public class ResumeOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(ResumeOptimizationService.class);

    /** JD 最短长度。太短的输入会让"分析"变成猜测。 */
    static final int MIN_JD_CHARS = 30;

    /** JD 最长长度：一份 JD 通常几百到几千字，15000 已远超正常范围。 */
    static final int MAX_JD_CHARS = 15_000;

    private final ResumeDocumentMapper documents;
    private final ResumeChildMapper children;
    private final ResumeDocumentService documentService;
    private final ResumeAnalyzer analyzer;
    private final ResumeProposer proposer;
    private final ResumeKeywordClassifier classifier;
    private final ResumeGate gate;
    private final ResumeJson json;
    private final CredentialService credentials;
    private final CreditService credits;
    private final io.github.xiaou61.ai.RunRecorder recorder;

    private static final ObjectMapper JSON = new ObjectMapper();

    ResumeOptimizationService(ResumeDocumentMapper documents, ResumeChildMapper children,
                              ResumeDocumentService documentService, ResumeAnalyzer analyzer,
                              ResumeProposer proposer, ResumeKeywordClassifier classifier,
                              ResumeGate gate, ResumeJson json, CredentialService credentials,
                              CreditService credits, io.github.xiaou61.ai.RunRecorder recorder) {
        this.documents = documents;
        this.children = children;
        this.documentService = documentService;
        this.analyzer = analyzer;
        this.proposer = proposer;
        this.classifier = classifier;
        this.gate = gate;
        this.json = json;
        this.credentials = credentials;
        this.credits = credits;
        this.recorder = recorder;
    }

    // ── 视图 ────────────────────────────────────────────────────────

    public record VersionSummary(long id, String company, String role, String status,
                                 int proposalCount, int acceptedCount, Instant createdAt) {
    }

    public record VersionDetail(long id, long documentId, String company, String role,
                                String status, AnalysisView analysis, ResumeData finalData,
                                Instant createdAt) {
    }

    /** 分析视图：三桶 + 摘要。桶名与前端约定一致（existing / evidenced / gap）。 */
    public record AnalysisView(String summary, List<ResumeKeywordClassifier.Classified> classified,
                               int covered, int total) {
    }

    public record ProposalView(long id, int seq, String entryRef, String field,
                               String originalText, String proposedText, String changeType,
                               String rationale, List<String> coversKeywords,
                               boolean accepted, String gateStatus, List<String> gateViolations) {
    }

    // ── 建版本 ──────────────────────────────────────────────────────

    /**
     * 建一个草稿版本（免费）。它只是"把 JD 存下来、准备好一个容器"。
     *
     * <p>先建版本再分析，是因为分析与提案都要往这个容器里写；而"用户可能只是点开看看"这条
     * 路径不该被计费。
     */
    @Transactional
    public long createVersion(long userId, long documentId, String company, String role, String jdText) {
        documentService.require(userId, documentId);
        String jd = normalizeJd(jdText);
        Instant now = Instant.now();
        ResumeChildMapper.VersionRow row = new ResumeChildMapper.VersionRow();
        row.setDocumentId(documentId);
        row.setUserId(userId);
        row.setCompany(trim(company, 120));
        row.setRole(trim(role, 120));
        row.setJdText(jd);
        row.setStatus("draft");
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        children.insertVersion(row);
        log.info("ai.resume.version_created userId={} documentId={} versionId={}",
                userId, documentId, row.getId());
        return row.getId();
    }

    // ── 分析 ────────────────────────────────────────────────────────

    /**
     * 跑分析（计费 #2）。结果写进版本的 `analysis_json`，同时返回三桶分类。
     *
     * <p>三桶是**代码算的**：模型只负责列 JD 要求，分桶由 {@link ResumeKeywordClassifier}
     * 用主简历原文做字符串查找。理由见该类的注释——模型自评"他会不会"是幻觉温床。
     */
    @Transactional
    public AnalysisView analyze(long userId, long versionId) {
        ResumeChildMapper.VersionRow version = requireVersion(userId, versionId);
        if (!"draft".equals(version.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "这个版本已经定稿，不能再重新分析");
        }
        ResumeDocumentService.Detail document = documentService.get(userId, version.getDocumentId());
        ModelAccess access = requireAccess(userId);

        long reserved = credits.reserve(userId, credits.estimate());
        TokenUsage usage = null;
        var run = recorder.begin(io.github.xiaou61.ai.RunSpec
                .of(io.github.xiaou61.ai.RunKind.RESUME_OPTIMIZE, userId)
                .withModel(access.model()));
        try {
            ResumeAnalyzer.Analyzed analyzed = analyzer.analyze(document.data(), version.getJdText(), access);
            usage = analyzed.usage();
            List<ResumeKeywordClassifier.Classified> classified =
                    classifier.classify(analyzed.analysis(), document.data(), plainTextOf(userId, version));

            int total = classified.size();
            int covered = (int) classified.stream()
                    .filter(item -> !ResumeKeywordClassifier.BUCKET_GAP.equals(item.bucket()))
                    .count();
            AnalysisView view = new AnalysisView(analyzed.analysis().summary(), classified, covered, total);

            children.updateAnalysis(versionId, userId, writeAnalysis(version, analyzed.analysis(), classified, view),
                    Instant.now());
            int charged = credits.settleResume(userId, reserved, access.model(),
                    usage == null ? null : usage.inputTokens(),
                    usage == null ? null : usage.outputTokens(), versionId);
            run.tokens(usage == null ? 0 : usage.inputTokens(), usage == null ? 0 : usage.outputTokens(), 0);
            run.addCredits(charged);
            run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
            log.info("ai.resume.analyzed userId={} versionId={} keywords={} gaps={}",
                    userId, versionId, total, total - covered);
            return view;
        } catch (RuntimeException failure) {
            settleOrRefund(userId, reserved, access, usage, "简历岗位分析失败");
            run.finish(io.github.xiaou61.ai.RunOutcome.FAILED, "resume_analyze_failed");
            throw failure;
        } finally {
            run.close();
        }
    }

    // ── 提案 ────────────────────────────────────────────────────────

    /**
     * 生成提案（计费 #3）。**先删旧提案再写新的**，同一个事务。
     *
     * <p>返回的提案全部 `accepted=false`（REQ-015：默认不采纳）。用户主动勾选才进定稿——
     * 这条是产品主张，不是实现细节："一键全采纳"会把"AI 优化"退化成"AI 决定"。
     */
    @Transactional
    public List<ProposalView> propose(long userId, long versionId) {
        ResumeChildMapper.VersionRow version = requireVersion(userId, versionId);
        if (!"draft".equals(version.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "这个版本已经定稿，不能再重新生成提案");
        }
        if (version.getAnalysisJson() == null || version.getAnalysisJson().isBlank()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "请先做一次岗位分析，再生成改写提案");
        }
        ResumeDocumentService.Detail document = documentService.get(userId, version.getDocumentId());
        ModelAccess access = requireAccess(userId);
        AnalysisView analysis = readAnalysis(version.getAnalysisJson());
        List<String> gaps = analysis.classified().stream()
                .filter(item -> ResumeKeywordClassifier.BUCKET_GAP.equals(item.bucket()))
                .map(ResumeKeywordClassifier.Classified::term)
                .toList();
        ResumeAnalysis analysisForModel = new ResumeAnalysis(
                analysis.classified().stream()
                        .map(item -> new ResumeAnalysis.Requirement(item.term(), item.quote()))
                        .toList(),
                analysis.summary());

        long reserved = credits.reserve(userId, credits.estimate());
        TokenUsage usage = null;
        var run = recorder.begin(io.github.xiaou61.ai.RunSpec
                .of(io.github.xiaou61.ai.RunKind.RESUME_OPTIMIZE, userId)
                .withModel(access.model()));
        try {
            ResumeProposer.Proposed proposed = proposer.propose(document.data(), version.getJdText(),
                    analysisForModel, gaps, access);
            usage = proposed.usage();

            children.deleteProposals(versionId, userId);
            Instant now = Instant.now();
            int seq = 1;
            for (ResumeProposal proposal : proposed.proposals()) {
                ResumeChildMapper.ProposalRow row = toRow(userId, versionId, seq++, proposal, now);
                children.insertProposal(row);
            }
            int charged = credits.settleResume(userId, reserved, access.model(),
                    usage == null ? null : usage.inputTokens(),
                    usage == null ? null : usage.outputTokens(), versionId);
            run.tokens(usage == null ? 0 : usage.inputTokens(), usage == null ? 0 : usage.outputTokens(), 0);
            run.addCredits(charged);
            run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
            log.info("ai.resume.proposed userId={} versionId={} proposals={} dropped={}",
                    userId, versionId, proposed.proposals().size(), proposed.dropped());
            return listProposals(userId, versionId);
        } catch (RuntimeException failure) {
            settleOrRefund(userId, reserved, access, usage, "简历改写提案失败");
            run.finish(io.github.xiaou61.ai.RunOutcome.FAILED, "resume_propose_failed");
            throw failure;
        } finally {
            run.close();
        }
    }

    // ── 采纳 ────────────────────────────────────────────────────────

    /**
     * 采纳/取消采纳，必要时同时改"改后文本"。
     *
     * <p>改文本后**必须重跑该条的闸门**（并写 `gate_status`）——否则用户可以借"编辑改后文本"
     * 绕过闸门：用一条合规提案进定稿，再把文字改成不合规的。
     *
     * @param proposedText 为空表示"只切采纳状态，不改文本"
     */
    @Transactional
    public ProposalView updateProposal(long userId, long proposalId, Boolean accepted, String proposedText) {
        ResumeChildMapper.ProposalRow row = children.findProposal(proposalId, userId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提案不存在");
        }
        ResumeChildMapper.VersionRow version = requireVersion(userId, row.getVersionId());
        if (!"draft".equals(version.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "这个版本已经定稿，不能再改提案");
        }

        Instant now = Instant.now();
        boolean textChanged = proposedText != null && !proposedText.isBlank()
                && !proposedText.equals(row.getProposedText());
        if (textChanged) {
            row.setProposedText(proposedText.length() > 2000
                    ? proposedText.substring(0, 2000) : proposedText);
        }
        if (accepted != null) {
            if (accepted) {
                requireApplicable(row);
            }
            row.setAccepted(accepted);
        }

        // 文本变了就重跑闸门；没变就保留上次的判定（避免每次点采纳都重算一遍）
        if (textChanged) {
            ResumeChildMapper.VersionRow currentVersion = version;
            ResumeDocumentService.Detail document = documentService.get(userId, currentVersion.getDocumentId());
            AnalysisView analysis = readAnalysis(currentVersion.getAnalysisJson());
            List<String> gaps = analysis.classified().stream()
                    .filter(item -> ResumeKeywordClassifier.BUCKET_GAP.equals(item.bucket()))
                    .map(ResumeKeywordClassifier.Classified::term)
                    .toList();
            ResumeProposal probe = new ResumeProposal(row.getEntryRef(), row.getField(),
                    row.getOriginalText(), row.getProposedText(), row.getChangeType(),
                    row.getRationale(), List.of());
            List<ResumeGate.Violation> violations =
                    gate.check(document.data(), plainTextOf(userId, currentVersion), List.of(probe), gaps);
            if (violations.isEmpty()) {
                row.setGateStatus("passed");
                row.setGateViolations(null);
            } else {
                row.setGateStatus("failed");
                row.setGateViolations(writeViolations(violations));
                // 闸门不通过时**强制取消采纳**：留着"已采纳但不合规"是一个自相矛盾的状态
                row.setAccepted(false);
            }
        }
        row.setUpdatedAt(now);
        children.updateProposal(row);
        return toView(row);
    }

    /**
     * 只允许采纳"定稿时真的能落地"的提案。
     *
     * <p>{@code compose} 目前只应用 {@code work[...]} / {@code projects[...].bullets[...]} 的
     * {@code rewrite}。提示词却允许模型产出 {@code add} / {@code remove} / {@code reorder}、
     * 整条 {@code work[i]} 引用与 {@code skills[i].items[j]}——不拦的话，用户点了"采纳"、
     * 定稿里却没有这条改动，是静默的数据丢失。这里在采纳这一步就明说。
     */
    private static void requireApplicable(ResumeChildMapper.ProposalRow row) {
        String ref = row.getEntryRef() == null ? "" : row.getEntryRef();
        boolean rewrite = "rewrite".equals(row.getChangeType());
        boolean covered = ref.startsWith("work[") || ref.startsWith("projects[");
        if (!rewrite || !covered) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "这条提案的改动类型本期无法自动应用（只支持改写工作经历/项目要点），不能采纳");
        }
    }

    // ── 定稿 ────────────────────────────────────────────────────────

    /**
     * 定稿：全量闸门 → 合成内容 → `status=finalized`。
     *
     * <p>**免费**（不调模型）。任何一条已采纳提案的闸门不通过，整次定稿就被拒——
     * 不可能"定稿了一半"。
     */
    @Transactional
    public VersionDetail finalizeVersion(long userId, long versionId) {
        ResumeChildMapper.VersionRow version = requireVersion(userId, versionId);
        if (!"draft".equals(version.getStatus())) {
            throw new BizException(ErrorCode.CONFLICT, "这个版本已经定稿");
        }
        ResumeDocumentService.Detail document = documentService.get(userId, version.getDocumentId());
        List<ResumeChildMapper.ProposalRow> rows = children.listProposals(versionId, userId);
        List<ResumeChildMapper.ProposalRow> accepted = rows.stream().filter(ResumeChildMapper.ProposalRow::isAccepted).toList();
        if (accepted.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "还没有采纳任何提案，无法定稿");
        }

        AnalysisView analysis = readAnalysis(version.getAnalysisJson());
        List<String> gaps = analysis.classified().stream()
                .filter(item -> ResumeKeywordClassifier.BUCKET_GAP.equals(item.bucket()))
                .map(ResumeKeywordClassifier.Classified::term)
                .toList();

        // 全量闸门：重新跑一遍（用户可能在采纳之后又改过文本）
        List<ResumeProposal> probe = accepted.stream()
                .map(row -> new ResumeProposal(row.getEntryRef(), row.getField(), row.getOriginalText(),
                        row.getProposedText(), row.getChangeType(), row.getRationale(), List.of()))
                .toList();
        List<ResumeGate.Violation> violations =
                gate.check(document.data(), plainTextOf(userId, version), probe, gaps);
        if (!violations.isEmpty()) {
            // 把违规写回对应提案：界面能直接定位到那一条
            for (ResumeGate.Violation violation : violations) {
                ResumeChildMapper.ProposalRow row = accepted.get(violation.proposalIndex());
                row.setGateStatus("failed");
                row.setGateViolations(writeViolations(List.of(violation)));
                row.setAccepted(false);
                row.setUpdatedAt(Instant.now());
                children.updateProposal(row);
            }
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "有 " + violations.size() + " 条采纳的提案没有通过事实校验，已标为不可采纳");
        }

        ResumeData finalData = compose(document.data(), accepted);
        Instant now = Instant.now();
        if (children.finalize(versionId, userId, json.write(finalData), now) == 0) {
            throw new BizException(ErrorCode.CONFLICT, "这个版本已经定稿");
        }
        log.info("ai.resume.finalized userId={} versionId={} accepted={}", userId, versionId, accepted.size());
        return getVersion(userId, versionId);
    }

    /**
     * 把已采纳的提案合成成定稿内容。
     *
     * <p>只处理 {@code work[...]} / {@code projects[...].bullets[...]} 的 {@code rewrite}。
     * 其余类型（{@code add} / {@code remove} / {@code reorder}、整条引用、{@code skills}）
     * 一期不落地——{@link #requireApplicable} 会在采纳时就拒绝它们，所以走到这里的一定能应用。
     */
    private ResumeData compose(ResumeData source, List<ResumeChildMapper.ProposalRow> accepted) {
        List<ResumeData.Work> work = new ArrayList<>(source.work());
        List<ResumeData.Project> projects = new ArrayList<>(source.projects());

        for (ResumeChildMapper.ProposalRow row : accepted) {
            String ref = row.getEntryRef();
            if ("rewrite".equals(row.getChangeType())) {
                applyRewrite(work, projects, ref, row.getProposedText());
            }
            // remove 一期不实现：删掉一条要点会让"条目数不得为 0"的结构检查产生歧义
        }
        return new ResumeData(source.basics(), source.intent(), source.education(), work, projects,
                source.skills(), source.awards(), source.selfEvaluation());
    }

    private void applyRewrite(List<ResumeData.Work> work, List<ResumeData.Project> projects,
                              String ref, String newText) {
        try {
            if (ref.startsWith("work[")) {
                int close = ref.indexOf(']');
                int index = Integer.parseInt(ref.substring("work[".length(), close));
                if (index < 0 || index >= work.size()) {
                    return;
                }
                ResumeData.Work item = work.get(index);
                String suffix = ref.substring(close + 1);
                if (".summary".equals(suffix)) {
                    work.set(index, new ResumeData.Work(item.company(), item.title(), item.start(),
                            item.end(), newText, item.bullets()));
                } else if (suffix.startsWith(".bullets[")) {
                    int bulletClose = suffix.indexOf(']', ".bullets[".length());
                    int bulletIndex = Integer.parseInt(suffix.substring(".bullets[".length(), bulletClose));
                    List<String> bullets = new ArrayList<>(item.bullets());
                    if (bulletIndex >= 0 && bulletIndex < bullets.size()) {
                        bullets.set(bulletIndex, newText);
                        work.set(index, new ResumeData.Work(item.company(), item.title(), item.start(),
                                item.end(), item.summary(), bullets));
                    }
                }
                return;
            }
            if (ref.startsWith("projects[")) {
                int close = ref.indexOf(']');
                int index = Integer.parseInt(ref.substring("projects[".length(), close));
                if (index < 0 || index >= projects.size()) {
                    return;
                }
                ResumeData.Project item = projects.get(index);
                String suffix = ref.substring(close + 1);
                if (suffix.startsWith(".bullets[")) {
                    int bulletClose = suffix.indexOf(']', ".bullets[".length());
                    int bulletIndex = Integer.parseInt(suffix.substring(".bullets[".length(), bulletClose));
                    List<String> bullets = new ArrayList<>(item.bullets());
                    if (bulletIndex >= 0 && bulletIndex < bullets.size()) {
                        bullets.set(bulletIndex, newText);
                        projects.set(index, new ResumeData.Project(item.name(), item.role(),
                                item.period(), bullets));
                    }
                }
            }
        } catch (RuntimeException exception) {
            // 位置失效（用户可能改过主简历）→ 跳过这一条，不整体失败：
            // 其余提案仍然有效，而"某一条落不上"比"整次定稿失败"更符合用户预期
            log.warn("ai.resume.compose_skipped ref={}", ref);
        }
    }

    // ── 读 ──────────────────────────────────────────────────────────

    public List<VersionSummary> listVersions(long userId, long documentId) {
        documentService.require(userId, documentId);
        List<VersionSummary> summaries = new ArrayList<>();
        for (ResumeChildMapper.VersionRow version : children.listVersions(documentId, userId)) {
            List<ResumeChildMapper.ProposalRow> proposals = children.listProposals(version.getId(), userId);
            summaries.add(new VersionSummary(version.getId(), version.getCompany(), version.getRole(),
                    version.getStatus(), proposals.size(),
                    (int) proposals.stream().filter(ResumeChildMapper.ProposalRow::isAccepted).count(),
                    version.getCreatedAt()));
        }
        return List.copyOf(summaries);
    }

    public VersionDetail getVersion(long userId, long versionId) {
        ResumeChildMapper.VersionRow version = requireVersion(userId, versionId);
        AnalysisView analysis = version.getAnalysisJson() == null || version.getAnalysisJson().isBlank()
                ? null : readAnalysis(version.getAnalysisJson());
        ResumeData finalData = version.getFinalJson() == null || version.getFinalJson().isBlank()
                ? null : json.parseStored(version.getFinalJson());
        return new VersionDetail(version.getId(), version.getDocumentId(), version.getCompany(),
                version.getRole(), version.getStatus(), analysis, finalData, version.getCreatedAt());
    }

    public List<ProposalView> listProposals(long userId, long versionId) {
        requireVersion(userId, versionId);
        return children.listProposals(versionId, userId).stream().map(ResumeOptimizationService::toView).toList();
    }

    @Transactional
    public void deleteVersion(long userId, long versionId) {
        requireVersion(userId, versionId);
        children.deleteProposals(versionId, userId);
        children.deleteVersion(versionId, userId);
        log.info("ai.resume.version_deleted userId={} versionId={}", userId, versionId);
    }

    // ── 内部 ────────────────────────────────────────────────────────

    ResumeChildMapper.VersionRow requireVersion(long userId, long versionId) {
        ResumeChildMapper.VersionRow version = children.findVersion(versionId, userId);
        if (version == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "版本不存在");
        }
        return version;
    }

    private ModelAccess requireAccess(long userId) {
        Optional<ModelAccess> access = credentials.access(userId);
        if (access.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "平台尚未配置 AI 服务，请联系管理员");
        }
        return access.get();
    }

    /** 主简历原文；用户手工新建的简历没有原文，此时返回空串（闸门会用结构化渲染兜住）。 */
    private String plainTextOf(long userId, ResumeChildMapper.VersionRow version) {
        ResumeDocumentMapper.ResumeDocumentRow row =
                documents.findByIdAndUser(version.getDocumentId(), userId);
        return row == null || row.getPlainText() == null ? "" : row.getPlainText();
    }

    private void settleOrRefund(long userId, long reserved, ModelAccess access,
                                TokenUsage usage, String note) {
        if (usage == null) {
            credits.refund(userId, reserved, null, note);
        } else {
            credits.settleResume(userId, reserved, access.model(),
                    usage.inputTokens(), usage.outputTokens(), null);
        }
    }

    private static ResumeChildMapper.ProposalRow toRow(long userId, long versionId, int seq,
                                                       ResumeProposal proposal, Instant now) {
        ResumeChildMapper.ProposalRow row = new ResumeChildMapper.ProposalRow();
        row.setVersionId(versionId);
        row.setUserId(userId);
        row.setSeq(seq);
        row.setEntryRef(proposal.entryRef());
        row.setField(proposal.field());
        row.setOriginalText(proposal.originalText());
        row.setProposedText(proposal.proposedText());
        row.setChangeType(proposal.changeType());
        row.setRationale(proposal.rationale());
        row.setCoversKeywords(writeJson(proposal.coversKeywords()));
        row.setAccepted(false);
        row.setGateStatus("pending");
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return row;
    }

    private static ProposalView toView(ResumeChildMapper.ProposalRow row) {
        return new ProposalView(row.getId(), row.getSeq(), row.getEntryRef(), row.getField(),
                row.getOriginalText(), row.getProposedText(), row.getChangeType(), row.getRationale(),
                readKeywords(row.getCoversKeywords()), row.isAccepted(), row.getGateStatus(),
                readViolations(row.getGateViolations()));
    }

    /** 分析结果落库的形态：模型给的要求 + 代码算的三桶 + 覆盖率。 */
    private static String writeAnalysis(ResumeChildMapper.VersionRow version, ResumeAnalysis analysis,
                                        List<ResumeKeywordClassifier.Classified> classified, AnalysisView view) {
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("summary", analysis.summary());
        payload.put("classified", classified);
        payload.put("covered", view.covered());
        payload.put("total", view.total());
        return writeJson(payload);
    }

    private static AnalysisView readAnalysis(String json) {
        try {
            var node = JSON.readTree(json);
            String summary = node.path("summary").asText("");
            List<ResumeKeywordClassifier.Classified> classified = new ArrayList<>();
            for (var item : node.path("classified")) {
                classified.add(new ResumeKeywordClassifier.Classified(
                        item.path("term").asText(""),
                        item.path("quote").asText(""),
                        item.path("bucket").asText(ResumeKeywordClassifier.BUCKET_GAP),
                        item.hasNonNull("hit") ? item.get("hit").asText() : null));
            }
            return new AnalysisView(summary, List.copyOf(classified),
                    node.path("covered").asInt(0), node.path("total").asInt(classified.size()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("库里的分析 JSON 无法解析", exception);
        }
    }

    private static String writeViolations(List<ResumeGate.Violation> violations) {
        return writeJson(violations.stream()
                .map(item -> java.util.Map.of("code", item.code(), "detail", item.detail()))
                .toList());
    }

    private static List<String> readViolations(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> details = new ArrayList<>();
            for (var node : JSON.readTree(json)) {
                details.add(node.path("detail").asText(""));
            }
            return List.copyOf(details);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private static List<String> readKeywords(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> values = new ArrayList<>();
            for (var node : JSON.readTree(json)) {
                values.add(node.asText());
            }
            return List.copyOf(values);
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private static String writeJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("结果无法序列化", exception);
        }
    }

    private static String normalizeJd(String jdText) {
        String trimmed = jdText == null ? "" : jdText.trim();
        if (trimmed.length() < MIN_JD_CHARS) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "JD 太短了（至少 " + MIN_JD_CHARS + " 字），请粘贴完整的岗位描述");
        }
        return trimmed.length() > MAX_JD_CHARS ? trimmed.substring(0, MAX_JD_CHARS) : trimmed;
    }

    private static String trim(String value, int max) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : (trimmed.length() > max ? trimmed.substring(0, max) : trimmed);
    }
}
