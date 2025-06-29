package com.xiaou.satoken.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.satoken.entity.UserRoles;
import com.xiaou.satoken.mapper.UserRolesMapper;
import com.xiaou.satoken.service.UserRolesService;
import org.springframework.stereotype.Service;

@Service
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper, UserRoles> implements UserRolesService {
}
