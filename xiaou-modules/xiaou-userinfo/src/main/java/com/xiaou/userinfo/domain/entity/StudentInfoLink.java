package com.xiaou.userinfo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生信息关联表实体类
 * 关联学号与专业、班级、学院
 */
@Data
@TableName("u_student_info_link")
public class StudentInfoLink {

    @TableId
    private Long linkId;             // 主键ID

    private String studentNumber;    // 学号

    private Long majorId;            // 专业ID

    private Long classId;            // 班级ID

    private Long collegeId;          // 学院ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;   // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;   // 更新时间
}
