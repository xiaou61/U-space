package com.xiaou.bbs.domain.resp;

import com.xiaou.auth.user.domain.entity.Student;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = Student.class)
public class BbsStudentInfoResp {


    private String id;

    /**
     * 学生姓名
     */
    private String name;
    /**
     * 头像
     */
    private String avatar;

}
