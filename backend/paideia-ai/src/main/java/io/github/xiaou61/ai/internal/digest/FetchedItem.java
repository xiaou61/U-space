package io.github.xiaou61.ai.internal.digest;

/**
 * 一个源抓回来的一条（WORK-027）。
 *
 * @param title       原始标题（可能是英文）
 * @param url         原文链接
 * @param summaryHint 源自己给的摘要线索（RSS 的 description、HN 的标题上下文），**已截断到 200 字**。
 *                    它只是给模型的输入，不会直接展示给用户——展示用我们自己写的中文摘要。
 * @param heat        源自身的信号：HN 点数、GitHub 当日新增 star；其它源为 {@code null}
 * @param meta        一条副信息（语言、作者、评论数），直接给人看
 * @param publishedAt 原文发布时间（epoch 毫秒）；源不提供时为抓取时刻
 * @param sourceKey   源标识，如 qbitai / github-trending
 * @param sourceName  源的显示名，如「量子位」
 * @param groupKey    news | github | community | hackernews，决定前端分组
 */
public record FetchedItem(String title, String url, String summaryHint, Integer heat, String meta,
                          long publishedAt, String sourceKey, String sourceName, String groupKey) {
}
