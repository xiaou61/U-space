package com.xiaou.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class LoginHelper {
    private static final Logger log = LoggerFactory.getLogger(LoginHelper.class);

    /**
     * 获取当前登录用户的用户名
     *
     * @return 用户名（如果未登录则可能返回 null）
     */
    public static String getCurrentUsername() {
        Object obj = StpUtil.getSession().get("user_login");
        if (obj == null) return null;

        JSONObject json = JSON.parseObject(JSON.toJSONString(obj));
        return json.getString("username");
    }

}
