package com.xiaou.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRoles {
    private Long id;
    private String role;
}
