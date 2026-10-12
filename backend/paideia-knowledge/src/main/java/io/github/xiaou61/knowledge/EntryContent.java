package io.github.xiaou61.knowledge;

import java.time.Instant;

/**
 * 跨模块读取的条目内容（已发布条目的标题与正文）。
 *
 * <p>刻意**不含** `status`、`categoryId` 这些别的模块用不上的字段：契约只暴露调用方真正
 * 需要的东西，将来内部表结构变了也不必改这里。
 *
 * <p>只有**已发布**条目会以这个形状流出（草稿对本模块之外不可见，见
 * {@link KnowledgeApi#findPublishedContent(long)}）。
 */
public record EntryContent(long id, String title, String body, Instant updatedAt) {
}
