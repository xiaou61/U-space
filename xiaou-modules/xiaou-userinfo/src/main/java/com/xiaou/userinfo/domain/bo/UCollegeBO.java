package com.xiaou.userinfo.domain.bo;

import lombok.Data;

@Data
public class UCollegeBO {
    private String name;         // 学院名称
    private String dean;         // 院长姓名
    private String phone;        // 联系电话
    private String email;        // 电子邮箱
    private String campusName;   // 所属校区名称
}
