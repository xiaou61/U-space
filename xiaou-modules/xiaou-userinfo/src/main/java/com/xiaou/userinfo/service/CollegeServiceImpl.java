package com.xiaou.userinfo.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.User;
import com.xiaou.userinfo.domain.bo.UCollegeBO;
import com.xiaou.userinfo.domain.entity.College;
import com.xiaou.userinfo.domain.vo.UCollegeVO;
import com.xiaou.userinfo.mapper.CollegeMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    @Resource
    private CollegeMapper collegeMapper;


    @Override
    public R<UCollegeVO> addCollege(UCollegeBO collegeBO) {
        College college = new College();
        BeanUtils.copyProperties(collegeBO, college);
        college.setCreatedTime(LocalDateTime.now());


        Object userObj = StpUtil.getSession().get("user_login");
        String createdBy = "admin123";  // 默认admin
        if (userObj instanceof User user && user.getUsername() != null && !user.getUsername().isBlank()) {
            createdBy = user.getUsername();
        }

        college.setCreatedBy(createdBy);
        collegeMapper.insert(college);
        return R.ok(UCollegeVO.fromEntity(college));
    }
}
