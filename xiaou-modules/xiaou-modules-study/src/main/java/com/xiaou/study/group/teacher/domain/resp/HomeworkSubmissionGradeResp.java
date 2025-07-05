package com.xiaou.study.group.teacher.domain.resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.xiaou.study.group.teacher.domain.entity.HomeworkSubmission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 作业提交表，存储学生提交的作业内容及附件等信息
 *
 * @TableName u_homework_submission
 */
@Data
@AutoMapper(target = HomeworkSubmission.class)
public class HomeworkSubmissionGradeResp {

    /**
     * 学生提交的文本内容
     */
    private BigDecimal grade;
    /**
     * 老师对作业的反馈意见
     */
    private String feedback;

}