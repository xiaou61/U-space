package com.xiaou.satoken.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.mapper.UserRolesMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * 为用户删除角色
     */
    public void deleteUserRole(String id, String role) {
        //查找id跟role
        QueryWrapper<UserRoles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("role", role);
        userRolesMapper.delete(queryWrapper);
    }

    /**
     * 注销当前用户
     */
    public void removeCurrentAppUser() {
        StpUtil.logout();
    }

}
