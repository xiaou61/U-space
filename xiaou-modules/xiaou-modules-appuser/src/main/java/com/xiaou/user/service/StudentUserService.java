package com.xiaou.user.service;


import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;


public interface StudentUserService extends IService<StudentUser> {

    R<SaResult> login(StudentUserBo bo);

    R<StudentUserVo> getLogin(String currentStudentId);

    R<String> uploadAvatar(String avatar);

    R<String> resetPassword(String password);
}
