package com.xiaou.userinfo.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UCollegeVO {
    private String name;             // 学院名称
    private String dean;             // 院长姓名
    private String phone;            // 联系电话
    private String email;            // 电子邮箱
    private String campusName;       // 所属校区名称
    private String createdBy;        // 创建人
    private String updatedBy;        // 更新人
    private LocalDateTime createdTime;  // 创建时间
    private LocalDateTime updatedTime;  // 更新时间
}
