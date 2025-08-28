package com.xiaou.auth.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.entity.SysRole;
import com.xiaou.auth.admin.domain.entity.SysUserRole;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.auth.admin.mapper.SysUserRoleMapper;
import com.xiaou.auth.admin.service.SysRoleService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/bbs")
@Validated
/**
 * bbsAdmin账号管理
 */
public class BbsAdminController {
    @Resource
    private AdminUserMapper adminUserMapper;
    @Autowired
    private LoginHelper loginHelper;
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;
    @Resource
    private SysRoleService sysRoleService;

    /**
     * 添加一个BBS管理员
     */
    @PostMapping("/add")
    public R<String> add(@RequestParam String username, @RequestParam String password){
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setPassword(PasswordUtil.getEncryptPassword(password));
        adminUserMapper.insert(adminUser);
        
        // 为新用户分配BBS管理员角色
        R<String> roleResult = sysRoleService.assignRolesToUser(
            adminUser.getId(), 
            Arrays.asList(RoleConstant.BBSADMIN), 
            "ADMIN"
        );
        
        if (roleResult.getCode() != 200) {
            return R.fail("添加用户成功，但分配角色失败: " + roleResult.getMsg());
        }
        
        return R.ok("添加成功");
    }
    
    /**
     * 删除一个BBS管理员
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id){
        // 删除用户角色关联
        sysUserRoleMapper.deleteByUserId(id, "ADMIN");
        // 删除用户
        adminUserMapper.deleteById(id);
        return R.ok("删除成功");
    }
    
    /**
     * 查看所有BBS管理员
     */
    @GetMapping("/list")
    public R<List<AdminUser>> list(){
        QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<>();
        // 查询用户类型为ADMIN且角色为bbs_admin的用户
        queryWrapper.eq("user_type", "ADMIN");
        
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(queryWrapper);
        
        if (userRoles.isEmpty()) {
            return R.ok(null);
        }
        
        // 获取用户ID列表
        List<String> userIds = userRoles.stream()
            .map(SysUserRole::getUserId)
            .collect(Collectors.toList());
            
        // 查找Admin用户
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(userIds);
        
        // 过滤出具有BBS管理员角色的用户
        List<AdminUser> bbsAdmins = adminUsers.stream()
            .filter(user -> {
                R<List<SysRole>> rolesResult = 
                    sysRoleService.getUserRoles(user.getId(), "ADMIN");
                if (rolesResult.getCode() == 200 && rolesResult.getData() != null) {
                    return rolesResult.getData().stream()
                        .anyMatch(role -> RoleConstant.BBSADMIN.equals(role.getRoleCode()));
                }
                return false;
            })
            .collect(Collectors.toList());

        return R.ok(bbsAdmins);
    }
}
