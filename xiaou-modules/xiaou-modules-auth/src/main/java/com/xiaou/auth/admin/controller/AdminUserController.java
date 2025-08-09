package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.service.AdminUserService;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理Controller
 * 
 * @author xiaou
 */
@RestController
@RequestMapping("/admin/user")
@Validated
public class AdminUserController {

    @Resource
    private AdminUserService adminUserService;

    /**
     * 获取所有管理员用户列表
     */
    @GetMapping("/list")
    public R<List<AdminUser>> getUserList() {
        return adminUserService.getAllUsers();
    }

    /**
     * 添加管理员用户
     */
    @Log(title = "添加管理员用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<String> addUser(@RequestBody AdminUser adminUser) {
        if (adminUser.getUsername() == null || adminUser.getUsername().trim().isEmpty()) {
            return R.fail("用户名不能为空");
        }
        if (adminUser.getPassword() == null || adminUser.getPassword().trim().isEmpty()) {
            return R.fail("密码不能为空");
        }
        return adminUserService.addUser(adminUser);
    }

    /**
     * 更新管理员用户信息
     */
    @Log(title = "更新管理员用户", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R<String> updateUser(@RequestBody AdminUser adminUser) {
        if (adminUser.getId() == null || adminUser.getId().trim().isEmpty()) {
            return R.fail("用户ID不能为空");
        }
        if (adminUser.getUsername() == null || adminUser.getUsername().trim().isEmpty()) {
            return R.fail("用户名不能为空");
        }
        return adminUserService.updateUser(adminUser);
    }

    /**
     * 删除管理员用户
     */
    @Log(title = "删除管理员用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{userId}")
    public R<String> deleteUser(@PathVariable String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return R.fail("用户ID不能为空");
        }
        return adminUserService.deleteUser(userId);
    }

    /**
     * 重置用户密码
     */
    @Log(title = "重置用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/reset-password")
    public R<String> resetPassword(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String newPassword = params.get("newPassword");

        if (userId == null || userId.trim().isEmpty()) {
            return R.fail("用户ID不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return R.fail("新密码不能为空");
        }

        return adminUserService.resetPassword(userId, newPassword);
    }
}