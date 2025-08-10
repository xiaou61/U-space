package com.xiaou.satoken.component;

import cn.dev33.satoken.stp.StpInterface;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String userId = loginId.toString();

        try {
            // 直接使用SQL查询用户权限代码
            String sql = """
                    SELECT DISTINCT p.permission_code
                    FROM sys_permission p
                    INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
                    INNER JOIN sys_role r ON rp.role_id = r.id
                    INNER JOIN sys_user_role ur ON r.id = ur.role_id
                    WHERE ur.user_id = ? COLLATE utf8mb4_general_ci
                    AND ur.user_type = ?
                    AND p.status = 1
                    AND r.status = 1
                    ORDER BY p.sort_order ASC
                    """;

            return jdbcTemplate.queryForList(sql, String.class, userId, "ADMIN");
        } catch (Exception e) {
            // 记录日志但不抛出异常，避免影响登录
            System.err.println("获取用户权限失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String userId = loginId.toString();

        try {
            // 直接使用SQL查询用户角色代码
            String sql = """
                    SELECT r.role_code
                    FROM sys_role r
                    INNER JOIN sys_user_role ur ON r.id = ur.role_id
                    WHERE ur.user_id = ? COLLATE utf8mb4_general_ci
                    AND ur.user_type = ?
                    AND r.status = 1
                    ORDER BY r.sort_order ASC
                    """;

            List<String> roles = jdbcTemplate.queryForList(sql, String.class, userId, "ADMIN");
            return roles.isEmpty() ? Collections.emptyList() : roles;
        } catch (Exception e) {
            // 记录日志但不抛出异常，避免影响登录
            System.err.println("获取用户角色失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
