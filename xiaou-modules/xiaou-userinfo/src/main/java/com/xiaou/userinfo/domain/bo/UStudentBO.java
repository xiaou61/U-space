package com.xiaou.userinfo.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学生业务对象，用于新增或编辑学生信息
 */
@Data
public class UStudentBO {

    @NotBlank(message = "学号不能为空")
    private String studentNumber;   // 学号

    @NotBlank(message = "姓名不能为空")
    private String name;            // 姓名

    @NotBlank(message = "性别不能为空")
    private String gender;          // 性别（男、女、其他）

    private String phone;           // 联系电话

    @NotNull(message = "入学年份不能为空")
    private Integer enrollmentYear; // 入学年份

    @NotBlank(message = "学籍状态不能为空")
    private String status;          // 学籍状态（在读、休学、退学、毕业）

    //专业id
    private Long majorId;
    //班级id
    private Long classId;
    //学院id
    private Long collegeId;


}
