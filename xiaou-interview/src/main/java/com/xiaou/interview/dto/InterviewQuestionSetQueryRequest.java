package com.xiaou.interview.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试题单查询请求DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InterviewQuestionSetQueryRequest implements PageRequest {

    /**
     * 题单标题（模糊查询）
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 类型 (1-官方 2-用户创建)
     */
    private Integer type;

    /**
     * 可见性 (1-公开 2-私有)
     */
    private Integer visibility;

    /**
     * 状态 (0-草稿 1-发布 2-下线)
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 关键词搜索（标题、描述）
     */
    private String keyword;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public InterviewQuestionSetQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public InterviewQuestionSetQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 