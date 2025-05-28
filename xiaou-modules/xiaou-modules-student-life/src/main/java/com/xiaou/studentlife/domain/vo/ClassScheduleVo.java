package com.xiaou.studentlife.domain.vo;

import com.xiaou.studentlife.domain.entity.ClassSchedule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * 班级课表
 */
@Data
@AutoMapper(target = ClassSchedule.class)
public class ClassScheduleVo {

    /**
     * 班级ID（关联学生的所属班级）
     */
    private Long classId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 任课老师名称
     */
    private String teacherName;

    /**
     * 上课教室
     */
    private String classroom;

    /**
     * 星期几（1=周一，7=周日）
     */
    private Integer dayOfWeek;

    /**
     * 第几节课（如1表示第一节）
     */
    private Integer period;

    /**
     * 周次范围，如"1-16"、"1,3,5,7"
     */
    private String weekRange;

}