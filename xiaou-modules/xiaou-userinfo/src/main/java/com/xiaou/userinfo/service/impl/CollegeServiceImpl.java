package com.xiaou.userinfo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;

import com.xiaou.userinfo.domain.bo.UCollegeBO;
import com.xiaou.userinfo.domain.entity.College;
import com.xiaou.userinfo.domain.vo.UCollegeVO;
import com.xiaou.userinfo.mapper.CollegeMapper;
import com.xiaou.userinfo.service.CollegeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    @Resource
    private CollegeMapper collegeMapper;


    @Override
    public R<UCollegeVO> addCollege(UCollegeBO collegeBO) {
        College college = new College();
        BeanUtils.copyProperties(collegeBO, college);
        college.setCreatedTime(LocalDateTime.now());
        Object obj = StpUtil.getSession().get("user_login");
        // 转换为 JSONObject
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
        // 提取 username
        String username = jsonObject.getString("username");
        college.setCreatedBy(username);


        collegeMapper.insert(college);
        return R.ok(UCollegeVO.fromEntity(college));
    }
}
