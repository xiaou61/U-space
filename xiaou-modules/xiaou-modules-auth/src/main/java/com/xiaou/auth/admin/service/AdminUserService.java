package com.xiaou.auth.admin.service;

import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.common.domain.R;

import java.util.List;

public interface AdminUserService extends IService<AdminUser> {

    R<SaResult> login(AdminUserReq req);

    R<String> updatePassword(String oldPassword, String newPassword);

    /**
     * 获取所有管理员用户列表
     */
    R<List<AdminUser>> getAllUsers();

    /**
     * 添加管理员用户
     */
    R<String> addUser(AdminUser adminUser);

    /**
     * 删除管理员用户
     */
    R<String> deleteUser(String userId);

    /**
     * 重置用户密码（管理员操作）
     */
    R<String> resetPassword(String userId, String newPassword);

    /**
     * 更新用户信息
     */
    R<String> updateUser(AdminUser adminUser);
}
