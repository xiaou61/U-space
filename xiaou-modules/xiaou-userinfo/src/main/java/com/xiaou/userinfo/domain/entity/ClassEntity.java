package com.xiaou.userinfo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("u_class")
public class ClassEntity {
    @TableId
    private Long classId;        // 班级编号（主键）

    private String name;         // 班级名称

    private Long majorId;        // 所属专业ID

    private Integer gradeYear;   // 入学年份（YEAR类型在Java中一般用Integer）

    private String classNo;      // 班号（如01、A1）

    private String headTeacher;  // 班主任

    private String status;       // 班级状态（'在读', '毕业', '停办'）

    private String createdBy;    // 创建人

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;  // 创建时间

    private String updatedBy;    // 更新人

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;  // 更新时间
}