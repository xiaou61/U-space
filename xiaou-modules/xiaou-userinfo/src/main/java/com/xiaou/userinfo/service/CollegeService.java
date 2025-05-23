package com.xiaou.userinfo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.bo.UCollegeBO;
import com.xiaou.userinfo.domain.entity.College;
import com.xiaou.userinfo.domain.vo.UCollegeVO;

public interface CollegeService extends IService<College> {
    R<UCollegeVO> addCollege(UCollegeBO collegeBO);
}
