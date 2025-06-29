package com.xiaou.auth.user.domain.req;

import com.xiaou.auth.user.domain.entity.Student;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 学生表
 * @TableName u_student
 */
@Data
@AutoMapper(target = Student.class)
public class StudentRegisterReq {

    /**
     * 学号（唯一编号）
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 班级ID，关联class表主键
     */
    private String classId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

}