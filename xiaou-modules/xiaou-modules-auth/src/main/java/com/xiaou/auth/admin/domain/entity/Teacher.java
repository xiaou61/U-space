package com.xiaou.auth.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 教师表
 * @TableName u_teacher
 */
@TableName(value ="u_teacher")
@Data
public class Teacher {
    /**
     * 主键UUID，32位无连字符
     */
    @TableId(type = IdType.ASSIGN_UUID)
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
     * 密码，默认手机号后6位
     */
    private String password;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;
}