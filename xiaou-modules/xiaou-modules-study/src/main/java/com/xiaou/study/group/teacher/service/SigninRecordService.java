package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;

import java.util.List;

public interface SigninRecordService extends IService<SigninRecord> {

    R<SigninRecord> add(SigninRecordReq req);

    R<List<SigninResp>> listMy(String groupId);
}
