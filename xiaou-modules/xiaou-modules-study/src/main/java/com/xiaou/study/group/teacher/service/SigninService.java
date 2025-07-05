package com.xiaou.study.group.teacher.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.domain.resp.SigninRecordResp;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;

import java.util.List;

public interface SigninService extends IService<Signin> {

    R<String> add(SigninReq req);

    R<PageRespDto<SigninResp>> PageList(PageReqDto req);

    R<List<SigninRecordResp>> detail(String id);
}
