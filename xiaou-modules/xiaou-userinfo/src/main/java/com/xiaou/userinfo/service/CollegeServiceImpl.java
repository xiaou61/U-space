package com.xiaou.userinfo.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.userinfo.domain.entity.College;
import com.xiaou.userinfo.mapper.CollegeMapper;
import org.springframework.stereotype.Service;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

}
