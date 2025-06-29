package com.xiaou.auth.admin.service;


import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.common.domain.R;

public interface AdminUserService extends IService<AdminUser> {

    R<SaResult> login(AdminUserReq req);
}
