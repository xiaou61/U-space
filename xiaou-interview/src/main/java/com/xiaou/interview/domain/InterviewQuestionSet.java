package com.xiaou.interview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 面试题单
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewQuestionSet {

    /**
     * 题单ID
     */
    private Long id;

    /**
     * 题单标题
     */
    private String title;

    /**
     * 题单描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 类型 (1-官方 2-用户创建)
     */
    private Integer type;

    /**
     * 可见性 (1-公开 2-私有) 仅用户创建题单有效
     */
    private Integer visibility;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 状态 (0-草稿 1-发布 2-下线)
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 分类名称（查询时使用）
     */
    private String categoryName;
} 