package com.xiaou.auth.admin.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.auth.admin.domain.entity.Teacher;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 教师表
 * @TableName u_teacher
 */
@Data
@AutoMapper(target = Teacher.class)
public class TeacherReq {

    /**
     * 教职工编号（8位数字字符串）
     */
    @NotBlank(message = "教职工编号不能为空")
    private String employeeNo;

    /**
     * 教师姓名
     */
    @NotBlank(message = "教师姓名不能为空")
    private String name;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;


}