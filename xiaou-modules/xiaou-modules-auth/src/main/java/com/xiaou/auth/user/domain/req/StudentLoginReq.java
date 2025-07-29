package com.xiaou.auth.user.domain.req;

import com.xiaou.auth.user.domain.entity.StudentEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@Data
@AutoMapper(target = StudentEntity.class)
public class StudentLoginReq {
    /**
     * 学号（唯一编号）
     */
    private String studentNo;
    /**
     * 密码
     */
    private String password;
}
