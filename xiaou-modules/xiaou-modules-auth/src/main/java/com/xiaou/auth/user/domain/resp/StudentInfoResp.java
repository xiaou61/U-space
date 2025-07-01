package com.xiaou.auth.user.domain.resp;

import com.xiaou.auth.user.domain.entity.Student;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Student.class)
public class StudentInfoResp {
    private String id;
    private String studentNo;
    private String name;
    private String classId;
    private String className;
    private String phone;
    /**
     * 头像
     */
    private String avatar;
}
