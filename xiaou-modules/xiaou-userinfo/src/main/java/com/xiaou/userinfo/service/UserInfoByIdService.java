package com.xiaou.userinfo.service;

import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.entity.ClassEntity;
import com.xiaou.userinfo.domain.entity.Major;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.domain.vo.UMajorVO;

import java.util.List;

public interface UserInfoByIdService {
    R<List<Major>> getMajor(Long id);

    R<List<ClassEntity>> getClazz(Long id);

}
