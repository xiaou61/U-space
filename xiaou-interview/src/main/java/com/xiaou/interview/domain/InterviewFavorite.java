package com.xiaou.interview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户收藏
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewFavorite {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏类型 (1-题单 2-题目)
     */
    private Integer targetType;

    /**
     * 目标ID（题单ID或题目ID）
     */
    private Long targetId;

    /**
     * 收藏时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 目标标题（查询时使用）
     */
    private String targetTitle;

    /**
     * 目标描述（查询时使用，题单有描述）
     */
    private String targetDescription;
} 