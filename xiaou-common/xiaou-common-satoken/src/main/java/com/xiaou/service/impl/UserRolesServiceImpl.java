package com.xiaou.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.domain.UserRoles;
import com.xiaou.mapper.UserRolesMapper;
import com.xiaou.service.UserRolesService;
import org.springframework.stereotype.Service;

@Service
public class UserRolesServiceImpl extends ServiceImpl<UserRolesMapper,UserRoles> implements UserRolesService {
}
