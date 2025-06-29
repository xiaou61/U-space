package com.xiaou.satoken.utils;

import cn.dev33.satoken.stp.StpUtil;

import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.mapper.UserRolesMapper;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoginHelper {
    @Resource
    private static UserRolesMapper userRolesMapper;

    /**
     * 获得当前用户id
     */
    public static Long getCurrentAppUserId() {
        return StpUtil.getLoginIdAsLong();
    }


    /**
     * 为用户添加权限
     * @param id
     * @param teacher
     */
    public static void addUserRole(Long id, String teacher) {
        UserRoles userRoles = new UserRoles();
        userRoles.setRole(teacher);
        userRoles.setId(id);
        userRolesMapper.insert(userRoles);
    }
}
