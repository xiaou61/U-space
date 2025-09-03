package com.xiaou.interview.dto;

import lombok.Data;

import java.util.List;

/**
 * 随机抽题请求DTO
 *
 * @author xiaou
 */
@Data
public class RandomQuestionRequest {

    /**
     * 题单ID列表
     */
    private List<Long> questionSetIds;

    /**
     * 抽题数量
     */
    private Integer count;
} 