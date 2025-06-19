package com.xiaou.grade.domain.bo;

import com.xiaou.grade.domain.entity.Teacher;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@AutoMapper(target = Teacher.class)
public class TeacherBo {

    /**
     * 教师姓名
     */
    @NotBlank(message = "教师姓名不能为空")
    private String name;

    /**
     * 手机号（用于登录）
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

}
