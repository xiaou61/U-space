package io.github.xiaou61.knowledge.internal.concept;

import java.time.Instant;

/**
 * 一个知识概念（WORK-021）。
 *
 * <p>概念是知识图谱的节点，条目是它的载体：同一条目可以承载多个概念，同一概念可以分散在
 * 多个条目里。可变类的原因同项目其它实体：MyBatis 用 {@code useGeneratedKeys} 回写主键。
 */
public class Concept {

    private Long id;
    /** 全局唯一：两个同名概念在图上无法区分。 */
    private String name;
    /** 一句话说明，可为空。 */
    private String summary;
    /** 归属分类，可为空（概念不必须属于分类）。 */
    private Long categoryId;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
