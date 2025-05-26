package com.xiaou.user.domain.vo;

import com.xiaou.user.domain.entity.StudentUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 学生用户表
 * @TableName u_student_user
 */
@Data
@AutoMapper(target = StudentUser.class)
public class StudentUserVo {
    /**
     * 学号
     */
    private String studentNumber;

    /**
     * 姓名
     */
    private String name;

    /**
     * 密码
     */
    private String password;

}