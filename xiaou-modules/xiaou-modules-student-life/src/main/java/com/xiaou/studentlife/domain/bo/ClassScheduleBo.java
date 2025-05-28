package com.xiaou.studentlife.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.studentlife.domain.entity.ClassSchedule;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 班级课表
 */
@Data
@AutoMapper(target = ClassSchedule.class)
public class ClassScheduleBo {


    /**
     * 班级ID（关联学生的所属班级）
     */
    @NotBlank(message = "classId不能为空")
    private Long classId;

    /**
     * 课程名称
     */
    @NotBlank(message = "courseName不能为空")
    private String courseName;

    /**
     * 任课老师名称
     */
    @NotBlank(message = "teacherName不能为空")
    private String teacherName;

    /**
     * 上课教室
     */
    @NotBlank(message = "classroom不能为空")
    private String classroom;

    /**
     * 星期几（1=周一，7=周日）
     */
    @NotBlank(message = "dayOfWeek不能为空")
    private Integer dayOfWeek;

    /**
     * 第几节课（如1表示第一节）
     */
    @NotBlank(message = "period不能为空")
    private Integer period;

    /**
     * 周次范围，如"1-16"、"1,3,5,7"
     */
    @NotBlank(message = "weekRange不能为空")
    private String weekRange;

}