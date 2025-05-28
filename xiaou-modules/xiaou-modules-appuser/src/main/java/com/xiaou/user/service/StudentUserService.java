package com.xiaou.user.service;


import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;

import java.util.Map;
import java.util.Set;


public interface StudentUserService extends IService<StudentUser> {

    R<Map<String, Object>> login(StudentUserBo bo);

    R<StudentUserVo> getLogin(String currentStudentId);

    R<String> uploadAvatar(String avatar);

    R<String> resetPassword(String password);

    R<String> bindEmail(String email,String code);

    R<String> forgetPassword(String email, String code, String password);

    Map<Long, StudentUser> getUserMapByIds(Set<Long> fromUserIds);
}
