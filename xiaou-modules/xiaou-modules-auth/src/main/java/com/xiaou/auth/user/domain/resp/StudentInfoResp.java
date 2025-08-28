package com.xiaou.auth.user.domain.resp;

import lombok.Data;

@Data
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
