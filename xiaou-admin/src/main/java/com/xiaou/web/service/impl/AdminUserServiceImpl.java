package com.xiaou.web.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.AdminUserConstant;
import com.xiaou.common.domain.R;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.exception.ErrorCode;
import com.xiaou.web.domain.User;
import com.xiaou.web.domain.UserDto;
import com.xiaou.web.mapper.UserMapper;
import com.xiaou.web.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User>
        implements AdminUserService {
    @Override
    public R<SaResult> login(User user, HttpServletRequest request) {
        String username = user.getUsername();
        String password = user.getPassword();
        // 1. 校验
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return R.fail(AdminUserConstant.PARAMETER_ERROR);
        }
        // 2. 查询数据库中的用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(AdminUserConstant.USERNAME, username);
        queryWrapper.eq(AdminUserConstant.PASSWORD, password);
        User userinfo = this.baseMapper.selectOne(queryWrapper);
        // 不存在
        if (userinfo == null) {
            return R.fail("用户名或密码错误");

        }
        //3.保存到sa-token
        StpUtil.login(userinfo.getId());
        // 4. 保存用户的登录态
        StpUtil.getSession().set("user_login", userinfo);

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        // 5. 返回结果
        return R.ok("登录成功", SaResult.data(tokenInfo));
    }

    @Override
    public User getLoginUser() {
        // 判断是否已经登录
        User currentUser = (User)StpUtil.getSession().get("user_login");
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询（追求性能的话可以注释，直接返回上述结果）
        Integer userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute("user_login");
        //sa-token登录态移除
        StpUtil.logout();
        return true;
    }
}
