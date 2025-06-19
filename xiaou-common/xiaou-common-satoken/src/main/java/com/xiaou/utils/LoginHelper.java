package com.xiaou.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xiaou.domain.UserRoles;
import com.xiaou.mapper.UserRolesMapper;
import com.xiaou.service.UserRolesService;
import jakarta.annotation.Resource;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
public class LoginHelper {
    @Resource
    private static UserRolesMapper userRolesMapper;


    /**
     * 获取当前登录admin的用户名
     *
     * @return 用户名（如果未登录则可能返回 null）
     */
    public static String getCurrentUsername() {
        Object obj = StpUtil.getSession().get("adminUserLogin");
        if (obj == null) return null;

        JSONObject json = JSON.parseObject(JSON.toJSONString(obj));
        return json.getString("username");
    }

    /**
     * 获取当前登录appuser的id Login类型的
     */
    public static Long getCurrentAppUserId() {
        return StpUtil.getLoginIdAsLong();
    }


    public static void addUserRole(Long id, String teacher) {
        UserRoles userRoles = new UserRoles();
        userRoles.setRole(teacher);
        userRoles.setId(id);
        userRolesMapper.insert(userRoles);
    }
}
