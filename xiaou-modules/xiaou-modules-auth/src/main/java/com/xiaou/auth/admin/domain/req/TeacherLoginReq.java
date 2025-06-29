package com.xiaou.auth.admin.domain.req;

import lombok.Data;

@Data
public class TeacherLoginReq {
    private String employeeNo;
    private String password;
}
