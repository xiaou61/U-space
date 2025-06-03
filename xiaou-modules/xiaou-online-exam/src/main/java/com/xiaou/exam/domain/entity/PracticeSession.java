package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@TableName(value = "u_practice_session")
@Data
public class PracticeSession {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题库ID
     */
    private Long repoId;

    /**
     * 本次练习题目数
     */
    private Integer questionCount;

    /**
     * 正确数量
     */
    private Integer correctCount;

    /**
     * 错误数量
     */
    private Integer wrongCount;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer currentIndex; // 当前练习题目索引，0-based

}