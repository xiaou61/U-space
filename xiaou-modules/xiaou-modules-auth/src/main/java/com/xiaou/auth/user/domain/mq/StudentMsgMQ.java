package com.xiaou.auth.user.domain.mq;

import com.xiaou.auth.user.domain.entity.StudentEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = StudentEntity.class)
public class StudentMsgMQ {
    /**
     * 学生姓名
     */
    private String name;

    /**
     * 学生学号
     */
    private String studentNo;

    /**
     * 对应的班级号
     */
    private String classId;
}
