package io.github.xiaou61.ai.internal.resume;

import io.github.xiaou61.ai.ResumeData;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 主简历的编辑用例（WORK-018 TASK-005/006）。
 *
 * <p>四条纪律：
 * <ol>
 *   <li><b>归属一律经 SQL 校验</b>：拿不到（不存在 / 不是本人的）都是同一条 404，不区分原因——
 *       区分开就等于确认"某份简历存在"。</li>
 *   <li><b>保存用乐观锁</b>：revision 对不上就 409，绝不静默覆盖别人的改动。</li>
 *   <li><b>快照按时间窗口合并</b>：60 秒内的连续自动保存合并成一条，否则历史表会以每分钟
 *       十条的速度膨胀；每份最多留 50 条。</li>
 *   <li><b>回滚不删历史</b>：回滚本身是一次普通保存（revision+1、写新快照），
 *       所以"回到上一版"之后还能回到"回滚之前"。</li>
 * </ol>
 */
@Service
public class ResumeDocumentService {

    private static final Logger log = LoggerFactory.getLogger(ResumeDocumentService.class);

    /**
     * 快照合并窗口。60 秒覆盖了"打一段话再停下"的节奏；更短会让历史里塞满半句话。
     *
     * <p>ponytail: 常量而不是配置项——它只在"自动保存太碎"这个真实症状出现时才需要调，
     * 而现在没有任何样本能说明该调成多少。升级路径是改成配置属性。
     */
    static final Duration MERGE_WINDOW = Duration.ofSeconds(60);

    /** 每份简历保留多少条快照。 */
    static final int KEEP_HISTORY = 50;

    private final ResumeDocumentMapper documents;
    private final ResumeChildMapper children;
    private final ResumeJson json;
    private final ResumeCheckup checkup;

    ResumeDocumentService(ResumeDocumentMapper documents, ResumeChildMapper children,
                          ResumeJson json, ResumeCheckup checkup) {
        this.documents = documents;
        this.children = children;
        this.json = json;
        this.checkup = checkup;
    }

    /** 列表项。**不含简历内容**：列表只用来选，打开某一份才读内容。 */
    public record Summary(long id, String title, boolean isDefault, int revision, Instant updatedAt) {
    }

    /** 详情：结构化数据 + 原文 + revision（编辑器保存时要带回来）。 */
    public record Detail(long id, String title, boolean isDefault, int revision,
                         ResumeData data, Instant updatedAt) {
    }

    /** 快照项。**不含内容**：预览某一版时才读。 */
    public record HistorySummary(long id, Instant createdAt) {
    }

    public List<Summary> list(long userId) {
        return documents.listByUser(userId).stream()
                .map(row -> new Summary(row.getId(), row.getTitle(), row.isDefault(),
                        row.getRevision(), row.getUpdatedAt()))
                .toList();
    }

    public Detail get(long userId, long documentId) {
        ResumeDocumentMapper.ResumeDocumentRow row = require(userId, documentId);
        return new Detail(row.getId(), row.getTitle(), row.isDefault(), row.getRevision(),
                json.parseStored(row.getDataJson()), row.getUpdatedAt());
    }

    /**
     * 创建主简历。
     *
     * @param plainText 抽取原文；用户完全手工新建时为空字符串（此时闸门只用结构化渲染当基准）
     */
    @Transactional
    public long create(long userId, String title, ResumeData data, String plainText) {
        String safeTitle = normalizeTitle(title);
        Instant now = Instant.now();
        ResumeDocumentMapper.ResumeDocumentRow row = new ResumeDocumentMapper.ResumeDocumentRow();
        row.setUserId(userId);
        row.setTitle(safeTitle);
        // 第一份自动成为默认：空列表里"默认"这个概念对用户没有意义，但有了第二份之后就有
        row.setDefault(documents.listByUser(userId).isEmpty());
        row.setDataJson(json.write(data));
        row.setPlainText(plainText == null ? "" : plainText);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        documents.insertRow(row);
        long id = row.getId();
        writeSnapshot(userId, id, row.getDataJson(), now);
        log.info("ai.resume.created userId={} documentId={}", userId, id);
        return id;
    }

    /**
     * 整份保存（自动保存用它）。
     *
     * @throws BizException 404（不属于他）/ 409（revision 冲突）
     */
    @Transactional
    public Detail save(long userId, long documentId, String title, ResumeData data, int revision) {
        // 先取一次旧行：一是校验归属（拿不到就 404），二是判断是否真的需要写
        ResumeDocumentMapper.ResumeDocumentRow existing = require(userId, documentId);
        String safeTitle = normalizeTitle(title);
        Instant now = Instant.now();
        ResumeDocumentMapper.ResumeDocumentRow update = new ResumeDocumentMapper.ResumeDocumentRow();
        update.setId(documentId);
        update.setUserId(userId);
        update.setTitle(safeTitle);
        update.setRevision(revision);
        update.setDataJson(json.write(data));
        update.setPlainText(existing.getPlainText());
        update.setUpdatedAt(now);

        if (documents.updateWithRevision(update) == 0) {
            throw new BizException(ErrorCode.CONFLICT,
                    "这份简历在别处被改过了。请选择「用我的版本覆盖」或「重新加载」");
        }
        writeSnapshot(userId, documentId, update.getDataJson(), now);
        return get(userId, documentId);
    }

    /** 改名。不动 revision：改标题不是改内容，不该让并行的编辑器冲突。 */
    public Summary rename(long userId, long documentId, String title) {
        require(userId, documentId);
        documents.rename(documentId, userId, normalizeTitle(title), Instant.now());
        return summarize(userId, documentId);
    }

    /** 设默认。先清后设，两条在同一事务里。 */
    @Transactional
    public Summary setDefault(long userId, long documentId) {
        require(userId, documentId);
        documents.clearDefault(userId);
        documents.markDefault(documentId, userId, Instant.now());
        return summarize(userId, documentId);
    }

    /**
     * 删除。**没有跨模块外键，删的顺序必须自己写对**：提案 → 版本 → 快照 → 主表。
     *
     * <p>顺序反了不会报错（没有外键），只会留下谁也指不到的孤儿行——那正是注销级联
     * 那条同类教训（删了主表、子表全留下）。
     */
    @Transactional
    public void delete(long userId, long documentId) {
        require(userId, documentId);
        children.deleteProposalsOfDocument(documentId, userId);
        children.deleteVersionsOfDocument(documentId, userId);
        children.deleteHistoryOfDocument(documentId, userId);
        documents.deleteByIdAndUser(documentId, userId);
        log.info("ai.resume.deleted userId={} documentId={}", userId, documentId);
    }

    // ── 历史 ────────────────────────────────────────────────────────

    public List<HistorySummary> history(long userId, long documentId) {
        require(userId, documentId);
        return children.listHistory(documentId, userId, KEEP_HISTORY).stream()
                .map(row -> new HistorySummary(row.getId(), row.getCreatedAt()))
                .toList();
    }

    /** 读某一版的内容（预览用）。 */
    public ResumeData historyData(long userId, long documentId, long historyId) {
        require(userId, documentId);
        ResumeChildMapper.HistoryRow row = children.findHistory(historyId, userId);
        if (row == null || !Long.valueOf(documentId).equals(row.getDocumentId())) {
            throw notFound();
        }
        return json.parseStored(row.getDataJson());
    }

    /**
     * 回滚到某一版。
     *
     * <p>回滚 = 把那一版的内容当作一次普通保存写回去，所以：
     * <ul>
     *   <li>revision 会 +1（并行编辑器能看到变化）；</li>
     *   <li>历史里会多一条新快照（"回滚"这个动作本身也被记录）；</li>
     *   <li>被回滚到的那一版**不消失**，还能再回去。</li>
     * </ul>
     */
    @Transactional
    public Detail restore(long userId, long documentId, long historyId, int revision) {
        ResumeData target = historyData(userId, documentId, historyId);
        ResumeDocumentMapper.ResumeDocumentRow existing = require(userId, documentId);
        return save(userId, documentId, existing.getTitle(), target, revision);
    }

    // ── 体检 ────────────────────────────────────────────────────────

    public List<ResumeCheckup.Item> checkup(long userId, long documentId) {
        return checkup.check(get(userId, documentId).data());
    }

    // ── 内部 ────────────────────────────────────────────────────────

    ResumeDocumentMapper.ResumeDocumentRow require(long userId, long documentId) {
        ResumeDocumentMapper.ResumeDocumentRow row = documents.findByIdAndUser(documentId, userId);
        if (row == null) {
            throw notFound();
        }
        return row;
    }

    private Summary summarize(long userId, long documentId) {
        ResumeDocumentMapper.ResumeDocumentRow row = require(userId, documentId);
        return new Summary(row.getId(), row.getTitle(), row.isDefault(), row.getRevision(), row.getUpdatedAt());
    }

    /**
     * 写一条快照；60 秒窗口内已有快照就覆盖它。
     *
     * <p>窗口判断用**最新一条**的时间，不是"任意一条"：用户连续改十分钟，期间每小时一条会
     * 让历史失去粒度；而只与最新一条比较就能得到"每 60 秒最多一条"的效果。
     */
    private void writeSnapshot(long userId, long documentId, String dataJson, Instant now) {
        ResumeChildMapper.HistoryRow latest = children.findLatestHistory(documentId, userId);
        if (latest != null && Duration.between(latest.getCreatedAt(), now).compareTo(MERGE_WINDOW) <= 0) {
            latest.setDataJson(dataJson);
            latest.setCreatedAt(now);
            children.updateHistory(latest);
        } else {
            ResumeChildMapper.HistoryRow row = new ResumeChildMapper.HistoryRow();
            row.setDocumentId(documentId);
            row.setUserId(userId);
            row.setDataJson(dataJson);
            row.setCreatedAt(now);
            children.insertHistory(row);
        }
        children.trimHistory(documentId, userId, KEEP_HISTORY);
    }

    private static String normalizeTitle(String title) {
        String trimmed = title == null ? "" : title.trim();
        if (trimmed.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "简历标题不能为空");
        }
        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }

    private static BizException notFound() {
        return new BizException(ErrorCode.NOT_FOUND, "简历不存在");
    }
}
