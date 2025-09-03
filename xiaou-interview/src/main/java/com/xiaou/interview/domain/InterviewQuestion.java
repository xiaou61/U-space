package com.xiaou.interview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 面试题目
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewQuestion {

    /**
     * 题目ID
     */
    private Long id;

    /**
     * 所属题单ID
     */
    private Long questionSetId;

    /**
     * 题目标题（题目问题）
     */
    private String title;

    /**
     * 参考答案 (Markdown格式)
     */
    private String answer;

    /**
     * 题单内排序
     */
    private Integer sortOrder;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

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
     * 题单标题（查询时使用）
     */
    private String questionSetTitle;
} 