package com.xiaou.bbs.domain.resp;


import lombok.Data;

@Data
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
