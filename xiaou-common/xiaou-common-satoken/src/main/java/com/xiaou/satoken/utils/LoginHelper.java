package com.xiaou.satoken.utils;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Component
public class LoginHelper {


    /**
     * 获取当前登录用户ID
     */
    public String getCurrentAppUserId() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 注销当前用户
     */
    public void removeCurrentAppUser() {
        StpUtil.logout();
    }

}
