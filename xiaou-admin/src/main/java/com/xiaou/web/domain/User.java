package com.xiaou.web.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_user")
public class User {
    private Integer id;
    private String username;
    private String password;
}
