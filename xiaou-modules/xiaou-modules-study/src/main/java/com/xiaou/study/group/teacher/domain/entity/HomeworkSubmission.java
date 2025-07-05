package com.xiaou.study.group.teacher.domain.entity;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.mybatis.handler.JsonListTypeHandler;
import lombok.Data;

/**
 * 作业提交表，存储学生提交的作业内容及附件等信息
 * @TableName u_homework_submission
 */
@TableName(value ="u_homework_submission")
@Data
public class HomeworkSubmission {
    /**
     * 提交ID，主键，UUID格式
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 关联的作业ID，外键，关联u_homework表
     */
    private String homeworkId;

    /**
     * 提交作业的学生ID，关联学生表
     */
    private String studentId;

    /**
     * 学生提交的文本内容
     */
    private String content;

    /**
     * 附件地址的JSON数组，存储多个附件链接
     */
    @TableField(typeHandler = JsonListTypeHandler.class)
    private List<String> attachmentUrls;

    /**
     * 提交时间，默认当前时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    /**
     * 提交状态，如submitted(已提交), graded(已评分)等
     */
    private String status;

    /**
     * 评分，最多5位，2位小数
     */
    private BigDecimal grade;

    /**
     * 老师对作业的反馈意见
     */
    private String feedback;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}