package com.xiaou.satoken.component;

import cn.dev33.satoken.stp.StpInterface;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.service.UserRolesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserRolesService userRolesService;

    @Override
    public List<String> getPermissionList(Object o, String s) {
        //目前系统这个不做实现
        return List.of();
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String userId = loginId.toString();

        // 查询该用户的所有角色记录
        List<UserRoles> userRoles = userRolesService.list(
                new LambdaQueryWrapper<UserRoles>().eq(UserRoles::getId, userId)
        );

        // 若无角色，返回 null（也可以考虑返回空集合）
        if (userRoles.isEmpty()) {
            return null;
        }

        // 提取角色名列表并返回
        return userRoles.stream()
                .map(UserRoles::getRole)
                .collect(Collectors.toList());
    }


}
