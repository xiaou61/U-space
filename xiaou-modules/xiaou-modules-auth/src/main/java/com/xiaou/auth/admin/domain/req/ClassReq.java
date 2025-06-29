package com.xiaou.auth.admin.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 班级信息表
 * @TableName u_class
 */
@Data
@AutoMapper(target = ClassEntity.class)
public class ClassReq {

    /**
     * 班级名称
     */
    @NotBlank(message = "班级名称不能为空")
    private String className;

    /**
     * 年级，2025级
     */
    @NotBlank(message = "年级不能为空")
    private String grade;

    /**
     * 所属专业，例如：计算机科学与技术
     */
    @NotBlank(message = "专业不能为空")
    private String major;

    /**
     * 班主任姓名
     */
    @NotBlank(message = "班主任姓名不能为空")
    private String classTeacher;

    /**
     * 学生人数
     */
    @Min(value = 0, message = "学生人数不能小于0")
    private Integer studentCount;

}