package com.xiaou.userinfo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * u_major 表对应的实体类
 */
@Data
@TableName("u_major")
public class Major {

    @TableId
    private Long majorId;               // 专业编号（主键）

    private String name;                // 专业名称

    private String code;                // 专业代码

    private Long collegeId;             // 所属学院ID

    private String leader;              // 专业负责人

    private String educationLevel;      // 学历层次

    private String status;              // 专业状态

    private String createdBy;           // 创建人

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;  // 创建时间

    private String updatedBy;           // 更新人

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;  // 更新时间
}