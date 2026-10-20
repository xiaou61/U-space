package io.github.xiaou61.knowledge.internal.entry;

import java.time.Instant;

/**
 * 知识条目。
 *
 * <p>一行一个条目、正文是 markdown 源码（渲染在前端做）。可变类的原因同 {@code Category}：
 * 本项目用 {@code useGeneratedKeys} 回写自增主键。
 *
 * <p>查询时会**按需只填部分字段**：列表与相邻条目查询不取 {@code body}（正文可能有几十 KB，
 * 列表里取回来纯属浪费），此时 {@code body} 为 null。判断"有没有正文"不能靠它。
 */
public class Entry {

    private Long id;
    private Long categoryId;
    /**
     * 学习线里的站序（WORK-020）。**可空**：为空表示这条内容没有声明顺序，调用方退回
     * {@code (published_at, id)}。存量条目全是 null，因此加这一列不改变任何既有排序。
     */
    private Integer sortOrder;
    private String title;
    private String body;
    /** 原始来源链接（WORK-025）。「面经真题」展示"回到原帖"；平台自产内容为 null。 */
    private String sourceUrl;
    /**
     * 题目指纹（WORK-025）。自动采集按它去重——同一道题会在不同面经里反复出现。
     * 管理端手抄与课程导入的条目为 null。
     */
    private String fingerprint;
    /** **只有列表查询填充**（`LEFT(body, 200)`）；详情查询不填，为 null。 */
    private String excerpt;
    /** draft | published */
    private String status;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
