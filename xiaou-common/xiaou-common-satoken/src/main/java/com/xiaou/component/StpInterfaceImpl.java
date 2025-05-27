package com.xiaou.component;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaou.domain.UserRoles;
import com.xiaou.service.UserRolesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        List<UserRoles> roleIds = userRolesService.list(new LambdaQueryWrapper<UserRoles>()
                .eq(UserRoles::getId, Integer.parseInt(loginId.toString())));
        List<String> list = new ArrayList<>();
        if (!(roleIds.size() > 0)) {
            // 用户没有分配角色
            return null;
        }
        roleIds.forEach(roleId -> {
            UserRoles byId = userRolesService.getById(roleId.getId());
            list.add(byId.getRole());
        });
        return list;
    }

}
