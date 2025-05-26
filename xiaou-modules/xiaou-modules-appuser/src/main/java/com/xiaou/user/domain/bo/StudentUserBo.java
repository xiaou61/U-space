package com.xiaou.user.domain.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.user.domain.entity.StudentUser;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 学生用户表
 *
 * @TableName u_student_user
 */
@Data
@AutoMapper(target = StudentUser.class)
public class StudentUserBo {

    /**
     * 学号
     */
    @NotBlank(message = "学号不为空")
    private String studentNumber;


    /**
     * 密码
     */
    @NotBlank(message = "密码不为空")
    private String password;

}