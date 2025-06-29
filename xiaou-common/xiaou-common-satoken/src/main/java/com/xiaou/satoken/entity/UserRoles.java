package com.xiaou.satoken.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRoles {
    private String id;
    private String role;
}
