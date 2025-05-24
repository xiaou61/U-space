package com.xiaou.userinfo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("u_college")
public class College {

    @TableId
    private Long collegeId;       // 学院编号（主键）
    private String name;             // 学院名称
    private String dean;             // 院长姓名
    private String phone;            // 联系电话
    private String email;            // 电子邮箱
    private String campusName;       // 所属校区名称
    private String createdBy;        // 创建人
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;  // 创建时间
    private String updatedBy;        // 更新人
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;  // 更新时间
}
