package io.github.xiaou61.ai.internal.essay;

/**
 * 一条抓来的面经帖（WORK-025）。
 *
 * @param externalId  来源站的内容 id，用于去重与回溯
 * @param title       帖子标题
 * @param body        帖子正文（**只在内存里流转**——入库的是从它里面提炼出的题目，不是原文）
 * @param url         原帖地址，展示给用户
 * @param publishedAt 发布时间（epoch millis）
 */
public record FetchedPost(String externalId, String title, String body, String url, long publishedAt) {
}
