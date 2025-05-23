package com.xiaou.userinfo.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UCollegeBO {
    @NotBlank(message = "学院名称不能为空")
    private String name;         // 学院名称
    @NotBlank(message = "院长姓名不能为空")
    private String dean;         // 院长姓名
    @NotBlank(message = "联系电话不能为空")
    private String phone;        // 联系电话
    @NotBlank(message = "电子邮箱不能为空")
    private String email;        // 电子邮箱
    @NotBlank(message = "所属校区不能为空")
    private String campusName;   // 所属校区名称
}
