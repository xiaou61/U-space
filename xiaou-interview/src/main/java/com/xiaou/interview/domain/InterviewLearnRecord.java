package com.xiaou.interview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 学习记录
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewLearnRecord {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题单ID
     */
    private Long questionSetId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 学习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
