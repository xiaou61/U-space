package com.xiaou.study.group.teacher.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.study.group.teacher.domain.entity.Homework;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;



@Data
@AutoMapper(target = Homework.class)
public class HomeworkReq {

    /**
     * 作业标题
     */
    private String title;

    /**
     * 作业描述，内容说明
     */
    private String description;

    /**
     * 作业截止提交时间
     */
    private Date deadline;

    /**
     * 群组id
     */
    private String groupId;


}