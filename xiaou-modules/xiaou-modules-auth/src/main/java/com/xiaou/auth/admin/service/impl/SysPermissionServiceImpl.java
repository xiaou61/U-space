package com.xiaou.auth.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.SysPermission;
import com.xiaou.auth.admin.domain.entity.SysRolePermission;
import com.xiaou.auth.admin.mapper.SysPermissionMapper;
import com.xiaou.auth.admin.mapper.SysRolePermissionMapper;
import com.xiaou.auth.admin.service.SysPermissionService;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统权限Service实现类
 * 
 * @author xiaou
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
        implements SysPermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private LoginHelper loginHelper;

    @Override
    public R<List<SysPermission>> getMenuPermissionTree() {
        try {
            List<SysPermission> permissions = sysPermissionMapper.selectMenuPermissions();
            List<SysPermission> tree = buildPermissionTree(permissions);
            return R.ok(tree);
        } catch (Exception e) {
            return R.fail("获取菜单权限树失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<SysPermission>> getPermissionsByRoleId(String roleId) {
        try {
            if (!StringUtils.hasText(roleId)) {
                return R.fail("角色ID不能为空");
            }
            List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByRoleId(roleId);
            return R.ok(permissions);
        } catch (Exception e) {
            return R.fail("查询角色权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<SysPermission>> getPermissionsByUserId(String userId, String userType) {
        try {
            if (!StringUtils.hasText(userId)) {
                return R.fail("用户ID不能为空");
            }
            List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByUserId(userId, userType);
            return R.ok(permissions);
        } catch (Exception e) {
            return R.fail("查询用户权限失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> assignPermissionsToRole(String roleId, List<String> permissionIds) {
        try {
            if (!StringUtils.hasText(roleId)) {
                return R.fail("角色ID不能为空");
            }

            // 先删除该角色的所有权限关联
            sysRolePermissionMapper.deleteByRoleId(roleId);

            // 如果权限ID列表不为空，则重新分配权限
            if (permissionIds != null && !permissionIds.isEmpty()) {
                List<SysRolePermission> rolePermissions = new ArrayList<>();
                for (String permissionId : permissionIds) {
                    SysRolePermission rolePermission = new SysRolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(permissionId);
                    rolePermissions.add(rolePermission);
                }
                sysRolePermissionMapper.batchInsert(rolePermissions);
            }

            return R.ok("角色权限分配成功");
        } catch (Exception e) {
            return R.fail("角色权限分配失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> addPermission(SysPermission permission) {
        try {
            if (permission == null) {
                return R.fail("权限信息不能为空");
            }

            // 检查权限代码是否已存在
            SysPermission existingPermission = sysPermissionMapper
                    .selectByPermissionCode(permission.getPermissionCode());
            if (existingPermission != null) {
                return R.fail("权限代码已存在");
            }

            boolean result = save(permission);
            if (result) {
                return R.ok("权限添加成功");
            } else {
                return R.fail("权限添加失败");
            }
        } catch (Exception e) {
            return R.fail("权限添加失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> updatePermission(SysPermission permission) {
        try {
            if (permission == null || !StringUtils.hasText(permission.getId())) {
                return R.fail("权限信息不能为空");
            }

            boolean result = updateById(permission);
            if (result) {
                return R.ok("权限更新成功");
            } else {
                return R.fail("权限更新失败");
            }
        } catch (Exception e) {
            return R.fail("权限更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deletePermission(String permissionId) {
        try {
            if (!StringUtils.hasText(permissionId)) {
                return R.fail("权限ID不能为空");
            }

            // 先删除相关的角色权限关联
            sysRolePermissionMapper.deleteByPermissionId(permissionId);

            // 删除权限本身
            boolean result = removeById(permissionId);
            if (result) {
                return R.ok("权限删除成功");
            } else {
                return R.fail("权限删除失败");
            }
        } catch (Exception e) {
            return R.fail("权限删除失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<String>> getUserPermissionCodes(String userId, String userType) {
        try {
            if (!StringUtils.hasText(userId)) {
                return R.fail("用户ID不能为空");
            }

            List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByUserId(userId, userType);
            List<String> permissionCodes = permissions.stream()
                    .map(SysPermission::getPermissionCode)
                    .collect(Collectors.toList());

            return R.ok(permissionCodes);
        } catch (Exception e) {
            return R.fail("查询用户权限代码失败: " + e.getMessage());
        }
    }

    // ==================== 菜单管理实现方法 ====================

    @Override
    public R<List<SysPermission>> getAllPermissions() {
        try {
            List<SysPermission> permissions = sysPermissionMapper.selectEnabledPermissions();
            return R.ok(permissions);
        } catch (Exception e) {
            return R.fail("获取权限列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<SysPermission> getPermissionById(String permissionId) {
        try {
            SysPermission permission = sysPermissionMapper.selectById(permissionId);
            if (permission == null) {
                return R.fail("权限不存在");
            }
            return R.ok(permission);
        } catch (Exception e) {
            return R.fail("获取权限详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> batchDeletePermissions(List<String> permissionIds) {
        try {
            // 检查是否有子权限
            for (String permissionId : permissionIds) {
                QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("parent_id", permissionId);
                List<SysPermission> children = sysPermissionMapper.selectList(queryWrapper);
                if (!children.isEmpty()) {
                    return R.fail("存在子权限，无法删除");
                }
            }

            // 删除角色权限关联
            for (String permissionId : permissionIds) {
                sysRolePermissionMapper.deleteByPermissionId(permissionId);
            }

            // 删除权限
            int deleted = sysPermissionMapper.deleteBatchIds(permissionIds);
            return deleted > 0 ? R.ok("删除成功") : R.fail("删除失败");
        } catch (Exception e) {
            return R.fail("批量删除权限失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> updatePermissionSort(String permissionId, Integer sortOrder) {
        try {
            SysPermission permission = sysPermissionMapper.selectById(permissionId);
            if (permission == null) {
                return R.fail("权限不存在");
            }

            permission.setSortOrder(sortOrder);
            int updated = sysPermissionMapper.updateById(permission);
            return updated > 0 ? R.ok("更新排序成功") : R.fail("更新排序失败");
        } catch (Exception e) {
            return R.fail("更新权限排序失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<SysPermission>> getChildPermissions(String parentId) {
        try {
            QueryWrapper<SysPermission> queryWrapper = new QueryWrapper<>();
            if (parentId == null || parentId.isEmpty()) {
                queryWrapper.isNull("parent_id").or().eq("parent_id", "");
            } else {
                queryWrapper.eq("parent_id", parentId);
            }
            queryWrapper.eq("status", 1).orderByAsc("sort_order", "created_at");

            List<SysPermission> permissions = sysPermissionMapper.selectList(queryWrapper);
            return R.ok(permissions);
        } catch (Exception e) {
            return R.fail("获取子权限失败: " + e.getMessage());
        }
    }

    /**
     * 构建权限树
     *
     * @param permissions 权限列表
     * @return 权限树
     */
    private List<SysPermission> buildPermissionTree(List<SysPermission> permissions) {
        List<SysPermission> tree = new ArrayList<>();

        // 找出所有的根节点
        for (SysPermission permission : permissions) {
            if (!StringUtils.hasText(permission.getParentId()) || "0".equals(permission.getParentId())) {
                tree.add(permission);
            }
        }

        // 为每个根节点构建子树
        for (SysPermission parent : tree) {
            buildChildren(parent, permissions);
        }

        return tree;
    }

    /**
     * 递归构建子权限
     *
     * @param parent      父权限
     * @param permissions 所有权限列表
     */
    private void buildChildren(SysPermission parent, List<SysPermission> permissions) {
        List<SysPermission> children = new ArrayList<>();

        for (SysPermission permission : permissions) {
            if (parent.getId().equals(permission.getParentId())) {
                children.add(permission);
                buildChildren(permission, permissions);
            }
        }

        parent.setChildren(children);
    }
}