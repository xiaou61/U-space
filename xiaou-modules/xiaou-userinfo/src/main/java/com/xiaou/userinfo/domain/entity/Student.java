package com.xiaou.userinfo.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("u_student") // 指定数据库表名
public class Student {

    /**
     * 学生编号（主键，自增）
     */
    @TableId
    private Long studentId;

    /**
     * 学号，唯一标识
     */
    private String studentNumber;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别（男 / 女 / 其他）
     */
    private String gender;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 入学年份（如 2022）
     */
    private Integer enrollmentYear;

    /**
     * 学籍状态（在读 / 休学 / 退学 / 毕业）
     */
    private String status;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间（自动填充）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间（自动更新）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
