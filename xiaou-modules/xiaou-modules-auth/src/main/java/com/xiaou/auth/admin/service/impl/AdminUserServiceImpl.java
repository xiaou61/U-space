package com.xiaou.auth.admin.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.constant.AdminUserConstant;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.auth.admin.service.AdminUserService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
    implements AdminUserService {
    @Resource
    private LoginHelper loginHelper;

    @Override
    public R<SaResult> login(AdminUserReq req) {
        //用户登录操作
        AdminUser user = MapstructUtils.convert(req, AdminUser.class);
        //判断用户是否存在
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(AdminUserConstant.USERNAME, user.getUsername());
        AdminUser adminUser = this.getOne(queryWrapper);
        if (adminUser==null) {
            return R.fail("用户不存在");
        }
        //存在继续校验密码
        String password= PasswordUtil.getEncryptPassword(user.getPassword());
        if (!StringUtils.equals(password, adminUser.getPassword())) {
            return R.fail("密码错误");
        }
        //都成功的话使用sa-token进行登录
        StpUtil.login(adminUser.getId());
        return R.ok(SaResult.data(StpUtil.getTokenInfo()));
    }

    @Override
    public R<String> updatePassword(String oldPassword, String newPassword) {
        AdminUser adminUser = this.getById(loginHelper.getCurrentAppUserId());
        if (!StringUtils.equals(PasswordUtil.getEncryptPassword(oldPassword), adminUser.getPassword())) {
            return R.fail("旧密码错误");
        }
        adminUser.setPassword(PasswordUtil.getEncryptPassword(newPassword));
        baseMapper.updateById(adminUser);
        //删除当前登录用户
        loginHelper.removeCurrentAppUser();
        return R.ok("修改密码成功");
    }
}




