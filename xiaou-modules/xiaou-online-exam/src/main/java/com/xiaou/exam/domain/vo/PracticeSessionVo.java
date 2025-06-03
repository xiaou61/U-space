package com.xiaou.exam.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.exam.domain.entity.PracticeSession;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

@Data

public class PracticeSessionVo {
    private Long sessionId;       // 对应 PracticeSession 的 id
    private Long userId;
    private Long repoId;
    private Integer questionCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Date startTime;
    private Date endTime;
    private Integer currentIndex; // 当前练习题目索引
}

