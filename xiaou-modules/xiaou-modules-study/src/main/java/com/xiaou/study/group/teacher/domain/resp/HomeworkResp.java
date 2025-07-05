package com.xiaou.study.group.teacher.domain.resp;

import com.xiaou.study.group.teacher.domain.entity.Homework;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;


@Data
@AutoMapper(target = Homework.class)
public class HomeworkResp {

    private String id;

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

    private String groupId;

}