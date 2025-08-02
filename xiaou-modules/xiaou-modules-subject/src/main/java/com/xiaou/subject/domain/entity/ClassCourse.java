package com.xiaou.subject.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName u_class_course
 */
@TableName(value ="u_class_course")
@Data
public class ClassCourse {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 关联的班级id
     */
    private String classId;

    /**
     * 关联的课程id
     */
    private String courseId;
}