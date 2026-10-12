package io.github.xiaou61.ai.internal.memory;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.platform.SearchQuery;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 记忆的存取与召回（用户裁决 2：结构化 + 全文两路）。
 *
 * <p>**严格私有**（用户裁决 3）：每个方法都带 `user_id`，没有"读别人记忆"的入口，
 * 也没有分享开关。不是靠调用方自觉，而是这层就没有那条路。
 *
 * <p>召回是**两路合并**：结构化那路给的是"关于这个学习者的既成事实"（哪个条目懂／不懂），
 * 全文那路给的是"他说过的话"。顺序上结构化在前——事实比原话更稳定，也更容易被模型直接用上。
 */
@Service
public class MemoryStore {

    /**
     * 一次召回的条数上限。
     *
     * <p>ponytail: 固定值，不做成配置。它只影响"进提示词的记忆有多长"，等真的出现
     * "记忆太多把上下文挤爆"的实测再提成配置项。
     */
    private static final int RECALL_LIMIT = 8;

    /** 与迁移里的列宽一致：写入前先校验，别让数据库报一个用户看不懂的错。 */
    private static final int MAX_STRUCTURED_CHARS = 512;
    private static final int MAX_TEXT_CHARS = 1024;

    private final MemoryMapper mapper;

    MemoryStore(MemoryMapper mapper) {
        this.mapper = mapper;
    }

    /** 一条记忆的对外形状。设置页要按 `source` + `id` 逐条删除（两路 id 各自独立）。 */
    public record MemoryItem(Source source, long id, String text, Instant createdAt) {

        public enum Source {
            STRUCTURED,
            TEXT
        }
    }

    /** 写一条结构化记忆（如"某条目：还不懂"）。 */
    @Transactional
    public long rememberStructured(long userId, String kind, Long refId, String content) {
        String trimmedKind = requireNonBlank(kind, "记忆种类", 32);
        String trimmed = requireNonBlank(content, "记忆内容", MAX_STRUCTURED_CHARS);
        MemoryRow row = new MemoryRow();
        row.setUserId(userId);
        row.setKind(trimmedKind);
        row.setRefId(refId);
        row.setContent(trimmed);
        mapper.insertStructured(row);
        return row.getId();
    }

    /** 写一条全文记忆（"他说过的话"）。 */
    @Transactional
    public long rememberText(long userId, String content) {
        String trimmed = requireNonBlank(content, "记忆内容", MAX_TEXT_CHARS);
        MemoryRow row = new MemoryRow();
        row.setUserId(userId);
        row.setContent(trimmed);
        mapper.insertText(row);
        return row.getId();
    }

    /**
     * 召回：结构化取最近的若干条，全文按关键词检索（没有关键词时取最近若干条）。
     *
     * <p>关键词走 {@link SearchQuery} 的判定：中文单字在 ngram 索引上召回为零，必须退回 `LIKE`。
     * 这是**已经踩过一次**的坑（知识库检索那里），所以这里直接用同一份判定而不是重写一遍。
     */
    public List<MemoryItem> recall(long userId, String keyword) {
        List<MemoryItem> items = new ArrayList<>();
        for (MemoryRow row : mapper.selectStructuredRecent(userId, RECALL_LIMIT)) {
            items.add(new MemoryItem(MemoryItem.Source.STRUCTURED, row.getId(), row.getContent(), row.getCreatedAt()));
        }

        String normalized = SearchQuery.normalize(keyword);
        boolean fulltext = SearchQuery.usesFulltext(normalized);
        List<MemoryRow> textRows = normalized == null
                ? mapper.selectTextRecent(userId, RECALL_LIMIT)
                : mapper.searchText(userId, normalized,
                        fulltext ? null : SearchQuery.likePattern(normalized), fulltext, RECALL_LIMIT);
        for (MemoryRow row : textRows) {
            items.add(new MemoryItem(MemoryItem.Source.TEXT, row.getId(), row.getContent(), row.getCreatedAt()));
        }
        return items;
    }

    /**
     * 本人全部记忆（设置页用，用户裁决 13）。
     *
     * <p>**不复用 {@link #recall}**：那个带上限（RECALL_LIMIT = 8，只为"进提示词的几行"服务）。
     * 设置页要的是"它到底记住了我什么"，被截断会让界面撒谎。
     *
     * <p>ponytail: 这里给 200 条的显示上限，超出只影响设置页能一次看到多少（不会静默删数据）。
     * 等真有人攒到那么多记忆再做分页。
     */
    private static final int LIST_LIMIT = 200;

    public List<MemoryItem> list(long userId) {
        List<MemoryItem> items = new ArrayList<>();
        for (MemoryRow row : mapper.selectStructuredRecent(userId, LIST_LIMIT)) {
            items.add(new MemoryItem(MemoryItem.Source.STRUCTURED, row.getId(), row.getContent(), row.getCreatedAt()));
        }
        for (MemoryRow row : mapper.selectTextRecent(userId, LIST_LIMIT)) {
            items.add(new MemoryItem(MemoryItem.Source.TEXT, row.getId(), row.getContent(), row.getCreatedAt()));
        }
        return items;
    }

    /** 逐条删除。返回是否真的删掉了一行（不是本人的行会返回 false，且**不泄漏它是否存在**）。 */
    @Transactional
    public boolean delete(long userId, MemoryItem.Source source, long id) {
        int affected = source == MemoryItem.Source.STRUCTURED
                ? mapper.deleteStructured(id, userId)
                : mapper.deleteText(id, userId);
        return affected > 0;
    }

    /**
     * 空白内容直接拒，超长也拒。
     *
     * <p>超长不静默截断：记忆是要被后续对话反复读取的东西，悄悄砍掉一半比报错更坏。
     * 这一层是信任边界（将来的写入方包含模型），所以校验放在这里而不是每个调用点。
     */
    private static String requireNonBlank(String raw, String label, int maxChars) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, label + "不能为空");
        }
        if (trimmed.length() > maxChars) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, label + "不能超过 " + maxChars + " 个字符");
        }
        return trimmed;
    }
}
