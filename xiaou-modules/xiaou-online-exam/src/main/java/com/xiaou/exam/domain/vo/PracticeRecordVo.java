package com.xiaou.exam.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.exam.domain.entity.PracticeSession;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@AutoMapper(target = PracticeSession.class)
public class PracticeRecordVo {
    private Long sessionId;
    private Long userId;
    private Long repoId;
    private Integer questionCount;
    private Integer correctCount;
    private Integer wrongCount;
    private Date startTime;
    private Date endTime;

    private List<PracticeQuestionRecordVo> records;
}