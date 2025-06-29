package com.xiaou.satoken.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.mapper.UserRolesMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LoginHelper {

    @Resource
    private UserRolesMapper userRolesMapper;

    /**
     * 获取当前登录用户ID
     */
    public String getCurrentAppUserId() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 为用户添加角色
     */
    public void addUserRole(String id, String role) {
        UserRoles userRoles = new UserRoles();
        userRoles.setId(id);
        userRoles.setRole(role);
        userRolesMapper.insert(userRoles);
    }

    /**
     * 注销当前用户
     */
    public void removeCurrentAppUser() {
        StpUtil.logout();
    }
}
