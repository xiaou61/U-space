package com.xiaou.auth.admin.domain.resp;

import com.xiaou.auth.user.domain.entity.StudentEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = StudentEntity.class)
public class StudentInfoPageResp {
    private String id;
    private String studentNo;
    private String name;
    private String classId;
    //这个需要联合查询
    private String className;
    private String phone;
    //是否审核
    private String status;
}
