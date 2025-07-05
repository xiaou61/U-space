package com.xiaou.study.group.teacher.serivce;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;

public interface SigninRecordService extends IService<SigninRecord> {

    R<SigninRecord> add(SigninRecordReq req);
}
