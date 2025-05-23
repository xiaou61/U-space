package com.xiaou.web.service;

import cn.dev33.satoken.util.SaResult;
import com.xiaou.common.domain.R;
import com.xiaou.web.domain.User;
import com.xiaou.web.domain.UserDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminUserService {
    R<SaResult> login(User user, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);
}
