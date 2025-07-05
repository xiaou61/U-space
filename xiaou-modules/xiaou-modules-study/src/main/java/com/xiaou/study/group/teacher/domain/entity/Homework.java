package com.xiaou.study.group.teacher.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


@TableName(value ="u_homework")
@Data
public class Homework {
    /**
     * 作业ID，主键，UUID格式
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 发布作业的老师ID，关联教师表
     */
    private String teacherId;

    /**
     * 作业标题
     */
    private String title;
    /**
     * 关联的组id
     */
    private String groupId;

    /**
     * 作业描述，内容说明
     */
    private String description;


    /**
     * 作业截止提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deadline;

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