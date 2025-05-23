package com.xiaou.web.service;

import com.xiaou.common.domain.R;
import com.xiaou.web.domain.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminUserService {
    R<User> login(User user, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);
}
