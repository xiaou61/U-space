package com.xiaou.interview.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试题目查询请求DTO
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InterviewQuestionQueryRequest implements PageRequest {

    /**
     * 题单ID
     */
    private Long questionSetId;

    /**
     * 题目标题（模糊查询）
     */
    private String title;

    /**
     * 关键词搜索（标题、答案）
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
    public InterviewQuestionQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public InterviewQuestionQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 