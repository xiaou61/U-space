package com.xiaou.study.group.teacher.domain.req;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeworkApproveReq {


    /**
     * 评分，最多5位，2位小数
     */
    private BigDecimal grade;

    /**
     * 老师对作业的反馈意见
     */
    private String feedback;
}
