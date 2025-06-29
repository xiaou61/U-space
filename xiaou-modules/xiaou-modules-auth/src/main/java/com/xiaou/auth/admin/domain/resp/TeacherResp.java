package com.xiaou.auth.admin.domain.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.auth.admin.domain.entity.Teacher;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 教师表
 * @TableName u_teacher
 */

@Data
@AutoMapper(target = Teacher.class)
public class TeacherResp {

    private String id;

    /**
     * 教职工编号（8位数字字符串）
     */
    private String employeeNo;

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;


    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;
}