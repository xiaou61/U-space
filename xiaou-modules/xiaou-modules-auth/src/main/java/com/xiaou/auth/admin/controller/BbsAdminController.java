package com.xiaou.auth.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.satoken.constant.RoleConstant;
import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.mapper.UserRolesMapper;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
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
    private UserRolesMapper userRolesMapper;

    /**
     * 添加一个BBS管理员
     */
    @PostMapping("/add")
    public R<String> add(@RequestParam String username, @RequestParam String password){
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(username);
        adminUser.setPassword(PasswordUtil.getEncryptPassword(password));
        adminUserMapper.insert(adminUser);
        loginHelper.addUserRole(adminUser.getId(), RoleConstant.BBSADMIN);
        return R.ok("添加成功");
    }
    /**
     * 删除一个BBS管理员
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id){
        loginHelper.deleteUserRole(id, RoleConstant.BBSADMIN);
        adminUserMapper.deleteById(id);
        return R.ok("删除成功");
    }
    /**
     * 查看所有BBS管理员
     */
    @GetMapping("/list")
    public R<List<AdminUser>> list(){
        QueryWrapper<UserRoles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", RoleConstant.BBSADMIN);
        List<UserRoles> userRoles = userRolesMapper.selectList(queryWrapper);
        //获取用户id 并且查找到AdminUser
        if (userRoles.isEmpty()){
            return R.ok(null);
        }
        List<String> userIds = userRoles.stream().map(UserRoles::getId).collect(Collectors.toList());
        //查找Admin用户
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(userIds);

        return R.ok(adminUsers);
    }
}
